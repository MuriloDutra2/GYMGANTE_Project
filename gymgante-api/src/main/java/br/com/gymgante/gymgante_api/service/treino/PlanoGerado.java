package br.com.gymgante.gymgante_api.service.treino;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** Saída do gerador, com exatamente os nomes de campo do contrato JSON consumido pelo front. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlanoGerado(String titulo, String descricao, List<DiaGerado> dias) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DiaGerado(String nome, String grupoMuscular, String observacoes, List<ExercicioGerado> exercicios) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ExercicioGerado(String nome, String series, String repeticoes, String descanso, String observacoes) {
    }
}
