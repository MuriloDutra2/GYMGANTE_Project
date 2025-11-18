package br.com.gymgante.gymgante_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gymgante.gymgante_api.domain.Anamnese;
import br.com.gymgante.gymgante_api.domain.Usuario;

import java.util.Optional;

@Repository
public interface AnamneseRepository extends JpaRepository<Anamnese, Long> {

    // Método de busca mágico:
    // "Encontre uma Anamnese com base no objeto Usuario"
    // Isso é possível por causa do relacionamento @OneToOne que criamos.
    Optional<Anamnese> findByUsuario(Usuario usuario);
    
    // "Encontre uma Anamnese com base no ID do Usuario"
    // Isso será útil para buscarmos o formulário de um usuário logado.
    Optional<Anamnese> findByUsuarioId(Long usuarioId);

}