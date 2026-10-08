package br.com.gymgante.gymgante_api.service;

import br.com.gymgante.gymgante_api.dto.DadosCadastroAnamnese;
import br.com.gymgante.gymgante_api.service.treino.CatalogoExercicios;
import br.com.gymgante.gymgante_api.service.treino.ExercicioCatalogo;
import br.com.gymgante.gymgante_api.service.treino.GrupoMuscular;
import br.com.gymgante.gymgante_api.service.treino.ModeloDivisao;
import br.com.gymgante.gymgante_api.service.treino.Nivel;
import br.com.gymgante.gymgante_api.service.treino.Objetivo;
import br.com.gymgante.gymgante_api.service.treino.PlanoGerado;
import br.com.gymgante.gymgante_api.service.treino.PlanoGerado.DiaGerado;
import br.com.gymgante.gymgante_api.service.treino.PlanoGerado.ExercicioGerado;
import br.com.gymgante.gymgante_api.service.treino.TipoExercicio;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gerador de treino por regras (sem IA): divisão por frequência, parâmetros por objetivo e ajustes
 * por nível, sorteando os exercícios do catálogo para variar o plano a cada geração.
 */
@Service
@ConditionalOnProperty(name = "treino.gerador", havingValue = "regras", matchIfMissing = true)
public class RegrasGeradorDeTreino implements GeradorDeTreino {

    private static final int TENTATIVAS_DIFERENTE_DO_ANTERIOR = 5;
    private static final Pattern DIGITO = Pattern.compile("(\\d)");
    private static final String OBS_AQUECIMENTO = "Aqueça 5-10 min (cardio leve + 1 série leve do primeiro exercício).";
    private static final String OBS_HIPERTROFIA = " Aumente a carga quando completar todas as repetições com boa técnica.";
    private static final String OBS_GORDURA = " Mantenha os descansos curtos para manter a frequência cardíaca alta.";
    private static final List<String> TECNICAS = List.of(
            "Técnica: drop-set na última série",
            "Técnica: rest-pause na última série");

    private final ObjectMapper objectMapper;
    private final Random random;

    @Autowired
    public RegrasGeradorDeTreino(ObjectMapper objectMapper) {
        this(objectMapper, new Random());
    }

    /** Usado pelos testes para fixar a semente do sorteio. */
    RegrasGeradorDeTreino(ObjectMapper objectMapper, Random random) {
        this.objectMapper = objectMapper;
        this.random = random;
    }

    @Override
    public String gerar(DadosCadastroAnamnese dados, String planoAnterior) {
        Objetivo objetivo = Objetivo.de(dados.objetivoPrincipal());
        Nivel nivel = Nivel.de(dados.nivel());
        int frequencia = frequencia(dados.diasPorSemana());

        String json = null;
        for (int tentativa = 1; tentativa <= TENTATIVAS_DIFERENTE_DO_ANTERIOR; tentativa++) {
            json = serializar(montar(objetivo, nivel, frequencia));
            if (planoAnterior == null || !json.equals(planoAnterior)) {
                return json;
            }
        }
        return json; // em último caso aceita repetir (catálogo pequeno)
    }

    // ---------- montagem do plano ----------

    private PlanoGerado montar(Objetivo objetivo, Nivel nivel, int frequencia) {
        List<ModeloDivisao.DiaModelo> divisao = ModeloDivisao.paraFrequencia(frequencia);
        List<DiaGerado> dias = new ArrayList<>();
        List<String> nomesDosGrupos = new ArrayList<>();

        for (int i = 0; i < divisao.size(); i++) {
            ModeloDivisao.DiaModelo modelo = divisao.get(i);
            nomesDosGrupos.add(modelo.grupoMuscular());
            dias.add(montarDia(i, modelo, objetivo, nivel));
        }

        String titulo = "Treino de " + objetivo.rotulo() + " - " + divisao.size() + "x por semana";
        String descricao = "Plano " + nivel.rotulo().toLowerCase() + " dividido em " + divisao.size()
                + " treinos (" + String.join(", ", nomesDosGrupos)
                + "). Montado automaticamente a partir da sua anamnese.";
        return new PlanoGerado(titulo, descricao, dias);
    }

    private DiaGerado montarDia(int indice, ModeloDivisao.DiaModelo modelo, Objetivo objetivo, Nivel nivel) {
        List<ExercicioCatalogo> escolhidos = new ArrayList<>();
        Set<String> usados = new HashSet<>();

        int quantidade = Math.min(nivel.exerciciosPorDia(), modelo.slots().size());
        for (int s = 0; s < quantidade; s++) {
            ModeloDivisao.Slot slot = modelo.slots().get(s);
            ExercicioCatalogo ex = escolher(slot.grupo(), slot.tipo(), nivel, usados);
            escolhidos.add(ex);
            usados.add(ex.nome());
        }

        List<ExercicioGerado> exercicios = new ArrayList<>();
        for (ExercicioCatalogo ex : escolhidos) {
            exercicios.add(new ExercicioGerado(
                    ex.nome(),
                    String.valueOf(series(objetivo, nivel, ex.tipo())),
                    ex.repeticoesFixas() != null ? ex.repeticoesFixas() : objetivo.repeticoes(ex.tipo()),
                    objetivo.descanso(ex.tipo()),
                    observacao(ex, nivel)));
        }

        if (nivel == Nivel.AVANCADO) {
            aplicarTecnicaAvancada(escolhidos, exercicios);
        }
        if (objetivo.temCardio()) {
            exercicios.add(new ExercicioGerado(
                    "Cardio (esteira, bike ou elíptico)", "1", "15-20 min", "-",
                    "Intensidade moderada: dá para falar, mas não para cantar"));
        }

        String observacoes = OBS_AQUECIMENTO;
        if (objetivo == Objetivo.HIPERTROFIA) {
            observacoes += OBS_HIPERTROFIA;
        } else if (objetivo == Objetivo.PERDA_DE_GORDURA) {
            observacoes += OBS_GORDURA;
        }
        return new DiaGerado("Treino " + (char) ('A' + indice), modelo.grupoMuscular(), observacoes, exercicios);
    }

    private int series(Objetivo objetivo, Nivel nivel, TipoExercicio tipo) {
        int series = objetivo.series(tipo);
        return nivel == Nivel.INICIANTE ? Math.max(2, series - 1) : series;
    }

    private String observacao(ExercicioCatalogo ex, Nivel nivel) {
        return switch (nivel) {
            case INICIANTE -> ex.dica();
            case INTERMEDIARIO -> ex.tipo() == TipoExercicio.COMPOSTO ? ex.dica() : null;
            case AVANCADO -> null;
        };
    }

    /** No último exercício isolado do dia (sem tempo fixo, como prancha) entra drop-set ou rest-pause. */
    private void aplicarTecnicaAvancada(List<ExercicioCatalogo> escolhidos, List<ExercicioGerado> exercicios) {
        for (int i = escolhidos.size() - 1; i >= 0; i--) {
            ExercicioCatalogo ex = escolhidos.get(i);
            if (ex.tipo() == TipoExercicio.ISOLADO && ex.repeticoesFixas() == null) {
                ExercicioGerado atual = exercicios.get(i);
                String tecnica = TECNICAS.get(random.nextInt(TECNICAS.size()));
                exercicios.set(i, new ExercicioGerado(
                        atual.nome(), atual.series(), atual.repeticoes(), atual.descanso(), tecnica));
                return;
            }
        }
    }

    // ---------- escolha de exercícios ----------

    private ExercicioCatalogo escolher(GrupoMuscular grupo, TipoExercicio tipo, Nivel nivel, Set<String> usados) {
        List<ExercicioCatalogo> doGrupo = CatalogoExercicios.doGrupo(grupo);
        int ordem = nivel.ordem();

        // Do mais restrito ao mais permissivo; o nível só é relaxado quando não há outra opção
        List<Predicate<ExercicioCatalogo>> criterios = List.of(
                e -> e.tipo() == tipo && e.nivelMinimo().ordem() <= ordem,
                e -> e.nivelMinimo().ordem() <= ordem,
                e -> e.nivelMinimo().ordem() <= ordem + 1,
                e -> true);

        for (Predicate<ExercicioCatalogo> criterio : criterios) {
            List<ExercicioCatalogo> candidatos = doGrupo.stream()
                    .filter(e -> !usados.contains(e.nome()))
                    .filter(criterio)
                    .toList();
            if (!candidatos.isEmpty()) {
                return candidatos.get(random.nextInt(candidatos.size()));
            }
        }
        throw new IllegalStateException("Catálogo sem exercício disponível para " + grupo + " / " + tipo);
    }

    // ---------- utilidades ----------

    private int frequencia(String texto) {
        if (texto != null) {
            Matcher m = DIGITO.matcher(texto);
            if (m.find()) {
                return Math.max(3, Math.min(6, Integer.parseInt(m.group(1))));
            }
        }
        return 3;
    }

    private String serializar(PlanoGerado plano) {
        try {
            return objectMapper.writeValueAsString(plano);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Não foi possível serializar o plano de treino", e);
        }
    }
}
