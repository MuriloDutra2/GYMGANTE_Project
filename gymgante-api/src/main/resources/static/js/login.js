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
            // 2. Enviar requisição
            // ✅ CORREÇÃO 1: Usando API_CONFIG no Login
            const urlLogin = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.USUARIOS.LOGIN}`;
            
            const response = await fetch(urlLogin, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(dadosLogin)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.mensagem || 'Credenciais inválidas');
            }

            // 3. Sucesso!
            console.log('✅ Login realizado:', data);

            // 4. Armazenar userId e dados do usuário
            localStorage.setItem('userId', data.id);
            localStorage.setItem('usuarioLogado', JSON.stringify(data));

            // 5. Verificar se tem treino e redirecionar
            await verificarETreino(data.id);

        } catch (error) {
            console.error('❌ Erro ao fazer login:', error);
            alert(`❌ Falha no login:\n\n${error.message}`);
        }
    });

    async function verificarETreino(userId) {
        try {
            // ✅ CORREÇÃO 2: Usando API_CONFIG na verificação de treino
            const urlCheck = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.ANAMNESE}/${userId}`;
            const response = await fetch(urlCheck);

            if (response.status === 404) {
                // Usuário ainda não tem treino
                window.location.href = 'anamnese.html';
                return;
            }

            if (!response.ok) {
                throw new Error('Erro desconhecido na verificação');
            }

            // Usuário tem treino
            const data = await response.json();
            localStorage.setItem('anamneseData', JSON.stringify({
                objetivoPrincipal: data.objetivoPrincipal,
                diasPorSemana: data.diasPorSemana,
                nivel: data.nivel,
                temRestricao: data.temRestricao
            }));
            localStorage.setItem('treinoData', JSON.stringify({ tipo: data.tipo, treino: data.treino }));
            window.location.href = 'dashboard.html';
        } catch (error) {
            console.error('❌ Erro ao verificar treino:', error);
                        window.location.href = 'anamnese.html';
        }
    }
});