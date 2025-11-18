package br.com.gymgante.gymgante_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gymgante.gymgante_api.domain.PlanoTreino;

import java.util.Optional;

@Repository
public interface PlanoTreinoRepository extends JpaRepository<PlanoTreino, Long> {

    // --- ESTE É O MÉTODO MAIS IMPORTANTE DA LÓGICA DE TREINO ---

    // O Spring vai ler este nome longo e construir a query:
    // "SELECT p FROM PlanoTreino p WHERE 
    //  p.objetivoPrincipal = ? AND p.diasPorSemana = ? AND p.nivel = ?"
    Optional<PlanoTreino> findByObjetivoPrincipalAndDiasPorSemanaAndNivel(
        String objetivo, 
        String dias, 
        String nivel
    );

}