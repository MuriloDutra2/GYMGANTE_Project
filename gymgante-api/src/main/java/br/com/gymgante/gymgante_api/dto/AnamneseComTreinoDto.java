package br.com.gymgante.gymgante_api.dto;

public record AnamneseComTreinoDto(
    Long anamneseId,
    Long usuarioId,
    String objetivoPrincipal,
    String diasPorSemana,
    String nivel,
    Boolean temRestricao,
    String treino,  // O treino gerado (markdown/texto)
    String tipo,    // "PLANO_TREINO" ou "AVISO"
    String planoDesde // data (yyyy-MM-dd) em que o plano foi gerado; pode ser nulo
) {}




