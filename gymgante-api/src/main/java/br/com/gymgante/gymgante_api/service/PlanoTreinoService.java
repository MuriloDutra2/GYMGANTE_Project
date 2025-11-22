package br.com.gymgante.gymgante_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gymgante.gymgante_api.dto.DadosCadastroAnamnese;
import br.com.gymgante.gymgante_api.dto.DadosPlanoTreino;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlanoTreinoService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String gerarPlanoTreino(DadosCadastroAnamnese dados) {
        System.out.println("🤖 === INÍCIO GERAÇÃO DE PLANO ===");
        System.out.println("📊 Dados recebidos:");
        System.out.println("   - Objetivo: " + dados.objetivoPrincipal());
        System.out.println("   - Dias: " + dados.diasPorSemana());
        System.out.println("   - Nível: " + dados.nivel());
        System.out.println("   - Tem Restrição: " + dados.temRestricao());

        try {
            System.out.println("📝 Construindo prompt...");
            String prompt = construirPrompt(dados);
            System.out.println("✅ Prompt construído");

            System.out.println("🌐 Preparando requisição para API Gemini...");
           // Na linha onde constrói a URL, mude para:
String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + apiKey;

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            
            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            System.out.println("📤 Enviando requisição...");
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            System.out.println("📥 Resposta recebida - Status: " + response.getStatusCode());

            System.out.println("🔄 Processando resposta JSON...");
            JsonNode root = objectMapper.readTree(response.getBody());
            String respostaGemini = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            System.out.println("✅ Resposta do Gemini extraída!");
            System.out.println("📋 Tamanho da resposta: " + respostaGemini.length() + " caracteres");
            
            // Limpar a resposta (remover markdown code blocks se houver)
            String jsonLimpo = respostaGemini.trim();
            if (jsonLimpo.startsWith("```json")) {
                jsonLimpo = jsonLimpo.substring(7);
            }
            if (jsonLimpo.startsWith("```")) {
                jsonLimpo = jsonLimpo.substring(3);
            }
            if (jsonLimpo.endsWith("```")) {
                jsonLimpo = jsonLimpo.substring(0, jsonLimpo.length() - 3);
            }
            jsonLimpo = jsonLimpo.trim();
            
            // Validar se é JSON válido
            try {
                objectMapper.readTree(jsonLimpo);
                System.out.println("✅ JSON válido!");
            } catch (Exception e) {
                System.out.println("⚠️ Resposta não é JSON válido, retornando como texto");
                // Se não for JSON válido, retorna como estava antes (compatibilidade)
                return respostaGemini;
            }
            
            System.out.println("🤖 === FIM GERAÇÃO DE PLANO ===");
            
            return jsonLimpo;

        } catch (Exception e) {
            System.out.println("❌ ERRO ao gerar plano:");
            System.out.println("   Tipo: " + e.getClass().getName());
            System.out.println("   Mensagem: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao gerar plano de treino: " + e.getMessage(), e);
        }
    }

    private String construirPrompt(DadosCadastroAnamnese dados) {
        System.out.println("🔍 Construindo prompt para:");
        System.out.println("   - Objetivo: '" + dados.objetivoPrincipal() + "'");
        System.out.println("   - Frequência: '" + dados.diasPorSemana() + "'");
        System.out.println("   - Nível: '" + dados.nivel() + "'");

        String objetivo = normalizar(dados.objetivoPrincipal());
        String frequencia = normalizar(dados.diasPorSemana());
        String nivel = normalizar(dados.nivel());

        System.out.println("🔄 Valores normalizados:");
        System.out.println("   - Objetivo: '" + objetivo + "'");
        System.out.println("   - Frequência: '" + frequencia + "'");
        System.out.println("   - Nível: '" + nivel + "'");

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

        System.out.println("✅ Prompt construído com sucesso!");
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
