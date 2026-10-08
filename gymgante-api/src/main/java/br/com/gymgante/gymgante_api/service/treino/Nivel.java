package br.com.gymgante.gymgante_api.service.treino;

import java.text.Normalizer;

public enum Nivel {
    INICIANTE(0, "Iniciante", 4),
    INTERMEDIARIO(1, "Intermediário", 5),
    AVANCADO(2, "Avançado", 6);

    private final int ordem;
    private final String rotulo;
    private final int exerciciosPorDia;

    Nivel(int ordem, String rotulo, int exerciciosPorDia) {
        this.ordem = ordem;
        this.rotulo = rotulo;
        this.exerciciosPorDia = exerciciosPorDia;
    }

    public int ordem() {
        return ordem;
    }

    public String rotulo() {
        return rotulo;
    }

    /** Quantos exercícios entram por dia (sem contar o cardio). */
    public int exerciciosPorDia() {
        return exerciciosPorDia;
    }

    /** Converte o texto do formulário; qualquer valor desconhecido vira INTERMEDIARIO. */
    public static Nivel de(String texto) {
        String t = normalizar(texto);
        if (t.startsWith("inic")) {
            return INICIANTE;
        }
        if (t.startsWith("avan")) {
            return AVANCADO;
        }
        return INTERMEDIARIO;
    }

    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
