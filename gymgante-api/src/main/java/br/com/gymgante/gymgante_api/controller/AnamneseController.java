package br.com.gymgante.gymgante_api.controller;

import br.com.gymgante.gymgante_api.dto.AnamneseComTreinoDto;
import br.com.gymgante.gymgante_api.dto.DadosCadastroAnamnese;
import br.com.gymgante.gymgante_api.dto.DadosPlanoTreino;
import br.com.gymgante.gymgante_api.service.AnamneseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/anamnese")
public class AnamneseController {

    @Autowired
    private AnamneseService anamneseService;

    // Endpoint POST - Criar nova anamnese
    @PostMapping
    public ResponseEntity<DadosPlanoTreino> cadastrarAnamnese(@RequestBody @Valid DadosCadastroAnamnese dados) {
        try {
            DadosPlanoTreino resultado = anamneseService.salvarAnamneseEBuscarPlano(dados);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("já possui um treino")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new DadosPlanoTreino("ERRO", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DadosPlanoTreino("ERRO", "Ocorreu um erro inesperado no servidor."));
        }
    }

    // Endpoint PUT - Atualizar anamnese existente
    @PutMapping("/{usuarioId}")
    public ResponseEntity<DadosPlanoTreino> atualizarAnamnese(
            @PathVariable Long usuarioId,
            @RequestBody @Valid DadosCadastroAnamnese dados) {
        try {
            DadosPlanoTreino resultado = anamneseService.atualizarAnamneseEBuscarPlano(usuarioId, dados);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrad")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new DadosPlanoTreino("ERRO", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DadosPlanoTreino("ERRO", "Ocorreu um erro inesperado no servidor."));
        }
    }

    // Endpoint GET - Buscar anamnese e treino do usuário
    @GetMapping("/{usuarioId}")
    public ResponseEntity<AnamneseComTreinoDto> buscarAnamneseETreino(@PathVariable Long usuarioId) {
        try {
            AnamneseComTreinoDto resultado = anamneseService.buscarAnamneseETreino(usuarioId);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrad")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}