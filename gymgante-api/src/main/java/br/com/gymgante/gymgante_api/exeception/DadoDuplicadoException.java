package br.com.gymgante.gymgante_api.exeception;

/** E-mail ou CPF já cadastrados. */
public class DadoDuplicadoException extends RuntimeException {
    public DadoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
