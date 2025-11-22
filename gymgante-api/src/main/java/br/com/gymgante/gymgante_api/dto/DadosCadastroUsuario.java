package br.com.gymgante.gymgante_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

// Vamos usar "Records" do Java moderno.
// É uma forma super concisa de criar uma classe DTO.
// O "record" automaticamente cria os campos (atributos), 
// o construtor, os getters, toString(), equals() e hashCode().
public record DadosCadastroUsuario(
    
    // O Spring vai mapear o JSON que vem do front-end
    // para estes campos, baseado no nome exato.
    
    @NotBlank(message = "Nome completo é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    String nomeCompleto,
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    String email,
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    String senha,
    
    @NotBlank(message = "CPF é obrigatório")
    @Size(min = 11, max = 11, message = "CPF deve ter 11 dígitos")
    String cpf,
    
    @NotNull(message = "Data de nascimento é obrigatória")
    LocalDate dataNascimento,
    
    String telefone
) {
}