package br.com.gymgante.gymgante_api.controller;

import br.com.gymgante.gymgante_api.dto.AtualizarConclusaoDto;
import br.com.gymgante.gymgante_api.dto.ConclusaoDto;
import br.com.gymgante.gymgante_api.service.ProgressoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progresso")
public class ProgressoController {

    @Autowired
    private ProgressoService progressoService;

    /** Exercícios concluídos nos últimos N dias (padrão 60). */
    @GetMapping("/{usuarioId}")
    public List<ConclusaoDto> listar(@PathVariable Long usuarioId,
                                     @RequestParam(defaultValue = "60") int dias) {
        return progressoService.listar(usuarioId, dias);
    }

    /** Marca ou desmarca um exercício como concluído. */
    @PutMapping("/{usuarioId}")
    public ResponseEntity<Void> atualizar(@PathVariable Long usuarioId,
                                          @RequestBody @Valid AtualizarConclusaoDto dto) {
        progressoService.atualizar(usuarioId, dto);
        return ResponseEntity.noContent().build();
    }
}
