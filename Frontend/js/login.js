document.addEventListener('DOMContentLoaded', () => {

    const formLogin = document.getElementById('form-login');

    formLogin.addEventListener('submit', (evento) => {
        // 1. Previne o recarregamento da página
        evento.preventDefault();
        console.log("Formulário de login interceptado. Capturando dados...");

        // 2. Captura os dados do formulário
        const formData = new FormData(formLogin);
        const dadosUsuario = {};
        formData.forEach((valor, chave) => {
            dadosUsuario[chave] = valor;
        });
        
        // (Aqui não precisamos de mapeamento extra, pois os nomes
        // 'loginIdentifier' e 'senha' já batem com o DTO Java)
        console.log("Dados a serem enviados (JSON):", dadosUsuario);

        // 3. --- INÍCIO DA INTEGRAÇÃO (LOGIN) ---
        fetch('http://localhost:8080/api/usuarios/login', { 
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(dadosUsuario) // Enviamos o objeto direto
        })
        .then(response => {
            // Se a resposta NÃO for "OK" (ex: 404, 500)
            if (!response.ok) {
                // Nosso back-end (Service) manda "Credenciais inválidas"
                // e o Spring gera um erro 500. Vamos pegar a mensagem.
                // Usamos .text() porque o erro 500 pode não ser um JSON.
                return response.json().then(err => {
                    throw new Error('Falha no login: ' + (err.message || 'Credenciais inválidas'));
                }).catch(() => {
                    throw new Error('Falha no login: Credenciais inválidas.');
                });
            }
            // Se a resposta for "OK" (200)
            return response.json(); 
        })
        .then(data => {
            // 'data' é o {id: 1, nomeCompleto: "...", ...}
            console.log('Resposta do servidor (Sucesso!):', data);
            alert('Login realizado com sucesso! Bem-vindo, ' + data.nomeCompleto);
            
            // Opcional: Redirecionar para a página principal do site
            // window.location.href = 'dashboard.html';
        })
        .catch(error => {
            // Pega qualquer erro de rede ou o erro que jogamos (throw)
            console.error('Erro ao logar:', error);
            alert(error.message); // Mostra o alerta (ex: "Falha no login: Credenciais inválidas")
        });
        // --- FIM DA INTEGRAÇÃO ---
    });
});