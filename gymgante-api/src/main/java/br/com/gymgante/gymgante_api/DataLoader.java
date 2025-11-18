package br.com.gymgante.gymgante_api; // Pacote correto

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.com.gymgante.gymgante_api.domain.PlanoTreino;
import br.com.gymgante.gymgante_api.repository.PlanoTreinoRepository;

// Esta classe agora é "top-level", que é a forma correta
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private PlanoTreinoRepository planoTreinoRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. A combinação exata que queremos salvar
        String objetivo = "Ganho de Massa Magra";
        String dias = "3x por semana";
        String nivel = "Iniciante";

        // 2. Checa se essa combinação JÁ EXISTE
        if (planoTreinoRepository.findByObjetivoPrincipalAndDiasPorSemanaAndNivel(objetivo, dias, nivel).isEmpty()) {
            
            System.out.println(">>> INSERINDO PLANO DE TREINO DE TESTE (Hipertrofia Iniciante 3x)...");

            // 3. O JSON de treino COMPLETO e CORRETO
            String treinoJsonTeste = """
            {
              "planoNome": "Hipertrofia Básica 3x",
              "dias": [
                {
                  "dia": "Treino A (Peito, Ombro, Tríceps)",
                  "exercicios": [
                    {"nome": "Supino Reto c/ Halteres", "series": "3x10"},
                    {"nome": "Desenvolvimento c/ Halteres", "series": "3x10"},
                    {"nome": "Tríceps Pulley", "series": "3x12"}
                  ]
                },
                {
                  "dia": "Treino B (Perna Completa)",
                  "exercicios": [
                    {"nome": "Agachamento Livre", "series": "3x10"},
                    {"nome": "Leg Press 45", "series": "3x12"},
                    {"nome": "Cadeira Extensora", "series": "3x12"}
                  ]
                },
                {
                  "dia": "Treino C (Costas, Bíceps)",
                  "exercicios": [
                    {"nome": "Puxada Frontal", "series": "3x10"},
                    {"nome": "Remada Curvada", "series": "3x10"},
                    {"nome": "Rosca Direta", "series": "3x12"}
                  ]
                }
              ]
            }
            """; // Fim do Text Block

            // 4. Criar e salvar a entidade PlanoTreino
            PlanoTreino planoTeste = new PlanoTreino();
            planoTeste.setObjetivoPrincipal(objetivo);
            planoTeste.setDiasPorSemana(dias);
            planoTeste.setNivel(nivel);
            planoTeste.setTreinoJson(treinoJsonTeste);

            planoTreinoRepository.save(planoTeste);
            System.out.println(">>> PLANO DE TREINO DE TESTE INSERIDO!");
        } else {
            System.out.println(">>> Plano de treino de teste já existe. Nenhum dado foi inserido.");
        }
    }
}