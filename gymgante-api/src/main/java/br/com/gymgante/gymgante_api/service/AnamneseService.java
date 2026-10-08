package br.com.gymgante.gymgante_api.service;

import br.com.gymgante.gymgante_api.domain.Anamnese;
import br.com.gymgante.gymgante_api.domain.Usuario;
import br.com.gymgante.gymgante_api.dto.AnamneseComTreinoDto;
import br.com.gymgante.gymgante_api.dto.DadosCadastroAnamnese;
import br.com.gymgante.gymgante_api.dto.DadosPlanoTreino;
import br.com.gymgante.gymgante_api.repository.AnamneseRepository;
import br.com.gymgante.gymgante_api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnamneseService {

    private static final String AVISO_RESTRICAO =
            "Seu formulário foi salvo, mas por ter uma restrição, pedimos que procure um profissional da academia para montar seu treino.";

    @Autowired
    private AnamneseRepository anamneseRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PlanoTreinoService planoTreinoService;

    @Transactional
    public DadosPlanoTreino salvarAnamneseEBuscarPlano(DadosCadastroAnamnese dados) {
        Usuario usuario = usuarioRepository.findById(dados.usuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (anamneseRepository.findByUsuarioId(dados.usuarioId()).isPresent()) {
            throw new RuntimeException("Este usuário já possui um treino cadastrado. Use PUT /anamnese/{usuarioId} para atualizar.");
        }

        Anamnese anamnese = new Anamnese(dados, usuario);
        return aplicarPlano(anamnese, dados);
    }

    @Transactional
    public DadosPlanoTreino atualizarAnamneseEBuscarPlano(Long usuarioId, DadosCadastroAnamnese dados) {
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Anamnese anamnese = anamneseRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Anamnese não encontrada para este usuário. Use POST /anamnese para criar."));

        anamnese.setObjetivoPrincipal(dados.objetivoPrincipal());
        anamnese.setDiasPorSemana(dados.diasPorSemana());
        anamnese.setNivel(dados.nivel());
        anamnese.setTemRestricao(dados.temRestricao());
        return aplicarPlano(anamnese, dados);
    }

    /**
     * Busca a anamnese do usuário e o treino salvo. Só chama a IA se ainda não houver treino guardado.
     */
    @Transactional
    public AnamneseComTreinoDto buscarAnamneseETreino(Long usuarioId) {
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Anamnese anamnese = anamneseRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Anamnese não encontrada para este usuário."));

        String tipo;
        String treino;
        if (Boolean.TRUE.equals(anamnese.getTemRestricao())) {
            tipo = "AVISO";
            treino = AVISO_RESTRICAO;
        } else {
            if (anamnese.getTreinoJson() == null || anamnese.getTreinoJson().isBlank()) {
                DadosCadastroAnamnese dados = new DadosCadastroAnamnese(
                        usuarioId, anamnese.getObjetivoPrincipal(), anamnese.getDiasPorSemana(),
                        anamnese.getNivel(), false);
                anamnese.setTreinoJson(planoTreinoService.gerarPlanoTreino(dados));
                anamneseRepository.save(anamnese);
            }
            tipo = "PLANO_TREINO";
            treino = anamnese.getTreinoJson();
        }

        return new AnamneseComTreinoDto(
                anamnese.getId(), usuarioId, anamnese.getObjetivoPrincipal(),
                anamnese.getDiasPorSemana(), anamnese.getNivel(), anamnese.getTemRestricao(),
                treino, tipo);
    }

    /** Salva a anamnese e, se não houver restrição, gera e guarda o treino. */
    private DadosPlanoTreino aplicarPlano(Anamnese anamnese, DadosCadastroAnamnese dados) {
        if (dados.temRestricao()) {
            anamnese.setTreinoJson(null);
            anamneseRepository.save(anamnese);
            return new DadosPlanoTreino("AVISO", AVISO_RESTRICAO);
        }

        // Se a IA falhar, a exceção desfaz a transação e nada é salvo pela metade
        String plano = planoTreinoService.gerarPlanoTreino(dados);
        anamnese.setTreinoJson(plano);
        anamneseRepository.save(anamnese);
        return new DadosPlanoTreino("PLANO_TREINO", plano);
    }
}
