package br.com.gymgante.gymgante_api.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gymgante.gymgante_api.controller.dto.DadosCadastroUsuario;
import br.com.gymgante.gymgante_api.domain.Usuario;
import br.com.gymgante.gymgante_api.service.UsuarioService;

@RestController // Diz ao Spring que esta classe é um Controlador de API REST
@RequestMapping("/api/usuarios") // Define o prefixo do URL para todos os métodos desta classe
@CrossOrigin(origins = "*") // <-- IMPORTANTE: Permite que nosso front-end (em outro domínio) chame esta API
public class UsuarioController {

    @Autowired // Injeta o "cérebro" (o serviço)
    private UsuarioService usuarioService;

    // Define que este método responde a requisições POST no URL /api/usuarios/cadastro
    @PostMapping("/cadastro")
    public ResponseEntity<Usuario> cadastrar(@RequestBody DadosCadastroUsuario dados) {
        
        // 1. Pega o DTO "dados" que veio do front-end...
        
        // 2. ...e entrega para o serviço (o "cérebro") fazer o trabalho pesado
        Usuario usuarioSalvo = usuarioService.cadastrarUsuario(dados);
        
        // 3. Responde ao front-end com um "HTTP 200 OK"
        // e envia de volta o usuário que acabou de ser salvo no banco
        return ResponseEntity.ok(usuarioSalvo);
    }

}