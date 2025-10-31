package br.com.gymgante.gymgante_api.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gymgante.gymgante_api.domain.Usuario;

// A anotação @Repository é opcional quando se estende JpaRepository,

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // (Por enquanto, deixamos o corpo da interface vazio)
    
    // O Spring Data JPA vai ler o nome deste método e criar a query:
    // "SELECT u FROM Usuario u WHERE u.email = ?"
    Usuario findByEmail(String email);

    // Também vamos precisar deste para o login por CPF
    Usuario findByCpf(String cpf);

}