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
            alert(`✅ Bem-vindo(a), ${data.nomeCompleto}!`);

            // 4. Armazenar usuário
            localStorage.setItem('usuarioLogado', JSON.stringify(data));

            // 5. Redirecionar (ajuste conforme sua estrutura)
            // window.location.href = 'anamnese.html';

        } catch (error) {
            console.error('❌ Erro ao fazer login:', error);
            alert(`❌ Falha no login:\n\n${error.message}`);
        }
    });
});