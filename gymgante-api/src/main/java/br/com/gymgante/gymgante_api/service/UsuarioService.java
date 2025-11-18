package br.com.gymgante.gymgante_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.gymgante.gymgante_api.controller.dto.DadosCadastroUsuario;
import br.com.gymgante.gymgante_api.controller.dto.DadosLoginUsuario;
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

    // ... (o método cadastrarUsuario e as injeções @Autowired já estão aqui em cima) ...

    // --- Nosso segundo método de Lógica de Negócio ---
    public Usuario autenticarUsuario(DadosLoginUsuario dados) {
        
        Usuario usuario;

        // 1. Verificar se o loginIdentifier é um email ou CPF
        // (Isso é uma lógica de negócio simples, perfeita para o Service)
        if (dados.loginIdentifier().contains("@")) {
            // É um email, busca por email
            usuario = usuarioRepository.findByEmail(dados.loginIdentifier());
        } else {
            // Não é um email, assume que é CPF e busca por CPF
            usuario = usuarioRepository.findByCpf(dados.loginIdentifier());
        }

        // 2. Verificar se o usuário foi encontrado
        if (usuario == null) {
            // Usuário não encontrado no banco.
            throw new RuntimeException("Credenciais inválidas");
        }

        // 3. Se o usuário foi encontrado, VERIFICAR A SENHA
        //    Usamos o passwordEncoder.matches() para comparar:
        //    A senha PURA que o usuário digitou (dados.senha())
        //    com a senha CRIPTOGRAFADA que está no banco (usuario.getSenhaHash())
        if (passwordEncoder.matches(dados.senha(), usuario.getSenhaHash())) {
            // Senha correta! Retorna o usuário encontrado.
            return usuario;
        } else {
            // Senha incorreta.
            throw new RuntimeException("Credenciais inválidas");
        }
    }

}