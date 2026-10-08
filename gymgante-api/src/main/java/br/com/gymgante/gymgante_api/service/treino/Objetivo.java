package br.com.gymgante.gymgante_api.service.treino;

public enum Objetivo {

    //        rótulo               séries/reps/descanso: composto          isolado             cardio
    HIPERTROFIA("Hipertrofia",
            "4", "8-10", "90 s", "3", "10-12", "60 s", false),
    DEFINICAO("Definição Muscular",
            "4", "10-12", "60 s", "3", "12-15", "45 s", false),
    PERDA_DE_GORDURA("Perda de Gordura",
            "3", "12-15", "45 s", "3", "15", "30 s", true);

    private final String rotulo;
    private final String seriesComposto;
    private final String repsComposto;
    private final String descansoComposto;
    private final String seriesIsolado;
    private final String repsIsolado;
    private final String descansoIsolado;
    private final boolean cardio;

    Objetivo(String rotulo, String seriesComposto, String repsComposto, String descansoComposto,
             String seriesIsolado, String repsIsolado, String descansoIsolado, boolean cardio) {
        this.rotulo = rotulo;
        this.seriesComposto = seriesComposto;
        this.repsComposto = repsComposto;
        this.descansoComposto = descansoComposto;
        this.seriesIsolado = seriesIsolado;
        this.repsIsolado = repsIsolado;
        this.descansoIsolado = descansoIsolado;
        this.cardio = cardio;
    }

    public String rotulo() {
        return rotulo;
    }

    public boolean temCardio() {
        return cardio;
    }

    public int series(TipoExercicio tipo) {
        return Integer.parseInt(tipo == TipoExercicio.COMPOSTO ? seriesComposto : seriesIsolado);
    }

    public String repeticoes(TipoExercicio tipo) {
        return tipo == TipoExercicio.COMPOSTO ? repsComposto : repsIsolado;
    }

    public String descanso(TipoExercicio tipo) {
        return tipo == TipoExercicio.COMPOSTO ? descansoComposto : descansoIsolado;
    }

    /** Converte o texto do formulário; qualquer valor desconhecido vira HIPERTROFIA. */
    public static Objetivo de(String texto) {
        String t = Nivel.normalizar(texto);
        if (t.contains("massa") || t.contains("hipertrofia")) {
            return HIPERTROFIA;
        }
        if (t.contains("defini")) {
            return DEFINICAO;
        }
        if (t.contains("gordura") || t.contains("emagre") || t.contains("perda")) {
            return PERDA_DE_GORDURA;
        }
        return HIPERTROFIA;
    }
}
