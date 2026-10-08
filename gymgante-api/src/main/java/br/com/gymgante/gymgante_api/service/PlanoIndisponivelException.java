package br.com.gymgante.gymgante_api.service;

/** Lançada quando a IA (Gemini) não consegue gerar o treino. */
public class PlanoIndisponivelException extends RuntimeException {
    public PlanoIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
