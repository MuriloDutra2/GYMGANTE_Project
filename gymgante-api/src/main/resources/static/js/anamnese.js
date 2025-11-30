document.addEventListener('DOMContentLoaded', async () => {
    const form = document.getElementById('form-anamnese');
    const loadingOverlay = document.getElementById('loading-overlay');
    const btnSubmit = document.getElementById('btn-submit');
    const userId = localStorage.getItem('userId');

    if (!userId) {
        showToast('Usuário não logado. Redirecionando...', 'error');
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);
        return;
    }

// Verificar se usuário já tem anamnese
    let isUpdate = false;
    try {
        
        const urlCheck = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.ANAMNESE}/${userId}`;
        const response = await fetch(urlCheck);
        if (response.ok) {
            isUpdate = true;
            console.log('ℹ️ Usuário já tem anamnese. Modo: ATUALIZAÇÃO');
            
            // Carregar dados existentes para preencher o formulário
            const data = await response.json();
            if (data.objetivoPrincipal) {
                document.getElementById('objetivo').value = data.objetivoPrincipal;
            }
            if (data.diasPorSemana) {
                document.getElementById('diasPorSemana').value = data.diasPorSemana;
            }
            if (data.nivel) {
                document.getElementById('nivel').value = data.nivel;
            }
            if (data.temRestricao) {
                document.getElementById('temRestricao').checked = data.temRestricao;
            }
        } else if (response.status === 404) {
            isUpdate = false;
            console.log('ℹ️ Usuário não tem anamnese. Modo: CRIAÇÃO');
        }
    } catch (error) {
        console.error('Erro ao verificar anamnese existente:', error);
        // Em caso de erro, assume que é criação
        isUpdate = false;
    }

    // Também verificar parâmetro da URL (caso venha do botão "Gerar Novo Treino")
    if (window.location.href.includes('?update=true')) {
        isUpdate = true;
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // Validação
        const objetivo = document.getElementById('objetivo').value;
        const diasPorSemana = document.getElementById('diasPorSemana').value;
        const nivel = document.getElementById('nivel').value;

        if (!objetivo || !diasPorSemana || !nivel) {
            showToast('Preencha todos os campos obrigatórios.', 'error');
            return;
        }

        // Mostrar loading e manter formulário visível (mas desabilitado)
        loadingOverlay.classList.remove('hidden');
        btnSubmit.disabled = true;
        form.style.opacity = '0.6';
        form.style.pointerEvents = 'none';
        
        // Atualizar mensagem do loading baseado no modo
        const loadingTitle = loadingOverlay.querySelector('h3');
        const loadingText = loadingOverlay.querySelector('p');
        if (loadingTitle) {
            loadingTitle.textContent = isUpdate ? '🔄 Atualizando seu treino...' : '🤖 Gerando seu treino personalizado...';
        }
        if (loadingText) {
            loadingText.textContent = isUpdate 
                ? 'Isso pode levar alguns segundos. Aguarde!' 
                : 'Isso pode levar até 15 segundos. Aguarde!';
        }

        const payload = {
            usuarioId: parseInt(userId),
            objetivoPrincipal: objetivo,
            diasPorSemana: diasPorSemana,
            nivel: nivel,
            temRestricao: document.getElementById('temRestricao').checked
        };

        try {
            const url = isUpdate 
                ? `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.ANAMNESE}/${userId}` 
                : `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.ANAMNESE}`;
            const method = isUpdate ? 'PUT' : 'POST';

            // Atualizar mensagem durante a requisição
            if (loadingText) {
                loadingText.textContent = 'Enviando dados e gerando treino com IA...';
            }

            const response = await fetchWithTimeout(url, {
                method: method,
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            }, 60000); // 60 segundos de timeout para geração de treino

            const data = await response.json();

            if (response.ok) {
                if (data.tipo === 'PLANO_TREINO') {
                    // Atualizar mensagem de sucesso
                    if (loadingTitle) {
                        loadingTitle.textContent = '✅ Treino gerado com sucesso!';
                    }
                    if (loadingText) {
                        loadingText.textContent = 'Redirecionando para visualizar seu treino...';
                    }
                    
                    // Salvar dados da anamnese e treino
                    localStorage.setItem('anamneseData', JSON.stringify({
                        objetivoPrincipal: payload.objetivoPrincipal,
                        diasPorSemana: payload.diasPorSemana,
                        nivel: payload.nivel,
                        temRestricao: payload.temRestricao
                    }));
                    localStorage.setItem('treinoData', JSON.stringify({
                        tipo: data.tipo,
                        treino: data.conteudo
                    }));
                    
                    showToast(
                        isUpdate ? 'Treino atualizado com sucesso!' : 'Treino gerado com sucesso!', 
                        'success',
                        2000
                    );
                    
                    // Aguardar um pouco antes de redirecionar para mostrar a mensagem
                    setTimeout(() => {
                        window.location.href = 'treino.html';
                    }, 1500);
                } else if (data.tipo === 'AVISO') {
                    loadingOverlay.classList.add('hidden');
                    form.style.opacity = '1';
                    form.style.pointerEvents = 'auto';
                    showToast(data.conteudo, 'warning', 5000);
                }
            } else if (response.status === 409) {
                // Se ainda assim der erro 409, forçar modo de atualização
                if (loadingTitle) {
                    loadingTitle.textContent = '🔄 Atualizando treino existente...';
                }
                if (loadingText) {
                    loadingText.textContent = 'Você já possui um treino. Atualizando...';
                }
                
                // Tentar novamente com PUT
                const updateResponse = await fetchWithTimeout(
                    `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.ANAMNESE}/${userId}`,
                    {
                        method: 'PUT',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify(payload)
                    },
                    60000
                );
                const updateData = await updateResponse.json();
                if (updateResponse.ok && updateData.tipo === 'PLANO_TREINO') {
                    if (loadingTitle) {
                        loadingTitle.textContent = '✅ Treino atualizado com sucesso!';
                    }
                    if (loadingText) {
                        loadingText.textContent = 'Redirecionando...';
                    }
                    
                    localStorage.setItem('anamneseData', JSON.stringify({
                        objetivoPrincipal: payload.objetivoPrincipal,
                        diasPorSemana: payload.diasPorSemana,
                        nivel: payload.nivel,
                        temRestricao: payload.temRestricao
                    }));
                    localStorage.setItem('treinoData', JSON.stringify({
                        tipo: updateData.tipo,
                        treino: updateData.conteudo
                    }));
                    
                    showToast('Treino atualizado com sucesso!', 'success', 2000);
                    setTimeout(() => {
                        window.location.href = 'treino.html';
                    }, 1500);
                } else {
                    throw new Error('Erro ao atualizar treino.');
                }
            } else {
                throw new Error(data.mensagem || 'Erro na geração do treino.');
            }
        } catch (error) {
            console.error('Erro:', error);
            loadingOverlay.classList.add('hidden');
            form.style.opacity = '1';
            form.style.pointerEvents = 'auto';
            
            let mensagemErro = 'Erro ao gerar treino.';
            if (error.message.includes('timeout') || error.message.includes('demorou muito')) {
                mensagemErro = 'A requisição demorou muito. Tente novamente.';
            } else if (error.message.includes('conexão') || error.message.includes('network')) {
                mensagemErro = 'Erro de conexão. Verifique sua internet e tente novamente.';
            } else {
                mensagemErro = error.message || 'Erro ao gerar treino. Verifique a conexão com a API de IA.';
            }
            
            showToast(mensagemErro, 'error', 5000);
        } finally {
            btnSubmit.disabled = false;
        }
    });
});
