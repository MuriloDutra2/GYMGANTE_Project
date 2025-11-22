package br.com.gymgante.gymgante_api.dto;

// um record: simples, imutável, perfeito para DTO.
// Os nomes (loginIdentifier, senha) devem bater com o JSON do login.js
public record DadosLoginUsuario(
    String loginIdentifier,
    String senha
) {
}