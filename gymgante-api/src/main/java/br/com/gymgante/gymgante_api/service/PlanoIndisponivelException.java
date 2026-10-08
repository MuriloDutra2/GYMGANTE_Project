package br.com.gymgante.gymgante_api.service;

/** Lançada quando a IA (Gemini) não consegue gerar o treino. */
public class PlanoIndisponivelException extends RuntimeException {

    private final String detalhe;

    public PlanoIndisponivelException(String mensagem, String detalhe, Throwable causa) {
        super(mensagem, causa);
        this.detalhe = detalhe;
    }

    /** Resumo técnico sem dados sensíveis (ex.: "HTTP 429"), seguro para exibir ao usuário. */
    public String getDetalhe() {
        return detalhe;
    }
}
