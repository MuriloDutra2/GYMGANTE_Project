package br.com.gymgante.gymgante_api.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gymgante.gymgante_api.controller.dto.DadosCadastroUsuario;
import br.com.gymgante.gymgante_api.controller.dto.DadosLoginUsuario;
import br.com.gymgante.gymgante_api.domain.Usuario;
import br.com.gymgante.gymgante_api.service.UsuarioService;

@RestController // Diz ao Spring que esta classe é um Controlador de API REST
@RequestMapping("/api/usuarios") // Define o prefixo do URL para todos os métodos desta classe

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

    // ... (o método cadastrar e o @Autowired do serviço já estão aqui em cima) ...

    // Define que este método responde a requisições POST no URL /api/usuarios/login
    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody DadosLoginUsuario dados) {
        
        // 1. Tenta autenticar o usuário usando o serviço
        // Se as credenciais estiverem erradas, o service vai disparar uma 
        // exceção, e o Spring vai retornar um Erro 500 (que o front-end vai pegar no .catch())
        Usuario usuarioAutenticado = usuarioService.autenticarUsuario(dados);
        
        // 2. Se a autenticação deu certo, retorna "HTTP 200 OK"
        // com os dados do usuário logado.
        return ResponseEntity.ok(usuarioAutenticado);
    }

}