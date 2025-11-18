package br.com.gymgante.gymgante_api.controller.dto;

// Este record é o "pacote" de dados que o front-end
// enviará quando o usuário preencher o formulário
public record DadosCadastroAnamnese(
    
    // Este campo é crucial.
    // O front-end precisará nos dizer PARA QUAL usuário
    // estamos salvando este formulário.
    Long usuarioId, 

    String objetivoPrincipal,
    String diasPorSemana,
    String nivel,
    boolean temRestricao

    // Nota: Não precisamos de 'restricoesTexto'
    // porque nossa lógica (que você definiu) é apenas
    // parar o processo e mandar o usuário falar com um profissional.
) {
}