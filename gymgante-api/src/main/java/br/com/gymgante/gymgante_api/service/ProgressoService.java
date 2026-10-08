package br.com.gymgante.gymgante_api.service;

import br.com.gymgante.gymgante_api.domain.ConclusaoExercicio;
import br.com.gymgante.gymgante_api.dto.AtualizarConclusaoDto;
import br.com.gymgante.gymgante_api.dto.ConclusaoDto;
import br.com.gymgante.gymgante_api.repository.ConclusaoExercicioRepository;
import br.com.gymgante.gymgante_api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProgressoService {

    @Autowired
    private ConclusaoExercicioRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<ConclusaoDto> listar(Long usuarioId, int dias) {
        verificarUsuario(usuarioId);
        int janela = Math.max(1, Math.min(dias, 365));
        return repository
                .findByUsuarioIdAndDataGreaterThanEqualOrderByDataAsc(usuarioId, LocalDate.now().minusDays(janela))
                .stream()
                .map(c -> new ConclusaoDto(c.getData(), c.getDiaTreino(), c.getExercicio()))
                .toList();
    }

    @Transactional
    public void atualizar(Long usuarioId, AtualizarConclusaoDto dto) {
        verificarUsuario(usuarioId);
        var existente = repository.findByUsuarioIdAndDataAndDiaTreinoAndExercicio(
                usuarioId, dto.data(), dto.diaTreino(), dto.exercicio());

        if (dto.concluido() && existente.isEmpty()) {
            repository.save(new ConclusaoExercicio(usuarioId, dto.data(), dto.diaTreino(), dto.exercicio()));
        } else if (!dto.concluido()) {
            existente.ifPresent(repository::delete);
        }
    }

    private void verificarUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RuntimeException("Usuário não encontrado");
        }
    }
}
