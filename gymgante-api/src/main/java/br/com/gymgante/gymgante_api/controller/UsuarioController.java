package br.com.gymgante.gymgante_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.gymgante.gymgante_api.dto.DadosCadastroUsuario;
import br.com.gymgante.gymgante_api.dto.DadosLoginUsuario;
import br.com.gymgante.gymgante_api.dto.UsuarioResponseDto;
import br.com.gymgante.gymgante_api.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Endpoint de cadastro.
     * Agora retorna UsuarioResponseDto (DTO), não mais a entidade Usuario.
     */
    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponseDto> cadastrar(@RequestBody DadosCadastroUsuario dados) {
        
        UsuarioResponseDto response = usuarioService.cadastrarUsuario(dados);
        
        // ⭐ Status 201 (Created) é mais semântico que 200 para criação
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint de login.
     * Agora retorna UsuarioResponseDto (DTO), não mais a entidade Usuario.
     */
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDto> login(@RequestBody DadosLoginUsuario dados) {
        
        UsuarioResponseDto response = usuarioService.autenticarUsuario(dados);
        
        return ResponseEntity.ok(response);
    }
}