package br.com.gymgante.gymgante_api.dto;

import java.time.LocalDate;

public record ConclusaoDto(LocalDate data, String diaTreino, String exercicio) {
}
