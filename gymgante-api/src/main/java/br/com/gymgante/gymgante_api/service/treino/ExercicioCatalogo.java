package br.com.gymgante.gymgante_api.service.treino;

/**
 * Um exercício do catálogo.
 *
 * @param repeticoesFixas quando não nulo, substitui as repetições do objetivo (ex.: prancha em segundos)
 */
public record ExercicioCatalogo(
        String nome,
        GrupoMuscular grupo,
        TipoExercicio tipo,
        Nivel nivelMinimo,
        String dica,
        String repeticoesFixas) {
}
