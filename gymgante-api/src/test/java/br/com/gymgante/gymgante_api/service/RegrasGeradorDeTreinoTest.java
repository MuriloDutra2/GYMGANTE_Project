package br.com.gymgante.gymgante_api.service;

import br.com.gymgante.gymgante_api.dto.DadosCadastroAnamnese;
import br.com.gymgante.gymgante_api.service.treino.CatalogoExercicios;
import br.com.gymgante.gymgante_api.service.treino.ExercicioCatalogo;
import br.com.gymgante.gymgante_api.service.treino.Nivel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegrasGeradorDeTreinoTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final List<String> OBJETIVOS =
            List.of("Ganho de Massa Muscular", "Perda de Gordura", "Definição Muscular", "Hipertrofia");
    private static final List<Integer> FREQUENCIAS = List.of(3, 4, 5, 6);
    private static final List<String> NIVEIS = List.of("Iniciante", "Intermediário", "Avançado");
    private static final int SEEDS = 10;

    private String gerar(String objetivo, String frequencia, String nivel, long seed, String anterior) {
        var gerador = new RegrasGeradorDeTreino(MAPPER, new Random(seed));
        return gerador.gerar(new DadosCadastroAnamnese(1L, objetivo, frequencia, nivel, false), anterior);
    }

    private JsonNode plano(String objetivo, int freq, String nivel, long seed) throws Exception {
        return MAPPER.readTree(gerar(objetivo, freq + "x por semana", nivel, seed, null));
    }

    private int esperadoPorNivel(String nivel) {
        return switch (nivel) {
            case "Iniciante" -> 4;
            case "Intermediário" -> 5;
            default -> 6;
        };
    }

    @Test
    void todasAsCombinacoesGeramPlanosValidos() throws Exception {
        for (String objetivo : OBJETIVOS) {
            for (int freq : FREQUENCIAS) {
                for (String nivel : NIVEIS) {
                    for (long seed = 1; seed <= SEEDS; seed++) {
                        String contexto = objetivo + " / " + freq + "x / " + nivel + " / seed " + seed;
                        JsonNode p = plano(objetivo, freq, nivel, seed);

                        assertFalse(p.path("titulo").asText().isBlank(), "titulo vazio: " + contexto);
                        assertEquals(freq, p.path("dias").size(), "número de dias: " + contexto);

                        int esperado = esperadoPorNivel(nivel) + (objetivo.equals("Perda de Gordura") ? 1 : 0);
                        for (int d = 0; d < freq; d++) {
                            JsonNode dia = p.path("dias").get(d);
                            assertEquals("Treino " + (char) ('A' + d), dia.path("nome").asText(), contexto);
                            assertFalse(dia.path("grupoMuscular").asText().isBlank(), contexto);
                            assertEquals(esperado, dia.path("exercicios").size(), "exercícios por dia: " + contexto);

                            Set<String> nomes = new HashSet<>();
                            for (JsonNode ex : dia.path("exercicios")) {
                                assertTrue(nomes.add(ex.path("nome").asText()),
                                        "exercício repetido " + ex.path("nome").asText() + ": " + contexto);
                                for (String campo : List.of("nome", "series", "repeticoes", "descanso")) {
                                    assertFalse(ex.path(campo).asText().isBlank(),
                                            "campo " + campo + " vazio: " + contexto);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    void inicianteNuncaRecebeExercicioAvancado() throws Exception {
        for (String objetivo : OBJETIVOS) {
            for (int freq : FREQUENCIAS) {
                for (long seed = 1; seed <= SEEDS; seed++) {
                    JsonNode p = plano(objetivo, freq, "Iniciante", seed);
                    for (JsonNode dia : p.path("dias")) {
                        for (JsonNode ex : dia.path("exercicios")) {
                            String nome = ex.path("nome").asText();
                            if (nome.startsWith("Cardio")) {
                                continue;
                            }
                            ExercicioCatalogo catalogo = CatalogoExercicios.porNome(nome)
                                    .orElseThrow(() -> new AssertionError("fora do catálogo: " + nome));
                            assertNotEquals(Nivel.AVANCADO, catalogo.nivelMinimo(),
                                    nome + " é avançado e foi dado a um iniciante (" + objetivo + " / " + freq + "x)");
                        }
                    }
                }
            }
        }
    }

    @Test
    void avancadoTemTecnicaEmTodosOsDias() throws Exception {
        for (String objetivo : OBJETIVOS) {
            for (int freq : FREQUENCIAS) {
                for (long seed = 1; seed <= SEEDS; seed++) {
                    JsonNode p = plano(objetivo, freq, "Avançado", seed);
                    for (JsonNode dia : p.path("dias")) {
                        boolean tem = false;
                        for (JsonNode ex : dia.path("exercicios")) {
                            String obs = ex.path("observacoes").asText();
                            if (obs.contains("drop-set") || obs.contains("rest-pause")) {
                                tem = true;
                            }
                        }
                        assertTrue(tem, "sem técnica avançada: " + objetivo + " / " + freq + "x / seed " + seed
                                + " / " + dia.path("nome").asText());
                    }
                }
            }
        }
    }

    @Test
    void perdaDeGorduraTerminaTodoDiaComCardio() throws Exception {
        for (int freq : FREQUENCIAS) {
            for (String nivel : NIVEIS) {
                JsonNode p = plano("Perda de Gordura", freq, nivel, 7);
                for (JsonNode dia : p.path("dias")) {
                    JsonNode exercicios = dia.path("exercicios");
                    String ultimo = exercicios.get(exercicios.size() - 1).path("nome").asText();
                    assertTrue(ultimo.startsWith("Cardio"), "último exercício deveria ser cardio, foi " + ultimo);
                }
            }
        }
        // Os outros objetivos não têm cardio
        JsonNode hipertrofia = plano("Hipertrofia", 4, "Intermediário", 7);
        assertFalse(hipertrofia.toString().contains("Cardio"));
    }

    @Test
    void mesmasRespostasGeramPlanosVariados() {
        Set<String> planos = new HashSet<>();
        for (long seed = 1; seed <= 20; seed++) {
            planos.add(gerar("Hipertrofia", "4x por semana", "Intermediário", seed, null));
        }
        assertTrue(planos.size() >= 2, "o catálogo deveria variar o plano, mas só saiu " + planos.size());
    }

    @Test
    void evitaDevolverOMesmoPlanoDoAluno() {
        String primeiro = gerar("Hipertrofia", "4x por semana", "Intermediário", 1, null);
        // Mesma semente: o primeiro sorteio seria idêntico ao plano anterior, então o gerador deve sortear de novo
        String novo = gerar("Hipertrofia", "4x por semana", "Intermediário", 1, primeiro);
        assertNotEquals(primeiro, novo);
    }

    @Test
    void entradasEstranhasCaemNosPadroes() throws Exception {
        assertDoesNotThrow(() -> gerar(null, null, null, 1, null));

        JsonNode padrao = MAPPER.readTree(gerar(null, null, "xyz", 1, null));
        assertEquals(3, padrao.path("dias").size());
        assertTrue(padrao.path("titulo").asText().contains("Hipertrofia"));
        assertEquals(5, padrao.path("dias").get(0).path("exercicios").size()); // intermediário

        assertEquals(3, MAPPER.readTree(gerar("Hipertrofia", "2x", "Iniciante", 1, null)).path("dias").size());
        assertEquals(6, MAPPER.readTree(gerar("Hipertrofia", "9x", "Iniciante", 1, null)).path("dias").size());
    }

    @Test
    void jsonSegueOContratoDoFront() throws Exception {
        JsonNode p = plano("Hipertrofia", 3, "Iniciante", 1);
        assertTrue(p.has("titulo") && p.has("descricao") && p.path("dias").isArray());
        JsonNode dia = p.path("dias").get(0);
        assertTrue(dia.has("nome") && dia.has("grupoMuscular") && dia.path("exercicios").isArray());
        JsonNode ex = dia.path("exercicios").get(0);
        assertTrue(ex.has("nome") && ex.has("series") && ex.has("repeticoes") && ex.has("descanso"));
    }
}
