document.addEventListener('DOMContentLoaded', () => {

    // 1. Verificar se o usuário está logado
    const usuarioLogado = JSON.parse(localStorage.getItem('usuarioLogado'));
    
    if (!usuarioLogado) {
        alert('⚠️ Você precisa fazer login primeiro!');
        window.location.href = 'login.html';
        return;
    }

    console.log('👤 Usuário logado:', usuarioLogado);

    // 2. Exibir nome do usuário na página (opcional)
    const nomeUsuarioElement = document.getElementById('nome-usuario');
    if (nomeUsuarioElement) {
        nomeUsuarioElement.textContent = usuarioLogado.nomeCompleto;
    }

    // 3. Capturar o formulário de anamnese
    const formAnamnese = document.getElementById('form-anamnese');

    formAnamnese.addEventListener('submit', async (evento) => {
        evento.preventDefault();
        console.log("📋 Formulário de anamnese interceptado...");

        // 4. Capturar dados do formulário
        const formData = new FormData(formAnamnese);
        
        const dadosAnamnese = {
            usuarioId: usuarioLogado.id, // ⭐ ID do usuário logado
            objetivoPrincipal: formData.get('objetivo_principal'),
            diasPorSemana: formData.get('dias_por_semana'),
            nivel: formData.get('nivel'),
            temRestricao: formData.get('tem_restricao') === 'true' // Converte string para boolean
        };

        console.log("📤 Dados da anamnese:", dadosAnamnese);

        try {
            // 5. Enviar para o back-end
            const response = await fetch('http://localhost:8080/api/anamnese', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(dadosAnamnese)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.mensagem || 'Erro ao processar anamnese');
            }

            // 6. Processar resposta
            console.log('✅ Resposta da anamnese:', data);

            // ⭐ IMPORTANTE: AnamneseResponseDto tem 2 campos:
            // - tipo: "AVISO" ou "TREINO"
            // - conteudo: Mensagem de aviso OU JSON do treino

            if (data.tipo === 'AVISO') {
                // Mostrar aviso
                alert(`⚠️ ${data.conteudo}`);
                // Opcional: Redirecionar para página de contato
                
            } else if (data.tipo === 'TREINO') {
                // Mostrar treino
                console.log('🏋️ Treino recebido:', data.conteudo);
                
                // Parseiar o JSON do treino
                const treino = JSON.parse(data.conteudo);
                
                // Armazenar o treino
                localStorage.setItem('treinoAtual', JSON.stringify(treino));
                
                alert('✅ Seu treino está pronto!');
                
                // Redirecionar para página de visualização do treino
                window.location.href = 'treino.html';
            }

        } catch (error) {
            console.error('❌ Erro ao processar anamnese:', error);
            alert(`❌ Erro:\n\n${error.message}`);
        }
    });
});