package br.com.gymgante.gymgante_api.service;

import br.com.gymgante.gymgante_api.domain.Anamnese;
import br.com.gymgante.gymgante_api.domain.Usuario;
import br.com.gymgante.gymgante_api.dto.DadosCadastroAnamnese;
import br.com.gymgante.gymgante_api.dto.DadosPlanoTreino;
import br.com.gymgante.gymgante_api.repository.AnamneseRepository;
import br.com.gymgante.gymgante_api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AnamneseService {

    @Autowired
    private AnamneseRepository anamneseRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PlanoTreinoService planoTreinoService;  // ⬅️ INJETAR O PlanoTreinoService

    @Transactional
    public DadosPlanoTreino salvarAnamneseEBuscarPlano(DadosCadastroAnamnese dados) {
        System.out.println("📋 INÍCIO - salvarAnamneseEBuscarPlano");
        System.out.println("📋 Dados recebidos: " + dados);

        try {
            // Buscar usuário
            System.out.println("🔍 Buscando usuário ID: " + dados.usuarioId());
            Usuario usuario = usuarioRepository.findById(dados.usuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            System.out.println("✅ Usuário encontrado: " + usuario.getNomeCompleto());
            
            // Verificar se já tem anamnese
            System.out.println("🔍 Verificando se usuário já tem anamnese...");
            Optional<Anamnese> anamneseExistente = anamneseRepository.findByUsuarioId(dados.usuarioId());
            
            if (anamneseExistente.isPresent()) {
                System.out.println("⚠️ ERRO: Usuário já tem anamnese!");
                throw new RuntimeException("Este usuário já possui um treino cadastrado. Use PUT /anamnese/{usuarioId} para atualizar.");
            }

            System.out.println("✅ Usuário não tem anamnese ainda");
            
            // Criar e salvar anamnese
            System.out.println("📝 Criando nova anamnese...");
           Anamnese anamnese = new Anamnese();
anamnese.setUsuario(usuario);
anamnese.setObjetivoPrincipal(dados.objetivoPrincipal());
anamnese.setDiasPorSemana(dados.diasPorSemana());
anamnese.setNivel(dados.nivel());
anamnese.setTemRestricao(dados.temRestricao());
            
            System.out.println("💾 Salvando anamnese no banco...");
            anamnese = anamneseRepository.save(anamnese);
            System.out.println("✅ Anamnese salva com sucesso!");

            // Verificar restrição
            System.out.println("🔍 Verificando se tem restrição...");
            if (dados.temRestricao()) {
                System.out.println("⚠️ Usuário tem restrição - retornando aviso");
                return new DadosPlanoTreino(
                    "AVISO",
                    "Seu formulário foi salvo, mas por ter uma restrição, pedimos que procure um profissional da academia para montar seu treino."
                );
            }

            // USAR O GEMINI para gerar o plano
            System.out.println("🤖 Chamando Gemini para gerar plano de treino...");
            String planoGerado = planoTreinoService.gerarPlanoTreino(dados);
            System.out.println("✅ Plano gerado com sucesso pelo Gemini!");

            return new DadosPlanoTreino("PLANO_TREINO", planoGerado);

        } catch (Exception e) {
            System.out.println("❌ EXCEÇÃO CAPTURADA:");
            System.out.println("   Mensagem: " + e.getMessage());
            System.out.println("   Tipo: " + e.getClass().getName());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public DadosPlanoTreino atualizarAnamneseEBuscarPlano(Long usuarioId, DadosCadastroAnamnese dados) {
        System.out.println("🔄 INÍCIO - atualizarAnamneseEBuscarPlano");
        System.out.println("🔄 Usuário ID: " + usuarioId);
        System.out.println("🔄 Novos dados: " + dados);

        try {
            // Buscar usuário
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            System.out.println("✅ Usuário encontrado: " + usuario.getNomeCompleto());

            // Buscar anamnese existente
            Anamnese anamnese = anamneseRepository.findByUsuarioId(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Anamnese não encontrada para este usuário. Use POST /anamnese para criar."));
            
            System.out.println("📝 Anamnese encontrada - ID: " + anamnese.getId());

            // Atualizar dados
            anamnese.setObjetivoPrincipal(dados.objetivoPrincipal());
            anamnese.setDiasPorSemana(dados.diasPorSemana());
            anamnese.setNivel(dados.nivel());
            anamnese.setTemRestricao(dados.temRestricao());
            
            anamnese = anamneseRepository.save(anamnese);
            System.out.println("✅ Anamnese atualizada!");

            // Verificar restrição
            if (dados.temRestricao()) {
                System.out.println("⚠️ Usuário tem restrição - retornando aviso");
                return new DadosPlanoTreino(
                    "AVISO",
                    "Seu formulário foi atualizado, mas por ter uma restrição, pedimos que procure um profissional da academia para montar seu treino."
                );
            }

            // Gerar novo plano com Gemini
            System.out.println("🤖 Chamando Gemini para gerar novo plano...");
            String planoGerado = planoTreinoService.gerarPlanoTreino(dados);
            System.out.println("✅ Novo plano gerado com sucesso!");

            return new DadosPlanoTreino("PLANO_TREINO", planoGerado);

        } catch (Exception e) {
            System.out.println("❌ EXCEÇÃO CAPTURADA:");
            System.out.println("   Mensagem: " + e.getMessage());
            System.out.println("   Tipo: " + e.getClass().getName());
            e.printStackTrace();
            throw e;
        }
    }
}