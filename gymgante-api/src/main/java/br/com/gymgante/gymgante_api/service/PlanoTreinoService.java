package br.com.gymgante.gymgante_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gymgante.gymgante_api.dto.DadosCadastroAnamnese;
import br.com.gymgante.gymgante_api.dto.DadosPlanoTreino;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlanoTreinoService {

    @Value("${gemini.api.key}")
    private String apiKey;

    // Perfil local: devolve um treino de exemplo em vez de chamar o Gemini
    @Value("${gemini.mock:false}")
    private boolean mock;

    private final RestTemplate restTemplate = criarRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static RestTemplate criarRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(55_000);
        return new RestTemplate(factory);
    }

    public String gerarPlanoTreino(DadosCadastroAnamnese dados) {
        if (mock) {
            return planoDeExemplo(dados);
        }
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(Map.of("parts", List.of(Map.of("text", construirPrompt(dados))))));
        requestBody.put("generationConfig", Map.of(
                "responseMimeType", "application/json",
                "maxOutputTokens", 8192));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // A chave vai no header (e não na URL) para nunca aparecer em logs de erro
        headers.set("x-goog-api-key", apiKey);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(requestBody, headers), String.class);

            JsonNode texto = objectMapper.readTree(response.getBody())
                    .path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (texto.isMissingNode() || texto.asText().isBlank()) {
                throw new IllegalStateException("Resposta do Gemini sem conteúdo: " + response.getBody());
            }

            String json = limparMarkdown(texto.asText());
            objectMapper.readTree(json); // valida; lança exceção se não for JSON
            return json;

        } catch (HttpStatusCodeException e) {
            System.err.println("❌ Gemini respondeu " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
            throw new PlanoIndisponivelException("Gemini retornou " + e.getStatusCode(), e);
        } catch (Exception e) {
            System.err.println("❌ Falha ao gerar plano: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw new PlanoIndisponivelException("Falha ao gerar plano de treino", e);
        }
    }

    private String planoDeExemplo(DadosCadastroAnamnese dados) {
        return """
            {"titulo":"Plano de exemplo - %s","descricao":"Treino fictício do ambiente local (%s, %s).",
             "dias":[
              {"nome":"Treino A","grupoMuscular":"Peito e Tríceps","exercicios":[
                {"nome":"Supino reto","series":"4x","repeticoes":"8-12","descanso":"60-90 s","observacoes":"Controle a descida"},
                {"nome":"Tríceps corda","series":"3x","repeticoes":"12","descanso":"60 s"}]},
              {"nome":"Treino B","grupoMuscular":"Costas e Bíceps","exercicios":[
                {"nome":"Puxada frontal","series":"4x","repeticoes":"10","descanso":"60-90 s"},
                {"nome":"Rosca direta","series":"3x","repeticoes":"12","descanso":"60 s"}]},
              {"nome":"Treino C","grupoMuscular":"Pernas","exercicios":[
                {"nome":"Agachamento livre","series":"4x","repeticoes":"8-10","descanso":"90 s","observacoes":"Mantenha a coluna neutra"},
                {"nome":"Leg press","series":"3x","repeticoes":"12","descanso":"90 s"}]}
             ]}
            """.formatted(dados.objetivoPrincipal(), dados.nivel(), dados.diasPorSemana());
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
