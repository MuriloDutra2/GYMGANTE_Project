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
            const response = await fetch('http://localhost:8080/api/usuarios/login', {
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
            // Usa a configuração relativa (vazia) que definimos no config.js
const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.ANAMNESE}/${userId}`);
            const data = await response.json();

            if (response.ok) {
                // Usuário tem treino
                localStorage.setItem('treinoData', JSON.stringify(data));
                window.location.href = 'treino.html';
            } else if (response.status === 404) {
                // Usuário não tem treino
                window.location.href = 'anamnese.html';
            } else {
                throw new Error('Erro desconhecido na verificação');
            }
        } catch (error) {
            console.error('❌ Erro ao verificar treino:', error);
            alert('Erro ao verificar treino. Tentando redirecionar para formulário.');
            window.location.href = 'anamnese.html';
        }
    }
});
