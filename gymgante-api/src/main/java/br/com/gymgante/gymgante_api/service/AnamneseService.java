package br.com.gymgante.gymgante_api.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gymgante.gymgante_api.controller.dto.AnamneseResponseDto;
import br.com.gymgante.gymgante_api.controller.dto.DadosCadastroAnamnese;
import br.com.gymgante.gymgante_api.domain.Anamnese;
import br.com.gymgante.gymgante_api.domain.PlanoTreino;
import br.com.gymgante.gymgante_api.domain.Usuario;
import br.com.gymgante.gymgante_api.repository.AnamneseRepository;
import br.com.gymgante.gymgante_api.repository.PlanoTreinoRepository;
import br.com.gymgante.gymgante_api.repository.UsuarioRepository;

@Service
public class AnamneseService {

    // Precisamos de todas as "caixas de ferramenta" (Repositórios)
    @Autowired
    private AnamneseRepository anamneseRepository;

    @Autowired
    private PlanoTreinoRepository planoTreinoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Anotação importante: Garante que tudo aqui dentro
    // rode em uma "transação" (ou tudo funciona, ou nada é salvo)
    @Transactional
    public AnamneseResponseDto salvarAnamneseEBuscarPlano(DadosCadastroAnamnese dados) {

        // 1. Buscar o Usuário
        // O DTO nos deu o 'usuarioId', mas a Anamnese precisa do *objeto* Usuario.
        Usuario usuario = usuarioRepository.findById(dados.usuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // 2. Verificar se este usuário já preencheu
        Optional<Anamnese> anamneseExistente = anamneseRepository.findByUsuarioId(dados.usuarioId());
        if (anamneseExistente.isPresent()) {
            throw new RuntimeException("Este usuário já possui um treino cadastrado.");
        }

        // 3. Criar e Salvar a Anamnese (as respostas)
        Anamnese novaAnamnese = new Anamnese();
        novaAnamnese.setUsuario(usuario); // Ligamos o usuário à anamnese
        novaAnamnese.setObjetivoPrincipal(dados.objetivoPrincipal());
        novaAnamnese.setDiasPorSemana(dados.diasPorSemana());
        novaAnamnese.setNivel(dados.nivel());
        novaAnamnese.setTemRestricao(dados.temRestricao());
        
        anamneseRepository.save(novaAnamnese); // Salvamos no banco

        // 4. A LÓGICA DE NEGÓCIO (Sua regra!)
        if (dados.temRestricao()) {
            // Se tem restrição, retorna o AVISO
            return new AnamneseResponseDto(
                "AVISO", 
                "Seu formulário foi salvo, mas por ter uma restrição, pedimos que procure um profissional da academia para montar seu treino."
            );
        }

        // 5. Se NÃO tem restrição, buscar o Plano de Treino
        Optional<PlanoTreino> planoOptional = planoTreinoRepository
                .findByObjetivoPrincipalAndDiasPorSemanaAndNivel(
                        dados.objetivoPrincipal(),
                        dados.diasPorSemana(),
                        dados.nivel()
                );
        
        if (planoOptional.isEmpty()) {
            // Não encontramos um treino para essa combinação
            throw new RuntimeException("Nenhum plano de treino encontrado para esta combinação específica.");
        }

        // 6. Sucesso! Retornar o TREINO
        String jsonDoTreino = planoOptional.get().getTreinoJson();
        
        return new AnamneseResponseDto(
            "TREINO", 
            jsonDoTreino // O JSON gigante que cadastramos
        );
    }
}