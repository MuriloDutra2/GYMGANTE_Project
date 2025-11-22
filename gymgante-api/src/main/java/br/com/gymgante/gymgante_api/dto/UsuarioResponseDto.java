package br.com.gymgante.gymgante_api.dto;

import java.time.LocalDate;

/**
 * DTO de resposta para operações de Usuário.
 * 
 * IMPORTANTE: NUNCA retornar a entidade Usuario diretamente porque:
 * 1. Expõe a senha criptografada (senhaHash) ao front-end
 * 2. Pode causar serialização cíclica se houver relacionamentos bidirecionais
 * 3. Viola o princípio de separação entre camada de domínio e API
 */
public record UsuarioResponseDto(
    Long id,
    String nomeCompleto,
    String email,
    String cpf,
    LocalDate dataNascimento,
    String telefone
    // ⚠️ NÃO inclui senhaHash - CRÍTICO para segurança!
    // ⚠️ NÃO inclui o objeto Anamnese - Evita referência cíclica!
) {
}