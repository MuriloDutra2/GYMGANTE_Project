document.addEventListener('DOMContentLoaded', () => {

    const formLogin = document.getElementById('form-login');

    formLogin.addEventListener('submit', async (evento) => {
        evento.preventDefault();
        console.log("🔐 Formulário de login interceptado...");

        // 1. Capturar dados
        const formData = new FormData(formLogin);
        const dadosLogin = {
            loginIdentifier: formData.get('loginIdentifier'),
            senha: formData.get('senha')
        };

        console.log("📤 Enviando login:", { loginIdentifier: dadosLogin.loginIdentifier, senha: '***' });

        try {
            // 2. Enviar requisição com timeout
            const response = await fetchWithTimeout(
                `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.USUARIOS.LOGIN}`,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(dadosLogin)
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.mensagem || 'Credenciais inválidas');
            }

            // 3. Sucesso!
            console.log('✅ Login realizado:', data);
            showToast('Login realizado com sucesso!', 'success', 2000);

            // 4. Armazenar userId e dados do usuário
            localStorage.setItem('userId', data.id);
            localStorage.setItem('usuarioLogado', JSON.stringify(data));

            // 5. Mostrar loading e verificar se tem treino
            mostrarLoading();
            await verificarETreino(data.id);

        } catch (error) {
            console.error('❌ Erro ao fazer login:', error);
            esconderLoading();
            showToast(error.message || 'Falha no login', 'error');
        }
    });

    function mostrarLoading() {
        const loadingOverlay = document.getElementById('loading-overlay');
        if (loadingOverlay) {
            loadingOverlay.classList.remove('hidden');
        }
    }

    function esconderLoading() {
        const loadingOverlay = document.getElementById('loading-overlay');
        if (loadingOverlay) {
            loadingOverlay.classList.add('hidden');
        }
    }

    async function verificarETreino(userId) {
        try {
            console.log('🔍 Verificando se usuário tem treino...');
            const response = await fetchWithTimeout(
                `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.ANAMNESE}/${userId}`
            );
            
            if (response.status === 404) {
                // Usuário não tem anamnese/treino
                console.log('ℹ️ Usuário não tem treino. Redirecionando para anamnese...');
                esconderLoading();
                window.location.href = 'anamnese.html';
                return;
            }

            if (!response.ok) {
                throw new Error(`Erro ao buscar treino: ${response.status}`);
            }

            const data = await response.json();
            console.log('✅ Treino encontrado:', data);

            // Salvar dados da anamnese e treino no localStorage
            localStorage.setItem('anamneseData', JSON.stringify({
                anamneseId: data.anamneseId,
                objetivoPrincipal: data.objetivoPrincipal,
                diasPorSemana: data.diasPorSemana,
                nivel: data.nivel,
                temRestricao: data.temRestricao
            }));
            
            localStorage.setItem('treinoData', JSON.stringify({
                tipo: data.tipo,
                treino: data.treino
            }));

            // Redirecionar para a página de treino
            esconderLoading();
            window.location.href = 'treino.html';

        } catch (error) {
            console.error('❌ Erro ao verificar treino:', error);
            esconderLoading();
            // Em caso de erro, redireciona para anamnese para criar o treino
            showToast('Não foi possível carregar seu treino. Redirecionando...', 'error', 2000);
            setTimeout(() => {
                window.location.href = 'anamnese.html';
            }, 2000);
        }
    }
});
