package br.com.gymgante.gymgante_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.gymgante.gymgante_api.controller.dto.DadosCadastroUsuario;
import br.com.gymgante.gymgante_api.domain.Usuario;
import br.com.gymgante.gymgante_api.repository.UsuarioRepository;

@Service //Esta é uma classe de Serviço (lógica de negócio)"
public class UsuarioService {

    // --- Injeção de Dependência ---
    // Pedimos ao Spring para "injetar" as ferramentas que precisamos

    @Autowired // "Spring, por favor, me dê o Repositório de Usuário que você criou"
    private UsuarioRepository usuarioRepository;

    @Autowired // "Spring, me dê aquele PasswordEncoder (BCrypt) que definimos no SecurityConfig"
    private PasswordEncoder passwordEncoder;

    // --- Nosso primeiro método de Lógica de Negócio ---
    public Usuario cadastrarUsuario(DadosCadastroUsuario dados) {
        
        // 1. Criptografar a senha
        // Pegamos a senha pura (ex: "123456") do DTO
        // e usamos o BCrypt para transformá-la em um hash (ex: "$2a$10$...")
        String senhaCriptografada = passwordEncoder.encode(dados.senha());

        // 2. Converter o DTO (dados de entrada) para uma Entidade (dados de banco)
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNomeCompleto(dados.nomeCompleto());
        novoUsuario.setEmail(dados.email());
        novoUsuario.setCpf(dados.cpf());
        novoUsuario.setDataNascimento(dados.dataNascimento());
        novoUsuario.setTelefone(dados.telefone());
        
        // 3. Salvar a SENHA CRIPTOGRAFADA na entidade
        novoUsuario.setSenhaHash(senhaCriptografada); // <<-- MUITO IMPORTANTE!

        // 4. Dar a ordem para o Repositório salvar no banco
        // O método .save() vai executar um "INSERT INTO tb_usuario..."
        return usuarioRepository.save(novoUsuario);
    }

}