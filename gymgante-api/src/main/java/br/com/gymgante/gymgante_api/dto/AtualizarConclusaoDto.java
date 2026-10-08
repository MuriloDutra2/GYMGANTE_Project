package br.com.gymgante.gymgante_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AtualizarConclusaoDto(
        @NotNull(message = "Data é obrigatória") LocalDate data,
        @NotBlank(message = "Dia do treino é obrigatório") @Size(max = 120) String diaTreino,
        @NotBlank(message = "Exercício é obrigatório") @Size(max = 200) String exercicio,
        boolean concluido) {
}
