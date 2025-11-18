package br.com.gymgante.gymgante_api.controller.dto;

public record AnamneseResponseDto(
    String tipo,    // Ex: "AVISO" ou "TREINO"
    String conteudo // A mensagem de aviso ou o JSON do treino
) {
}