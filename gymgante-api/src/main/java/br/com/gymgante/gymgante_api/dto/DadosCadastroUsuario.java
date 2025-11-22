package br.com.gymgante.gymgante_api.dto;

import java.time.LocalDate;

// Vamos usar "Records" do Java moderno.
// É uma forma super concisa de criar uma classe DTO.
// O "record" automaticamente cria os campos (atributos), 
// o construtor, os getters, toString(), equals() e hashCode().
public record DadosCadastroUsuario(
    
    // O Spring vai mapear o JSON que vem do front-end
    // para estes campos, baseado no nome exato.
    
   
    // O Spring (via Jackson) converte "nome_completo" para "nomeCompleto"
    String nomeCompleto,
    String email,
    String senha,
    String cpf,
    LocalDate dataNascimento,
    String telefone
) {
}