package br.com.gymgante.gymgante_api.dto;

import java.util.List;

/**
 * DTO para representar um treino estruturado com dias e exercícios
 * Usado para receber a resposta estruturada do Gemini
 */
public record TreinoEstruturadoDto(
    String titulo,
    String descricao,
    List<DiaTreinoDto> dias
) {
    public record DiaTreinoDto(
        String nome,  // Ex: "Treino A", "Segunda-feira", etc
        String grupoMuscular,  // Ex: "Pernas", "Peito", "Costas e Bíceps"
        List<ExercicioDto> exercicios,
        String observacoes  // Observações gerais do dia
    ) {}
    
    public record ExercicioDto(
        String nome,
        String series,  // Ex: "4x"
        String repeticoes,  // Ex: "10-12"
        String descanso,  // Ex: "60-90 segundos"
        String observacoes  // Observações técnicas do exercício
    ) {}
}

