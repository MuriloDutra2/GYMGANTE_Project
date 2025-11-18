package br.com.gymgante.gymgante_api.domain;

// Importando as ferramentas que vamos usar
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate; // O tipo moderno do Java para datas
import lombok.Data; // A mágica do Lombok

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Getter // Adiciona só Getters
@Setter // Adiciona só Setters
@NoArgsConstructor // Adiciona construtor vazio (JPA precisa)
@AllArgsConstructor // Adiciona construtor com todos os campos
@EqualsAndHashCode(of = "id")
@Entity // Diz ao JPA: "Esta classe é uma entidade de banco de dados"
@Table(name = "tb_usuario") // Diz ao JPA: "O nome da tabela no banco será 'tb_usuario'"
public class Usuario {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id; 
    // Mapeia o campo Java 'nomeCompleto' para a coluna 'nome_completo'
    @Column(name = "nome_completo", nullable = false) 
    private String nomeCompleto; // Padrão Java: camelCase

    @Column(nullable = false, unique = true)
    private String email;

    // Mapeia o campo Java 'senhaHash' para a coluna 'senha_hash'
    @Column(name = "senha_hash", nullable = false)
    private String senhaHash; // Vamos armazenar o hash, não a senha pura

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento; // LocalDate é o tipo ideal para data (sem hora)

    @Column(length = 15) // Define o tamanho máximo
    private String telefone;


    // O Lombok (@Data) cuida de todos os Getters e Setters
    // Não precisamos escrever public String getEmail() { ... }
    // O código fica muito mais limpo!
}