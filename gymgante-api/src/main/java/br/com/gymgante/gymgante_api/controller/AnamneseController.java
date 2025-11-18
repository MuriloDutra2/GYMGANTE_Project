package br.com.gymgante.gymgante_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gymgante.gymgante_api.controller.dto.AnamneseResponseDto;
import br.com.gymgante.gymgante_api.controller.dto.DadosCadastroAnamnese;
import br.com.gymgante.gymgante_api.service.AnamneseService;

@RestController
@RequestMapping("/api/anamnese") // <-- Novo prefixo de URL

public class AnamneseController {

    @Autowired // Injeta o "cérebro" da lógica de anamnese
    private AnamneseService anamneseService;

    // Define o endpoint para POST /api/anamnese
    @PostMapping
    public ResponseEntity<AnamneseResponseDto> cadastrarAnamnese(@RequestBody DadosCadastroAnamnese dados) {
        
        // 1. Pega o DTO (com as respostas) que veio do front-end
        
        // 2. Chama o serviço para fazer todo o trabalho pesado
        // (salvar, checar restrição, buscar plano)
        AnamneseResponseDto resposta = anamneseService.salvarAnamneseEBuscarPlano(dados);
        
        // 3. Retorna a resposta (seja o AVISO ou o TREINO)
        // com um status "HTTP 200 OK"
        return ResponseEntity.ok(resposta);
    }

}