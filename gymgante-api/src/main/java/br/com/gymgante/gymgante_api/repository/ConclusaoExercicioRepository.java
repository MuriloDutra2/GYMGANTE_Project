package br.com.gymgante.gymgante_api.repository;

import br.com.gymgante.gymgante_api.domain.ConclusaoExercicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConclusaoExercicioRepository extends JpaRepository<ConclusaoExercicio, Long> {

    List<ConclusaoExercicio> findByUsuarioIdAndDataGreaterThanEqualOrderByDataAsc(Long usuarioId, LocalDate desde);

    Optional<ConclusaoExercicio> findByUsuarioIdAndDataAndDiaTreinoAndExercicio(
            Long usuarioId, LocalDate data, String diaTreino, String exercicio);
}
