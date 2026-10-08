package br.com.gymgante.gymgante_api.service;

import br.com.gymgante.gymgante_api.dto.DadosCadastroAnamnese;

/** Monta o plano de treino (JSON no formato do contrato) a partir da anamnese. */
public interface GeradorDeTreino {

    /**
     * @param dados         respostas da anamnese
     * @param planoAnterior JSON do plano atual do aluno (ou null). Usado para evitar devolver um plano idêntico.
     */
    String gerar(DadosCadastroAnamnese dados, String planoAnterior);
}
