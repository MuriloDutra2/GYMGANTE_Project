package br.com.gymgante.gymgante_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gymgante.gymgante_api.dto.DadosCadastroAnamnese;
import br.com.gymgante.gymgante_api.dto.DadosPlanoTreino;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fase de IA: só entra em uso com TREINO_GERADOR=gemini (padrão é o gerador por regras)
@Service
@ConditionalOnProperty(name = "treino.gerador", havingValue = "gemini")
public class PlanoTreinoService implements GeradorDeTreino {

    @Value("${gemini.api.key}")
    private String apiKey;

    // Perfil local: devolve um treino de exemplo em vez de chamar o Gemini
    @Value("${gemini.mock:false}")
    private boolean mock;

    @jakarta.annotation.PostConstruct
    void diagnosticarChave() {
        if (mock) {
            System.out.println("🤖 Gemini: modo mock ligado (perfil local)");
            return;
        }
        String k = apiKey == null ? "" : apiKey;
        if (k.isBlank()) {
            System.out.println("⚠️ Gemini ativo mas GEMINI_API_KEY está vazia");
            return;
        }
        System.out.println("🔑 Gemini: chave com " + k.trim().length() + " caracteres, começa com 'AIza': "
                + k.trim().startsWith("AIza") + ", espaços/quebras nas pontas: " + !k.equals(k.trim()));
    }

    private final RestTemplate restTemplate = criarRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static RestTemplate criarRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(20_000);
        return new RestTemplate(factory);
    }

    // Se o primeiro modelo estiver sobrecarregado (503/429), tenta os seguintes
    private static final List<String> MODELOS = List.of("gemini-flash-latest", "gemini-2.5-flash", "gemini-2.5-flash-lite");
    private static final long LIMITE_TOTAL_MS = 35_000;

    @Override
    public String gerar(DadosCadastroAnamnese dados, String planoAnterior) {
        return gerarPlanoTreino(dados);
    }

    public String gerarPlanoTreino(DadosCadastroAnamnese dados) {
        if (mock) {
            return planoDeExemplo(dados);
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(Map.of("parts", List.of(Map.of("text", construirPrompt(dados))))));
        requestBody.put("generationConfig", Map.of(
                "responseMimeType", "application/json",
                "maxOutputTokens", 8192));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // A chave vai no header (e não na URL) para nunca aparecer em logs de erro
        headers.set("x-goog-api-key", apiKey.trim());

        long limite = System.currentTimeMillis() + LIMITE_TOTAL_MS;
        String ultimoDetalhe = "sem resposta";
        Exception ultimaCausa = null;

        for (String modelo : MODELOS) {
            for (int tentativa = 1; tentativa <= 2; tentativa++) {
                if (System.currentTimeMillis() > limite) {
                    break;
                }
                try {
                    String plano = chamarGemini(modelo, requestBody, headers);
                    if (!modelo.equals(MODELOS.get(0)) || tentativa > 1) {
                        System.out.println("✅ Plano gerado com " + modelo + " (tentativa " + tentativa + ")");
                    }
                    return plano;
                } catch (HttpStatusCodeException e) {
                    int status = e.getStatusCode().value();
                    System.err.println("❌ Gemini [" + modelo + "] respondeu " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
                    ultimoDetalhe = "Gemini HTTP " + status;
                    ultimaCausa = e;
                    boolean transitorio = status == 429 || status >= 500;
                    if (!transitorio) {
                        break; // 400/403/404: repetir não adianta, passa para o próximo modelo
                    }
                    pausar(1500L * tentativa);
                } catch (Exception e) {
                    System.err.println("❌ Falha com " + modelo + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    ultimoDetalhe = e.getClass().getSimpleName();
                    ultimaCausa = e;
                    pausar(1000);
                }
            }
        }
        throw new PlanoIndisponivelException("Falha ao gerar plano de treino", ultimoDetalhe, ultimaCausa);
    }

    private String chamarGemini(String modelo, Map<String, Object> requestBody, HttpHeaders headers) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelo + ":generateContent";
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(requestBody, headers), String.class);

        JsonNode texto = objectMapper.readTree(response.getBody())
                .path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (texto.isMissingNode() || texto.asText().isBlank()) {
            throw new IllegalStateException("Resposta do Gemini sem conteúdo");
        }

        String json = limparMarkdown(texto.asText());
        objectMapper.readTree(json); // valida; lança exceção se não for JSON
        return json;
    }

    private void pausar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private String planoDeExemplo(DadosCadastroAnamnese dados) {
        String[][] grupos = {
            {"Peito e Tríceps", "Supino reto", "Crucifixo", "Tríceps corda"},
            {"Costas e Bíceps", "Puxada frontal", "Remada curvada", "Rosca direta"},
            {"Pernas", "Agachamento livre", "Leg press", "Panturrilha em pé"},
            {"Ombros", "Desenvolvimento", "Elevação lateral", "Encolhimento"},
            {"Pernas e Glúteos", "Stiff", "Afundo", "Elevação pélvica"},
            {"Braços e Core", "Rosca martelo", "Tríceps testa", "Abdominal"}
        };
        int n = 3;
        var m = java.util.regex.Pattern.compile("(\\d)x").matcher(String.valueOf(dados.diasPorSemana()));
        if (m.find()) n = Math.max(1, Math.min(6, Integer.parseInt(m.group(1))));

        StringBuilder dias = new StringBuilder();
        for (int i = 0; i < n; i++) {
            String[] g = grupos[i % grupos.length];
            if (i > 0) dias.append(',');
            dias.append("{\"nome\":\"Treino ").append((char) ('A' + i)).append("\",\"grupoMuscular\":\"").append(g[0]).append("\",\"exercicios\":[");
            for (int j = 1; j < g.length; j++) {
                if (j > 1) dias.append(',');
                dias.append("{\"nome\":\"").append(g[j]).append("\",\"series\":\"4x\",\"repeticoes\":\"8-12\",\"descanso\":\"60-90 s\",\"observacoes\":\"Controle a descida\"}");
            }
            dias.append("]}");
        }
        return "{\"titulo\":\"Plano de exemplo - " + dados.objetivoPrincipal()
                + "\",\"descricao\":\"Treino fictício do ambiente local (" + dados.nivel() + ", " + dados.diasPorSemana()
                + ").\",\"dias\":[" + dias + "]}";
    }

    private String limparMarkdown(String texto) {
        String s = texto.trim();
        if (s.startsWith("```json")) s = s.substring(7);
        else if (s.startsWith("```")) s = s.substring(3);
        if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        return s.trim();
    }

    private String construirPrompt(DadosCadastroAnamnese dados) {

        String objetivo = normalizar(dados.objetivoPrincipal());
        String frequencia = normalizar(dados.diasPorSemana());
        String nivel = normalizar(dados.nivel());


        String templateBase = """
            Você é um personal trainer experiente. Crie um plano de treino detalhado com as seguintes características:
            
            **Perfil do Aluno:**
            - Objetivo: %s
            - Frequência: %s
            - Nível: %s
            
            **IMPORTANTE: Você DEVE responder APENAS com um JSON válido, sem texto adicional antes ou depois.**
            
            **Formato JSON obrigatório:**
            {
              "titulo": "Nome do plano (ex: 'Treino para Ganho de Massa')",
              "descricao": "Breve descrição do plano",
              "dias": [
                {
                  "nome": "Treino A (ou nome do dia, ex: Segunda-feira)",
                  "grupoMuscular": "Grupo muscular focado (ex: Pernas, Peito, Costas e Bíceps)",
                  "exercicios": [
                    {
                      "nome": "Nome do exercício",
                      "series": "Número de séries (ex: 4x)",
                      "repeticoes": "Faixa de repetições (ex: 10-12)",
                      "descanso": "Tempo de descanso (ex: 60-90 segundos)",
                      "observacoes": "Observações técnicas (opcional)"
                    }
                  ],
                  "observacoes": "Observações gerais do dia (opcional)"
                }
              ]
            }
            
            **Instruções:**
            1. Organize o treino por dias (Treino A, B, C ou dias da semana)
            2. Para cada dia, inclua 4-6 exercícios principais
            3. Cada exercício deve ter: nome, séries, repetições, descanso e observações
            4. Seja específico e prático
            5. Use nomes de exercícios comuns de academia
            
            **Responda APENAS com o JSON, sem markdown, sem explicações, sem texto adicional.**
            """;

        String instrucaoObjetivo = switch (objetivo) {
            case "perda de gordura" -> """
                
                **Foco especial:**
                - Priorize exercícios compostos que queimam mais calorias
                - Inclua treinos metabólicos (HIIT, circuitos)
                - Tempos de descanso mais curtos (30-45 segundos)
                - Combine musculação com cardio
                """;
            case "ganho de massa muscular", "hipertrofia" -> """
                
                **Foco especial:**
                - Priorize exercícios compostos e isolados
                - Volume moderado a alto (3-4 séries de 8-12 repetições)
                - Descanso adequado entre séries (60-90 segundos)
                - Progressão de carga constante
                """;
            case "definicao muscular" -> """
                
                **Foco especial:**
                - Mantenha a intensidade alta
                - Volume moderado (3-4 séries de 10-15 repetições)
                - Descansos curtos a moderados (45-60 segundos)
                - Combine treino de força com metabólico
                """;
            default -> "";
        };

        String instrucaoFrequencia = switch (frequencia) {
            case "3x por semana" -> """
                
                **Distribuição:**
                - Treino A: Corpo superior (peito, costas, ombros)
                - Treino B: Corpo inferior (pernas, glúteos)
                - Treino C: Corpo completo ou treino funcional
                """;
            case "4x por semana" -> """
                
                **Distribuição:**
                - Dia 1: Peito e Tríceps
                - Dia 2: Costas e Bíceps
                - Dia 3: Pernas e Glúteos
                - Dia 4: Ombros e Core
                """;
            case "5x por semana" -> """
                
                **Distribuição:**
                - Dia 1: Peito
                - Dia 2: Costas
                - Dia 3: Pernas (posterior)
                - Dia 4: Ombros e Braços
                - Dia 5: Pernas (anterior) e Glúteos
                """;
            case "6x por semana" -> """
                
                **Distribuição:**
                - Dia 1: Peito e Tríceps
                - Dia 2: Costas e Bíceps
                - Dia 3: Pernas (Quadríceps e Panturrilhas)
                - Dia 4: Ombros e Trapézio
                - Dia 5: Pernas (Posterior e Glúteos)
                - Dia 6: Braços e Core
                """;
            default -> "";
        };

        String instrucaoNivel = switch (nivel) {
            case "iniciante" -> """
                
                **Adaptações para iniciante:**
                - Priorize exercícios básicos e seguros
                - Foque na técnica correta
                - Volume moderado (2-3 séries)
                - Explique bem a execução de cada exercício
                """;
            case "intermediario" -> """
                
                **Adaptações para intermediário:**
                - Inclua variações de exercícios
                - Volume moderado a alto (3-4 séries)
                - Adicione técnicas de intensificação moderadas
                """;
            case "avancado" -> """
                
                **Adaptações para avançado:**
                - Inclua exercícios complexos e técnicas avançadas
                - Alto volume (4-5 séries)
                - Técnicas de intensificação (drop sets, rest-pause, etc.)
                - Maior variedade de exercícios
                """;
            default -> "";
        };

        String promptCompleto = String.format(templateBase, 
            dados.objetivoPrincipal(), 
            dados.diasPorSemana(), 
            dados.nivel()
        ) + instrucaoObjetivo + instrucaoFrequencia + instrucaoNivel;

        return promptCompleto;
    }

    private String normalizar(String texto) {
        if (texto == null) return "";
        return texto.toLowerCase()
                .trim()
                .replace("ç", "c")
                .replace("á", "a")
                .replace("à", "a")
                .replace("â", "a")
                .replace("ã", "a")
                .replace("é", "e")
                .replace("ê", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ô", "o")
                .replace("õ", "o")
                .replace("ú", "u");
    }
}
