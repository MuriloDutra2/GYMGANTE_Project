package br.com.gymgante.gymgante_api.service.treino;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static br.com.gymgante.gymgante_api.service.treino.GrupoMuscular.*;

/**
 * Divisão dos treinos por frequência semanal. Cada dia tem 6 "slots" (grupo + tipo) em ordem de
 * prioridade; o gerador usa os primeiros N conforme o nível do aluno.
 */
public final class ModeloDivisao {

    public record Slot(GrupoMuscular grupo, TipoExercicio tipo) {
    }

    public record DiaModelo(String grupoMuscular, List<Slot> slots) {
    }

    // Código do grupo nos slots: P=peito, C=costas, O=ombros, Tr=trapézio, B=bíceps, T=tríceps,
    // Q=quadríceps, Po=posterior, G=glúteos, Pa=panturrilha, Co=core. Sufixo: c=composto, i=isolado.
    private static final Map<String, GrupoMuscular> CODIGOS = Map.ofEntries(
            Map.entry("P", PEITO), Map.entry("C", COSTAS), Map.entry("O", OMBROS),
            Map.entry("Tr", TRAPEZIO), Map.entry("B", BICEPS), Map.entry("T", TRICEPS),
            Map.entry("Q", QUADRICEPS), Map.entry("Po", POSTERIOR), Map.entry("G", GLUTEOS),
            Map.entry("Pa", PANTURRILHA), Map.entry("Co", CORE));

    private static final Map<Integer, List<DiaModelo>> POR_FREQUENCIA = Map.of(
            3, List.of(
                    dia("Superiores", "Pc Cc Oc Pi Bi Ti"),
                    dia("Inferiores", "Qc Poc Gc Qi Pai Coi"),
                    dia("Corpo completo", "Qc Pc Cc Gc Oi Coi")),
            4, List.of(
                    dia("Peito e Tríceps", "Pc Pc Pi Ti Ti Pi"),
                    dia("Costas e Bíceps", "Cc Cc Cc Bi Bi Ci"),
                    dia("Pernas e Glúteos", "Qc Poc Gc Qi Poi Pai"),
                    dia("Ombros e Core", "Oc Oi Oi Tri Coi Coi")),
            5, List.of(
                    dia("Peito", "Pc Pc Pc Pi Pi Coi"),
                    dia("Costas", "Cc Cc Cc Ci Tri Coi"),
                    dia("Pernas (posterior)", "Poc Poc Poi Gc Pai Poi"),
                    dia("Ombros e Braços", "Oc Oi Bi Ti Oi Bi"),
                    dia("Pernas (anterior) e Glúteos", "Qc Qc Gc Qi Gi Pai")),
            6, List.of(
                    dia("Peito e Tríceps", "Pc Pc Pi Ti Ti Pi"),
                    dia("Costas e Bíceps", "Cc Cc Cc Bi Bi Ci"),
                    dia("Quadríceps e Panturrilhas", "Qc Qc Qc Qi Pai Pai"),
                    dia("Ombros e Trapézio", "Oc Oi Oi Tri Oi Trc"),
                    dia("Posterior e Glúteos", "Poc Gc Poi Gc Gi Poi"),
                    dia("Braços e Core", "Bi Ti Bi Ti Coi Coi")));

    private ModeloDivisao() {
    }

    /** Divisão para a frequência pedida; valores fora de 3..6 são ajustados para o limite mais próximo. */
    public static List<DiaModelo> paraFrequencia(int frequencia) {
        int f = Math.max(3, Math.min(6, frequencia));
        return POR_FREQUENCIA.get(f);
    }

    private static DiaModelo dia(String grupoMuscular, String codigos) {
        List<Slot> slots = new ArrayList<>();
        for (String token : codigos.split(" ")) {
            String codigo = token.substring(0, token.length() - 1);
            char sufixo = token.charAt(token.length() - 1);
            GrupoMuscular grupo = CODIGOS.get(codigo);
            if (grupo == null || (sufixo != 'c' && sufixo != 'i')) {
                throw new IllegalStateException("Slot inválido na divisão: " + token);
            }
            slots.add(new Slot(grupo, sufixo == 'c' ? TipoExercicio.COMPOSTO : TipoExercicio.ISOLADO));
        }
        return new DiaModelo(grupoMuscular, List.copyOf(slots));
    }
}
