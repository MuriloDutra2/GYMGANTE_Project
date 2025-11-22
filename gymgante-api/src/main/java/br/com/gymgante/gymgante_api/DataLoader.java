package br.com.gymgante.gymgante_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.com.gymgante.gymgante_api.domain.PlanoTreino;
import br.com.gymgante.gymgante_api.repository.PlanoTreinoRepository;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private PlanoTreinoRepository planoTreinoRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // ⭐ OPÇÕES PADRONIZADAS (SEM ACENTOS ESPECIAIS)
        String[] objetivos = {
            "Perda de Gordura", 
            "Ganho de Massa Magra", 
            "Condicionamento Fisico",  // ⭐ SEM ACENTO
            "Resistencia"              // ⭐ SEM ACENTO
        };

        String[] frequencias = {
            "1x por semana", 
            "2x por semana", 
            "3x por semana", 
            "4x por semana", 
            "5x por semana", 
            "6x por semana", 
            "todos os dias"
        };

        String[] niveis = {
            "Iniciante", 
            "Intermediario",  // ⭐ SEM ACENTO
            "Avancado"        // ⭐ SEM ACENTO
        };

        System.out.println("🏋️ INICIANDO CARGA DE TREINOS (84 combinações)...");

        int contagemNovos = 0;
        int contagemExistentes = 0;

        // Loop para gerar as 84 combinações
        for (String objetivo : objetivos) {
            for (String freq : frequencias) {
                for (String nivel : niveis) {

                    // Verifica se já existe
                    boolean existe = planoTreinoRepository
                            .findByObjetivoPrincipalAndDiasPorSemanaAndNivel(objetivo, freq, nivel)
                            .isPresent();

                    if (!existe) {
                        // Gera o JSON dinamicamente
                        String jsonTreino = gerarJsonDinamico(objetivo, freq, nivel);

                        // Salva no Banco
                        PlanoTreino novoPlano = new PlanoTreino();
                        novoPlano.setObjetivoPrincipal(objetivo);
                        novoPlano.setDiasPorSemana(freq);
                        novoPlano.setNivel(nivel);
                        novoPlano.setTreinoJson(jsonTreino);

                        planoTreinoRepository.save(novoPlano);
                        contagemNovos++;
                        System.out.println("✅ Criado: " + objetivo + " | " + freq + " | " + nivel);
                    } else {
                        contagemExistentes++;
                    }
                }
            }
        }

        System.out.println("📊 PROCESSO FINALIZADO:");
        System.out.println("   ➕ Novos treinos inseridos: " + contagemNovos);
        System.out.println("   ♻️  Treinos já existentes: " + contagemExistentes);
        System.out.println("   📈 Total esperado: 84");
    }

    // ⭐ GERAÇÃO DO JSON (MANTIDO IGUAL)
    private String gerarJsonDinamico(String objetivo, String freqTexto, String nivel) {
        
        int diasNumericos = converterFrequenciaParaNumero(freqTexto);
        
        String seriesReps = switch (objetivo) {
            case "Perda de Gordura" -> "4x15 (Descanso curto)";
            case "Ganho de Massa Magra" -> "3x10-12 (Carga moderada)";
            case "Resistencia" -> "3x20 (Carga leve)";
            case "Condicionamento Fisico" -> "3x15 (Circuito)";
            default -> "3x10";
        };

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"planoNome\": \"Treino de ").append(objetivo).append(" - ").append(nivel).append("\",\n");
        json.append("  \"dias\": [\n");

        for (int i = 1; i <= diasNumericos; i++) {
            char letraTreino = (char) ('A' + (i - 1));
            
            json.append("    {\n");
            json.append("      \"dia\": \"Treino ").append(letraTreino).append("\",\n");
            json.append("      \"exercicios\": [\n");
            
            json.append(criarExercicioJson("Aquecimento (Esteira/Bike)", "5 a 10 min", false));
            json.append(criarExercicioJson("Exercicio Principal " + letraTreino + ".1", seriesReps, false));
            json.append(criarExercicioJson("Exercicio Principal " + letraTreino + ".2", seriesReps, false));
            json.append(criarExercicioJson("Exercicio Secundario " + letraTreino + ".3", seriesReps, false));
            json.append(criarExercicioJson("Exercicio Isolado " + letraTreino + ".4", seriesReps, false));
            json.append(criarExercicioJson("Abdominal / Core", "3x Falha", true));
            
            json.append("      ]\n");
            json.append("    }").append(i < diasNumericos ? "," : "").append("\n");
        }

        json.append("  ]\n");
        json.append("}");
        
        return json.toString();
    }

    private String criarExercicioJson(String nome, String series, boolean ultimo) {
        return String.format("        { \"nome\": \"%s\", \"series\": \"%s\" }%s\n", 
            nome, series, ultimo ? "" : ",");
    }

    private int converterFrequenciaParaNumero(String freq) {
        if (freq.startsWith("1x")) return 1;
        if (freq.startsWith("2x")) return 2;
        if (freq.startsWith("3x")) return 3;
        if (freq.startsWith("4x")) return 4;
        if (freq.startsWith("5x")) return 5;
        if (freq.startsWith("6x")) return 6;
        if (freq.contains("todos")) return 7;
        return 3;
    }
}