package br.com.gymgante.gymgante_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gymgante.gymgante_api.domain.Usuario;
import br.com.gymgante.gymgante_api.dto.DadosCadastroUsuario;
import br.com.gymgante.gymgante_api.dto.DadosLoginUsuario;
import br.com.gymgante.gymgante_api.dto.UsuarioResponseDto;
import br.com.gymgante.gymgante_api.exeception.DadoDuplicadoException;
import br.com.gymgante.gymgante_api.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Cadastra um novo usuário e retorna um DTO (NÃO a entidade).
     */
    @Transactional
    public UsuarioResponseDto cadastrarUsuario(DadosCadastroUsuario dados) {
        
        String email = dados.email().trim().toLowerCase();
        String cpf = dados.cpf().replaceAll("\\D", "");

        if (usuarioRepository.existsByEmail(email)) {
            throw new DadoDuplicadoException("O e-mail informado já está cadastrado.");
        }
        if (usuarioRepository.existsByCpf(cpf)) {
            throw new DadoDuplicadoException("O CPF informado já está cadastrado.");
        }

        // 1. Criptografar a senha
        String senhaCriptografada = passwordEncoder.encode(dados.senha());

        // 2. Criar a entidade
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNomeCompleto(dados.nomeCompleto());
        novoUsuario.setEmail(email);
        novoUsuario.setCpf(cpf);
        novoUsuario.setDataNascimento(dados.dataNascimento());
        novoUsuario.setTelefone(dados.telefone());
        novoUsuario.setSenhaHash(senhaCriptografada);

        // 3. Salvar no banco
        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        // 4. ⭐ CONVERTER PARA DTO ANTES DE RETORNAR (NUNCA retorne a entidade!)
        return converterParaDto(usuarioSalvo);
    }

    /**
     * Autentica um usuário e retorna um DTO (NÃO a entidade).
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDto autenticarUsuario(DadosLoginUsuario dados) {
        
        Usuario usuario;

        if (dados.loginIdentifier() == null || dados.senha() == null) {
            throw new RuntimeException("Credenciais inválidas");
        }

        // 1. Buscar por email (sem diferenciar maiúsculas) ou CPF (só dígitos)
        String identificador = dados.loginIdentifier().trim();
        if (identificador.contains("@")) {
            usuario = usuarioRepository.findByEmail(identificador.toLowerCase());
        } else {
            usuario = usuarioRepository.findByCpf(identificador.replaceAll("\\D", ""));
        }

        // 2. Verificar se encontrou
        if (usuario == null) {
            throw new RuntimeException("Credenciais inválidas");
        }

        // 3. Verificar senha
        if (!passwordEncoder.matches(dados.senha(), usuario.getSenhaHash())) {
            throw new RuntimeException("Credenciais inválidas");
        }

        // 4. ⭐ RETORNAR DTO, NÃO A ENTIDADE
        return converterParaDto(usuario);
    }

    /**
     * Método privado para converter Usuario → UsuarioResponseDto.
     * Centraliza a lógica de conversão em um único lugar.
     */
    private UsuarioResponseDto converterParaDto(Usuario usuario) {
        return new UsuarioResponseDto(
            usuario.getId(),
            usuario.getNomeCompleto(),
            usuario.getEmail(),
            usuario.getCpf(),
            usuario.getDataNascimento(),
            usuario.getTelefone()
            // ⚠️ senhaHash NÃO é incluído - segurança!
        );
    }
}