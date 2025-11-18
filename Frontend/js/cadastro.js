document.addEventListener('DOMContentLoaded', () => {

    const formCadastro = document.getElementById('form-cadastro');

    formCadastro.addEventListener('submit', (evento) => {
        // 1. Previne o recarregamento da página
        evento.preventDefault();
        console.log("Formulário interceptado. Capturando dados...");

        // 2. Captura os dados brutos do formulário
        const formData = new FormData(formCadastro);
        const dadosBrutos = {};
        formData.forEach((valor, chave) => {
            dadosBrutos[chave] = valor;
        });

        // 3. Mapeia os dados para o formato exato do DTO Java (camelCase)
        // O HTML usa 'nome_completo', mas o Java (record) espera 'nomeCompleto'.
        const dadosParaApi = {
            nomeCompleto: dadosBrutos.nome_completo,
            email: dadosBrutos.email,
            senha: dadosBrutos.senha,
            cpf: dadosBrutos.cpf,
            dataNascimento: dadosBrutos.data_nascimento,
            telefone: dadosBrutos.telefone
        };

        console.log("Dados a serem enviados (JSON):", dadosParaApi);

        // 4. --- INÍCIO DA INTEGRAÇÃO (AGORA ATIVO!) ---
        // Usamos a API Fetch para enviar os dados para o nosso back-end
        fetch('http://localhost:8080/api/usuarios/cadastro', {
            method: 'POST',
            headers: {
                // Informa ao back-end que estamos enviando dados em formato JSON
                'Content-Type': 'application/json',
            },
            // Converte nosso objeto JavaScript em uma string JSON
            body: JSON.stringify(dadosParaApi) 
        })
        .then(response => {
            // .then() é o que acontece quando o servidor responde
            
            // Se a resposta NÃO for "OK" (ex: erro 400 ou 500)
            if (!response.ok) {
                // Lemos a mensagem de erro que o back-end enviou
                return response.text().then(text => { 
                    throw new Error('Falha no cadastro: ' + text); 
                });
            }
            // Se a resposta for "OK", convertemos o JSON da resposta
            return response.json(); 
        })
        .then(data => {
            // Este é o "data" que o servidor enviou de volta (o usuário salvo)
            console.log('Resposta do servidor (Sucesso!):', data);
            alert('Cadastro realizado com sucesso! Seu novo ID é: ' + data.id);
            
            // Opcional: Redirecionar para a página de login após o sucesso
            // window.location.href = 'login.html';
        })
        .catch(error => {
            // .catch() é o que acontece se a rede falhar ou se o servidor der erro
            console.error('Erro ao cadastrar:', error);
            // Mostra o erro exato no alerta (ex: "CPF já existe")
            alert('Erro ao realizar o cadastro. Motivo: ' + error.message);
        });
        // --- FIM DA INTEGRAÇÃO ---
    });
});