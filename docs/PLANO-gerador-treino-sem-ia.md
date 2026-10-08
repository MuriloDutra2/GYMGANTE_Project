# Plano de implementação: gerador de treino sem IA (por regras)

> **Para quem vai implementar (Sonnet):** este documento é autocontido. Siga as etapas na ordem, rode os testes de cada etapa e só faça commit/push quando o usuário pedir. Todo o projeto fica em `GYMGANTE_Project/gymgante-api`.

## 1. Objetivo

Hoje o treino é gerado pelo Google Gemini, que está falhando (503 por sobrecarga no `gemini-flash-latest` e 404 nos modelos reserva `gemini-2.5-flash` / `gemini-2.5-flash-lite`, que não existem mais para contas novas).

Nesta fase o treino passa a ser montado **por regras**, só com as respostas da anamnese (objetivo, frequência, nível). A integração com o Gemini **continua no código, desligada por configuração**, para ser retomada nas próximas fases (fases de IA).

### Decisões já tomadas pelo usuário

| Decisão | Escolha |
|---|---|
| Gemini nesta fase | **Manter o código, desligado** por `treino.gerador` |
| Mesmas respostas → mesmo treino? | **Não. Variar** os exercícios a cada geração ("Novo treino" traz algo diferente) |

### Fora de escopo (não fazer agora)

- Mudanças no painel (`dashboard.*`), progresso (`/api/progresso`) ou autenticação.
- Corrigir os nomes dos modelos do Gemini (fica para a fase de IA; o log sugeriu `gemini-3.8-flash` e `gemini-3.5-flash-lite`).
- Escolha dos dias da semana pelo aluno.

---

## 2. Contrato que NÃO pode mudar

O front (`dashboard.js`, `treino.js`) e o progresso dependem deste JSON, guardado como texto em `tb_anamnese.treino_json`:

```json
{
  "titulo": "string",
  "descricao": "string",
  "dias": [
    {
      "nome": "Treino A",
      "grupoMuscular": "Peito e Tríceps",
      "observacoes": "string (opcional)",
      "exercicios": [
        { "nome": "Supino reto com barra", "series": "4", "repeticoes": "8-10",
          "descanso": "90 s", "observacoes": "string (opcional)" }
      ]
    }
  ]
}
```

Regras importantes:
- `dias[].nome` e `exercicios[].nome` são usados como **chave do progresso** (`diaTreino|exercicio`). Os nomes dos dias devem ser `Treino A`, `Treino B`, ... e **não pode haver exercício repetido dentro do mesmo dia**.
- `treino.js` só renderiza no modo estruturado se existir `titulo` e `dias` (array). Sempre preencha `titulo`.
- O número de dias deve ser igual à frequência (3 a 6). O painel distribui os dias assim: 3 → seg/qua/sex; 4 → seg/ter/qui/sex; 5 → seg a sex; 6 → seg a sáb.
- O JSON **deve ser gerado com Jackson** (`ObjectMapper.writeValueAsString`), nunca montando string na mão (acentos e aspas quebram o JSON).

Os valores que chegam do formulário (`anamnese.html`):
- objetivo: `Ganho de Massa Muscular`, `Perda de Gordura`, `Definição Muscular`, `Hipertrofia`
- frequência: `3x por semana`, `4x por semana`, `5x por semana`, `6x por semana`
- nível: `Iniciante`, `Intermediário`, `Avançado`

---

## 3. Arquitetura

```
AnamneseService ──usa──> GeradorDeTreino (interface)
                              ├── RegrasGeradorDeTreino   ← ativo quando treino.gerador=regras (PADRÃO)
                              └── PlanoTreinoService      ← Gemini, ativo só quando treino.gerador=gemini
```

### 3.1 Interface nova: `service/GeradorDeTreino.java`

```java
package br.com.gymgante.gymgante_api.service;

import br.com.gymgante.gymgante_api.dto.DadosCadastroAnamnese;

/** Monta o plano de treino (JSON no formato do contrato) a partir da anamnese. */
public interface GeradorDeTreino {

    /**
     * @param dados        respostas da anamnese
     * @param planoAnterior JSON do plano atual do aluno (ou null). Usado para evitar devolver um plano idêntico.
     */
    String gerar(DadosCadastroAnamnese dados, String planoAnterior);
}
```

### 3.2 Configuração

`src/main/resources/application.properties` — adicionar:

```properties
# Quem monta o treino: "regras" (padrão, sem IA) ou "gemini" (fase de IA)
treino.gerador=${TREINO_GERADOR:regras}
```

E trocar a linha do Gemini para não exigir a variável quando ele estiver desligado:

```properties
gemini.api.key=${GEMINI_API_KEY:}
```

`application-local.properties`: pode manter `gemini.api.key=local` e `gemini.mock=true` (não atrapalham; o bean do Gemini nem é criado com `regras`).

### 3.3 Gemini desligado: `service/PlanoTreinoService.java`

Mudanças mínimas, **sem apagar a lógica atual** (retentativas, modelos, mock, diagnóstico):

1. Anotar a classe com
   `@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "treino.gerador", havingValue = "gemini")`
2. `implements GeradorDeTreino` e adicionar:
   ```java
   @Override
   public String gerar(DadosCadastroAnamnese dados, String planoAnterior) {
       return gerarPlanoTreino(dados);
   }
   ```
3. No método `diagnosticarChave()`, se a chave estiver vazia, logar `⚠️ Gemini ativo mas GEMINI_API_KEY vazia` (sem lançar exceção).

### 3.4 `service/AnamneseService.java`

- Trocar o campo `PlanoTreinoService planoTreinoService` por `GeradorDeTreino geradorDeTreino`.
- Nas duas chamadas atuais:
  - linha ~78 (`buscarAnamneseETreino`, geração "preguiçosa" quando `treinoJson` é nulo): `geradorDeTreino.gerar(dados, null)`
  - linha ~102 (`aplicarPlano`): `geradorDeTreino.gerar(dados, anamnese.getTreinoJson())` — **ler o plano anterior antes de sobrescrever**.
- Atualizar os comentários que falam "IA" para "gerador".

`AnamneseController` não precisa mudar (o `catch (PlanoIndisponivelException)` continua valendo para o modo Gemini).

---

## 4. O gerador por regras

### 4.1 Arquivos novos (pacote `br.com.gymgante.gymgante_api.service.treino`)

| Arquivo | Conteúdo |
|---|---|
| `GrupoMuscular.java` | enum dos grupos |
| `TipoExercicio.java` | enum `COMPOSTO`, `ISOLADO` |
| `Nivel.java` | enum `INICIANTE(0)`, `INTERMEDIARIO(1)`, `AVANCADO(2)` + `static Nivel de(String)` |
| `Objetivo.java` | enum `HIPERTROFIA`, `DEFINICAO`, `PERDA_DE_GORDURA` + `static Objetivo de(String)` |
| `ExercicioCatalogo.java` | `record ExercicioCatalogo(String nome, GrupoMuscular grupo, TipoExercicio tipo, Nivel nivelMinimo, String dica, String repeticoesFixas)` |
| `CatalogoExercicios.java` | lista estática com todos os exercícios (seção 4.3) |
| `ModeloDivisao.java` | divisões por frequência e "slots" de cada dia (seção 4.4) |
| `PlanoGerado.java` | records de saída: `PlanoGerado(titulo, descricao, dias)`, `DiaGerado(nome, grupoMuscular, observacoes, exercicios)`, `ExercicioGerado(nome, series, repeticoes, descanso, observacoes)` |
| `../RegrasGeradorDeTreino.java` | o `@Service` que implementa `GeradorDeTreino` |

> Os records de saída devem usar exatamente os nomes de campo do contrato (`grupoMuscular`, `repeticoes`, ...). Para omitir `observacoes` nulas, anote os records com `@JsonInclude(JsonInclude.Include.NON_NULL)`.

### 4.2 Normalização das respostas

Normalize com: minúsculas, `trim`, sem acentos (`java.text.Normalizer` NFD + remover `\p{M}`).

- **Objetivo** (`Objetivo.de`):
  - contém `massa` ou `hipertrofia` → `HIPERTROFIA`
  - contém `defini` → `DEFINICAO`
  - contém `gordura`, `emagre` ou `perda` → `PERDA_DE_GORDURA`
  - qualquer outro / nulo → `HIPERTROFIA`
- **Nível** (`Nivel.de`): começa com `inic` → `INICIANTE`; `avan` → `AVANCADO`; senão `INTERMEDIARIO`.
- **Frequência**: primeiro dígito do texto (`(\d)`); limitar a **3..6** (sem dígito → 3).

> ⚠️ Em Java a regex é escrita `"(\\d)"` e o Normalizer usa `"\\p{M}"`. Ao editar, use a ferramenta Edit/Write. **Não** gere arquivos Java via heredoc + Python: nesta sessão isso transformou `\\d` em `\d` duas vezes e quebrou a compilação.

### 4.3 Catálogo de exercícios

Legenda: **C** = composto, **I** = isolado; nível mínimo **Ini/Int/Ava**. A dica vira `observacoes` do exercício quando o aluno é iniciante (ver 4.6). `repeticoesFixas` só existe onde indicado.

**PEITO**
| Exercício | Tipo | Nível | Dica |
|---|---|---|---|
| Supino reto com barra | C | Ini | Desça a barra até a linha do peito, com escápulas retraídas |
| Supino reto com halteres | C | Ini | Desça os halteres de forma controlada até a altura do peito |
| Supino inclinado com halteres | C | Ini | Banco entre 30° e 45°; não deixe os ombros subirem |
| Flexão de braço | C | Ini | Corpo alinhado da cabeça aos pés; desça até quase encostar o peito |
| Supino declinado | C | Int | Pés bem presos; desça a barra na parte baixa do peito |
| Mergulho nas paralelas (peito) | C | Ava | Incline o tronco à frente; desça até 90° de cotovelo |
| Crucifixo com halteres | I | Ini | Cotovelos levemente flexionados durante todo o movimento |
| Peck deck | I | Ini | Junte as mãos à frente do peito sem bater os pesos |
| Crossover na polia | I | Int | Cruze as mãos à frente do corpo e segure 1 s na contração |
| Crucifixo inclinado | I | Int | Abra até sentir alongar o peito, sem passar da linha do ombro |

**COSTAS**
| Exercício | Tipo | Nível | Dica |
|---|---|---|---|
| Puxada frontal | C | Ini | Puxe a barra até a parte alta do peito, levando os cotovelos para baixo |
| Remada baixa sentada | C | Ini | Coluna neutra; puxe com os cotovelos rentes ao corpo |
| Remada unilateral com halter | C | Ini | Apoie joelho e mão no banco; puxe o halter em direção ao quadril |
| Remada curvada com barra | C | Int | Tronco inclinado a ~45°, coluna neutra, barra até o umbigo |
| Barra fixa | C | Ava | Pegada pronada; suba até o queixo passar a barra |
| Levantamento terra | C | Ava | Barra próxima às pernas, coluna neutra, empurre o chão com os pés |
| Pulldown com braços estendidos | I | Int | Braços quase retos; leve a barra até as coxas contraindo as costas |
| Pullover com halter | I | Int | Movimento em arco, sem dobrar muito os cotovelos |

**OMBROS**
| Exercício | Tipo | Nível | Dica |
|---|---|---|---|
| Desenvolvimento com halteres | C | Ini | Empurre os halteres acima da cabeça sem arquear a lombar |
| Desenvolvimento militar com barra | C | Int | Contraia abdômen e glúteos; barra passa rente ao rosto |
| Arnold press | C | Ava | Gire as palmas durante a subida, de frente para fora |
| Elevação lateral | I | Ini | Suba os braços até a linha dos ombros, cotovelos levemente flexionados |
| Elevação frontal | I | Ini | Suba até a altura dos olhos, sem balançar o tronco |
| Crucifixo inverso | I | Ini | Tronco inclinado; abra os braços focando a parte de trás do ombro |
| Elevação lateral na polia | I | Int | Cabo cruzando à frente do corpo; suba de forma controlada |

**TRAPÉZIO**
| Exercício | Tipo | Nível | Dica |
|---|---|---|---|
| Encolhimento com halteres | I | Ini | Suba os ombros em direção às orelhas, sem girar |
| Encolhimento com barra | I | Int | Segure 1 s em cima antes de descer |
| Remada alta | C | Int | Puxe até a altura do peito, cotovelos acima das mãos |

**BÍCEPS** (todos I)
| Exercício | Nível | Dica |
|---|---|---|
| Rosca direta com barra | Ini | Cotovelos fixos ao lado do corpo, sem balançar |
| Rosca alternada com halteres | Ini | Gire o punho durante a subida |
| Rosca martelo | Ini | Pegada neutra (palmas viradas uma para a outra) |
| Rosca na polia | Ini | Mantenha a tensão do cabo também na descida |
| Rosca Scott | Int | Não estenda totalmente o cotovelo no final da descida |
| Rosca concentrada | Int | Cotovelo apoiado na coxa; suba devagar |

**TRÍCEPS**
| Exercício | Tipo | Nível | Dica |
|---|---|---|---|
| Tríceps na polia com corda | I | Ini | Cotovelos fixos; abra a corda no final do movimento |
| Tríceps coice com halter | I | Ini | Braço paralelo ao chão; estenda só o antebraço |
| Mergulho no banco | C | Ini | Mãos no banco, desça até 90° de cotovelo |
| Tríceps testa | I | Int | Desça a barra em direção à testa com os cotovelos parados |
| Tríceps francês | I | Int | Halter atrás da cabeça, cotovelos apontando para cima |
| Supino fechado | C | Ava | Mãos na largura dos ombros, cotovelos rentes ao corpo |

**QUADRÍCEPS**
| Exercício | Tipo | Nível | Dica |
|---|---|---|---|
| Leg press 45° | C | Ini | Não deixe o quadril sair do banco na descida |
| Agachamento livre | C | Ini | Joelhos na direção dos pés, coluna neutra, desça até onde mantiver a postura |
| Hack machine | C | Ini | Pés na largura do quadril; desça de forma controlada |
| Afundo | C | Ini | Passo largo; joelho de trás quase encosta no chão |
| Agachamento búlgaro | C | Int | Pé de trás apoiado no banco; tronco levemente inclinado |
| Agachamento frontal | C | Ava | Barra apoiada na frente dos ombros, cotovelos altos |
| Cadeira extensora | I | Ini | Segure 1 s com a perna estendida |

**POSTERIOR DE COXA**
| Exercício | Tipo | Nível | Dica |
|---|---|---|---|
| Stiff com halteres | C | Ini | Joelhos levemente flexionados; desça com a coluna reta |
| Levantamento terra romeno | C | Int | Empurre o quadril para trás mantendo a barra rente às pernas |
| Good morning | C | Ava | Barra nas costas; incline o tronco empurrando o quadril para trás |
| Mesa flexora | I | Ini | Não tire o quadril do banco durante a flexão |
| Cadeira flexora | I | Ini | Desça devagar, controlando o peso |
| Flexora unilateral em pé | I | Int | Uma perna por vez; não gire o quadril |

**GLÚTEOS**
| Exercício | Tipo | Nível | Dica |
|---|---|---|---|
| Elevação pélvica | C | Ini | Suba o quadril contraindo os glúteos e segure 1 s |
| Agachamento sumô | C | Ini | Pés afastados e apontados para fora |
| Passada | C | Ini | Passos longos, tronco ereto |
| Step-up no banco | C | Ini | Empurre com o calcanhar da perna de cima |
| Abdução na máquina | I | Ini | Abra as pernas de forma controlada, sem impulso |
| Glúteo na polia | I | Int | Leve a perna para trás sem arquear a lombar |

**PANTURRILHA** (todos I)
| Exercício | Nível | Dica |
|---|---|---|
| Panturrilha em pé | Ini | Amplitude total: desça o calcanhar e suba na ponta dos pés |
| Panturrilha sentado | Ini | Segure 1 s no topo |
| Panturrilha no leg press | Ini | Só os tornozelos se movem |
| Panturrilha unilateral | Int | Uma perna por vez, com apoio para equilíbrio |

**CORE** (todos I)
| Exercício | Nível | Dica | `repeticoesFixas` |
|---|---|---|---|
| Prancha | Ini | Corpo alinhado, abdômen contraído | `30-45 s` |
| Prancha lateral | Ini | Quadril alto, corpo em linha reta | `20-30 s cada lado` |
| Abdominal crunch | Ini | Suba só até tirar as escápulas do chão | — |
| Elevação de pernas | Int | Lombar colada no chão | — |
| Abdominal na polia | Int | Flexione o tronco, não puxe com os braços | — |
| Roda abdominal | Ava | Avance só até onde mantiver a lombar neutra | — |

### 4.4 Divisões por frequência ("slots")

Cada dia tem **6 slots** em ordem de prioridade (grupo + tipo). O gerador usa os **primeiros N** slots conforme o nível (seção 4.6). Notação: `P`=PEITO, `C`=COSTAS, `O`=OMBROS, `Tr`=TRAPÉZIO, `B`=BÍCEPS, `T`=TRÍCEPS, `Q`=QUADRÍCEPS, `Po`=POSTERIOR, `G`=GLÚTEOS, `Pa`=PANTURRILHA, `Co`=CORE; `c`=composto, `i`=isolado.

**3x por semana**
| Dia | grupoMuscular | Slots |
|---|---|---|
| Treino A | Superiores | P c, C c, O c, P i, B i, T i |
| Treino B | Inferiores | Q c, Po c, G c, Q i, Pa i, Co i |
| Treino C | Corpo completo | Q c, P c, C c, G c, O i, Co i |

**4x por semana**
| Dia | grupoMuscular | Slots |
|---|---|---|
| Treino A | Peito e Tríceps | P c, P c, P i, T i, T i, P i |
| Treino B | Costas e Bíceps | C c, C c, C c, B i, B i, C i |
| Treino C | Pernas e Glúteos | Q c, Po c, G c, Q i, Po i, Pa i |
| Treino D | Ombros e Core | O c, O i, O i, Tr i, Co i, Co i |

**5x por semana**
| Dia | grupoMuscular | Slots |
|---|---|---|
| Treino A | Peito | P c, P c, P c, P i, P i, Co i |
| Treino B | Costas | C c, C c, C c, C i, Tr i, Co i |
| Treino C | Pernas (posterior) | Po c, Po c, Po i, G c, Pa i, Po i |
| Treino D | Ombros e Braços | O c, O i, B i, T i, O i, B i |
| Treino E | Pernas (anterior) e Glúteos | Q c, Q c, G c, Q i, G i, Pa i |

**6x por semana**
| Dia | grupoMuscular | Slots |
|---|---|---|
| Treino A | Peito e Tríceps | P c, P c, P i, T i, T i, P i |
| Treino B | Costas e Bíceps | C c, C c, C c, B i, B i, C i |
| Treino C | Quadríceps e Panturrilhas | Q c, Q c, Q c, Q i, Pa i, Pa i |
| Treino D | Ombros e Trapézio | O c, O i, O i, Tr i, O i, Tr c |
| Treino E | Posterior e Glúteos | Po c, G c, Po i, G c, G i, Po i |
| Treino F | Braços e Core | B i, T i, B i, T i, Co i, Co i |

### 4.5 Parâmetros por objetivo

| Objetivo | Composto (séries / reps / descanso) | Isolado (séries / reps / descanso) | Extra |
|---|---|---|---|
| HIPERTROFIA | `4` / `8-10` / `90 s` | `3` / `10-12` / `60 s` | — |
| DEFINICAO | `4` / `10-12` / `60 s` | `3` / `12-15` / `45 s` | — |
| PERDA_DE_GORDURA | `3` / `12-15` / `45 s` | `3` / `15` / `30 s` | cardio no fim de cada dia (abaixo) |

- Se o exercício tiver `repeticoesFixas` (pranchas), use esse valor no lugar das reps.
- **Cardio (só PERDA_DE_GORDURA):** adicionar ao final de **cada dia** o exercício
  `nome="Cardio (esteira, bike ou elíptico)"`, `series="1"`, `repeticoes="15-20 min"`, `descanso="-"`, `observacoes="Intensidade moderada: dá para falar, mas não para cantar"`.
  Ele **não conta** no limite de slots do nível.

### 4.6 Ajustes por nível

| Nível | Slots usados por dia | Séries | Observações por exercício | Técnica avançada |
|---|---|---|---|---|
| INICIANTE | 4 | séries do objetivo **− 1** (mínimo 2) | `observacoes = dica` em **todos** os exercícios | — |
| INTERMEDIARIO | 5 | iguais à tabela | `dica` só nos **compostos** | — |
| AVANCADO | 6 | iguais à tabela | nenhuma, exceto a técnica | no **último exercício isolado** do dia: `observacoes = "Técnica: drop-set na última série"` **ou** `"Técnica: rest-pause na última série"` (sorteado) |

Observação de cada **dia** (`DiaGerado.observacoes`):
- Todos: `"Aqueça 5-10 min (cardio leve + 1 série leve do primeiro exercício)."`
- HIPERTROFIA: acrescentar `" Aumente a carga quando completar todas as repetições com boa técnica."`
- PERDA_DE_GORDURA: acrescentar `" Mantenha os descansos curtos para manter a frequência cardíaca alta."`

Título e descrição do plano:
- `titulo`: `"Treino de " + rotuloObjetivo + " - " + n + "x por semana"`, com `rotuloObjetivo` = `Hipertrofia` / `Definição Muscular` / `Perda de Gordura`.
- `descricao`: `"Plano " + rotuloNivel.toLowerCase() + " dividido em " + n + " treinos (" + nomesDosGrupos + "). Montado automaticamente a partir da sua anamnese."`, com `rotuloNivel` = `Iniciante` / `Intermediário` / `Avançado` e `nomesDosGrupos` = os `grupoMuscular` dos dias separados por `, `.

### 4.7 Algoritmo de escolha (com variedade)

```text
gerar(dados, planoAnterior):
    objetivo = Objetivo.de(...); nivel = Nivel.de(...); n = frequencia(...)
    para tentativa em 1..5:
        plano = montar(objetivo, nivel, n, random)
        json  = objectMapper.writeValueAsString(plano)
        se planoAnterior == null ou json != planoAnterior: retorna json
    retorna json            // em último caso aceita repetir (catálogo pequeno)

montar(...):
    para cada dia da divisão de n:
        usados = conjunto vazio de nomes
        para cada slot entre os primeiros K (K = 4/5/6 pelo nível):
            ex = escolher(slot.grupo, slot.tipo, nivel, usados, random)
            adiciona ex; usados += ex.nome
        se PERDA_DE_GORDURA: adiciona cardio
        aplica técnica avançada (se AVANCADO)

escolher(grupo, tipo, nivel, usados, random):
    1º  candidatos = catálogo do grupo, mesmo tipo, nivelMinimo <= nivel, não usados
    2º  se vazio: mesmo grupo, QUALQUER tipo, nivelMinimo <= nivel, não usados
    3º  se vazio: mesmo grupo, qualquer tipo, nivelMinimo <= nivel + 1, não usados
    4º  se vazio: mesmo grupo, qualquer tipo, QUALQUER nível, não usados
    5º  se vazio: lança IllegalStateException (é bug de catálogo; o teste deve pegar)
    retorna candidatos.get(random.nextInt(candidatos.size()))
```

> Por que o 3º passo existe: no `5x / Iniciante`, o Treino B (Costas) usa 3 compostos de nível Ini (todos os que existem) e o 4º slot é `C i`, que não tem opção Ini. Sem relaxar **um** nível por vez, o 4º passo poderia sortear `Barra fixa` ou `Levantamento terra` (Ava) para um iniciante. Com o 3º passo ele recebe `Pulldown`, `Pullover` ou `Remada curvada` (Int). Conferi as demais combinações de iniciante: nenhuma chega ao 4º passo.

Notas:
- Compostos vêm antes porque os slots já estão ordenados assim; **não reordene**.
- `RegrasGeradorDeTreino` deve ter **dois construtores**:
  - `@Autowired` público, recebendo `ObjectMapper` (do Spring) e usando `new Random()` (variedade real);
  - package-private recebendo `(ObjectMapper, Random)` para os testes usarem `new Random(seed)`.
- Anotar a classe com `@Service` e
  `@ConditionalOnProperty(name = "treino.gerador", havingValue = "regras", matchIfMissing = true)`.
- `gerar` nunca deve lançar `PlanoIndisponivelException`. Se a serialização do Jackson falhar, lance `IllegalStateException` (o controller devolve 500).

---

## 5. Limpeza

1. **Apagar** `DataLoader.java` (grava 84 treinos fictícios na tabela `tb_plano_treino` a cada subida e nada os usa).
2. **Apagar** `domain/PlanoTreino.java` e `repository/PlanoTreinoRepository.java` (só eram usados pelo DataLoader).
   - Antes, confirme com `Grep` por `PlanoTreino\b` e `PlanoTreinoRepository` que não há outros usos (cuidado para não confundir com `PlanoTreinoService` e `DadosPlanoTreino`, que **ficam**).
   - A tabela `tb_plano_treino` que já existe no Neon fica órfã e inofensiva (o `ddl-auto=update` não apaga tabelas). Não a apague sem pedir ao usuário.
3. `Anamnese.java`, linha ~30: comentário "Treino gerado pela IA" → "Treino gerado (regras ou IA)".

---

## 6. Textos do front e README

| Arquivo | Trecho atual | Novo |
|---|---|---|
| `static/anamnese.html:17` | `...gerar seu plano de treino com IA.` | `...montar seu plano de treino personalizado.` |
| `static/anamnese.html:60` | `🤖 Gerar Treino com IA` | `💪 Gerar meu treino` |
| `static/js/anamnese.js:103` | `'Enviando dados e gerando treino com IA...'` | `'Montando seu treino...'` |
| `static/js/anamnese.js:213` | `'...Verifique a conexão com a API de IA.'` | `'Erro ao gerar treino. Tente novamente.'` |
| `static/js/anamnese.js` (mensagens de loading `🤖 Gerando seu treino personalizado...` e `Isso pode levar até 15 segundos`) | | `💪 Montando seu treino...` e `Só um instante...` |
| `static/js/dashboard.js:104` | `'O serviço de IA está indisponível...'` | `'Não foi possível gerar seu treino agora. Tente novamente em instantes.'` |
| `static/dashboard.html` (dica do carregamento) | `(na primeira vez o treino pode levar alguns segundos para ser gerado)` | remover a dica |
| `static/js/config.js:14` | comentário `(IA + cold start do Render)` | `(cold start do Render)` |

**README.md**: ajustar para refletir a fase atual sem apagar a visão de IA:
- Título: `# 🏋️ GymGante - Sistema de Treinos Personalizados`
- Parágrafo de abertura: o treino é montado por um **motor de regras** (divisão por frequência, parâmetros por objetivo e ajustes por nível, com variação de exercícios); a geração com IA (Gemini) está implementada e **desligada**, prevista para as próximas fases (`TREINO_GERADOR=gemini`).
- Seção "Diferenciais Técnicos": trocar o item de engenharia de prompt por "Motor de regras" e manter o item da trava de segurança por restrição médica.
- Stack: `Integração IA: Google Gemini API (desligada nesta fase)`.
- Pré-requisitos e variáveis: `GEMINI_API_KEY` passa a ser **opcional**; documentar `TREINO_GERADOR` (`regras` padrão | `gemini`).
- Remover o badge `AI-Google_Gemini` ou trocar por `Treinos-Motor_de_regras`.

---

## 7. Testes

### 7.1 Teste unitário (novo)

Arquivo: `src/test/java/br/com/gymgante/gymgante_api/service/RegrasGeradorDeTreinoTest.java` (JUnit 5, **sem** `@SpringBootTest`; instancie com `new RegrasGeradorDeTreino(new ObjectMapper(), new Random(seed))`).

Casos:
1. **Todas as combinações**: 4 objetivos × 4 frequências × 3 níveis × 10 seeds. Para cada uma:
   - o resultado é JSON válido, com `titulo` não vazio e `dias.size() == n`;
   - os nomes dos dias são `Treino A`, `Treino B`, ...;
   - quantidade de exercícios por dia: 4/5/6 pelo nível (**+1** se perda de gordura);
   - não há nomes de exercício repetidos dentro do mesmo dia;
   - todos os exercícios têm `nome`, `series`, `repeticoes` e `descanso` não vazios.
2. **Iniciante não recebe exercício de nível Avançado**, em todas as combinações e seeds (o catálogo e o passo 3º da seção 4.7 foram pensados para isso; se falhar, ajuste o catálogo, não o teste). Para checar o nível, o teste pode consultar `CatalogoExercicios` pelo nome do exercício.
3. **Avançado** tem a observação de técnica (`drop-set` ou `rest-pause`) em todos os dias.
4. **Perda de gordura** termina todo dia com o cardio.
5. **Variedade**: para `Hipertrofia / 4x / Intermediário`, seeds 1..20 geram **pelo menos 2** planos diferentes.
6. **Evita repetir o anterior**: gere um plano, passe-o como `planoAnterior` e confira que o novo é diferente.
7. **Entradas estranhas**: objetivo `null`, nível `"xyz"`, frequência `"2x"` e `"9x"` não lançam exceção (caem nos padrões: hipertrofia, intermediário, 3 e 6 dias).

Rodar: `./mvnw test` (dentro de `gymgante-api`). Não há outros testes no projeto hoje.

### 7.2 Teste manual no STG local

```bash
cd gymgante-api
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

- Perfil `local` = banco H2 em memória (zera a cada reinício) e `treino.gerador` padrão (`regras`). **Não** use o Neon.
- No Windows, para reiniciar, mate o processo da porta 8080 com PowerShell:
  `$c = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue; if ($c) { Stop-Process -Id $c.OwningProcess -Force }`
  (`pkill` não existe neste ambiente).
- Pelo navegador interno, em `http://localhost:8080` (pode criar conta de teste só em localhost):
  1. cadastro → login → anamnese (`Perda de Gordura`, `5x`, `Iniciante`) → painel mostra o treino do dia com 4 exercícios + cardio;
  2. "Plano completo" (`treino.html`) renderiza os 5 dias em cartões (modo estruturado, não markdown);
  3. marcar exercícios no painel e recarregar: o progresso se mantém;
  4. "Novo treino" com as **mesmas** respostas → exercícios diferentes;
  5. "Novo treino" com `Avançado` e `6x` → 6 dias, 6 exercícios por dia, técnica no último isolado;
  6. marcar restrição médica → aviso, sem treino;
  7. checar que nenhum texto da tela ainda fala em "IA".
- Confirmar no log que **não** aparece `🔑 Gemini` (bean do Gemini desligado).

### 7.3 Depois do deploy (pelo usuário)

No site publicado eu não crio contas. Peça ao usuário para gerar um treino em https://gymgante-api.onrender.com e confirmar que sai na hora, sem `[Gemini HTTP ...]`. No Render, **não** é preciso apagar a variável `GEMINI_API_KEY`; o padrão `treino.gerador=regras` já desliga o Gemini.

---

## 8. Critérios de aceite

- [ ] `./mvnw test` passa com o teste novo.
- [ ] Com o padrão (`regras`), o app sobe **sem** `GEMINI_API_KEY` definida.
- [ ] Com `TREINO_GERADOR=gemini`, o `PlanoTreinoService` volta a ser usado (verificar subindo o perfil local com `-Dtreino.gerador=gemini`: deve logar `🤖 Gemini: modo mock ligado` e o painel recebe o "Plano de exemplo").
- [ ] Gerar treino é instantâneo e nunca retorna 503.
- [ ] "Novo treino" com as mesmas respostas devolve um plano diferente.
- [ ] Painel, plano completo e progresso funcionam sem mudanças de código no front (só os textos da seção 6).
- [ ] Nenhum texto da interface fala em IA.
- [ ] `DataLoader`, `PlanoTreino` e `PlanoTreinoRepository` removidos; compila sem avisos de import não usado.

## 9. Ordem sugerida de execução

1. Seção 3 (interface, config, Gemini condicional, `AnamneseService`). Compilar.
2. Seção 4 (enums, catálogo, divisões, records, `RegrasGeradorDeTreino`). Compilar.
3. Seção 7.1 (teste unitário). `./mvnw test` até passar.
4. Seção 5 (limpeza). Compilar e testar de novo.
5. Seção 6 (textos e README).
6. Seção 7.2 (STG no navegador).
7. Resumo ao usuário e, **só se ele pedir**, commit + push. A mensagem do commit deve terminar com a linha de coautoria indicada pelo sistema no momento.

## 10. Próximas fases (IA), para referência

- Trocar `MODELOS` no `PlanoTreinoService` pelos modelos atuais (o erro 404 sugeriu `gemini-3.8-flash` e `gemini-3.5-flash-lite`; confirmar na documentação do Google antes).
- Usar o gerador por regras como **reserva automática** quando o Gemini falhar (as duas implementações já seguem a mesma interface).
- Opcional: dar ao Gemini o catálogo de exercícios no prompt, para manter nomes consistentes com o histórico do progresso.
