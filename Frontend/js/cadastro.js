document.addEventListener('DOMContentLoaded', () => {

    const formCadastro = document.getElementById('form-cadastro');

    formCadastro.addEventListener('submit', async (evento) => {
        evento.preventDefault();
        console.log("📝 Formulário interceptado. Capturando dados...");

        // 1. Capturar dados do formulário
        const formData = new FormData(formCadastro);
        const dadosBrutos = Object.fromEntries(formData);

        // 2. Mapear para o formato do DTO Java (camelCase)
        const dadosParaApi = {
            nomeCompleto: dadosBrutos.nome_completo,
            email: dadosBrutos.email,
            senha: dadosBrutos.senha,
            cpf: dadosBrutos.cpf,
            dataNascimento: dadosBrutos.data_nascimento,
            telefone: dadosBrutos.telefone
        };

        console.log("📤 Enviando para o back-end:", dadosParaApi);

        try {
            // 3. Enviar requisição HTTP POST
            const response = await fetch('http://localhost:8080/api/usuarios/cadastro', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(dadosParaApi)
            });

            // 4. Processar resposta
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.mensagem || 'Erro ao cadastrar usuário');
            }

            // 5. Sucesso!
            console.log('✅ Resposta do servidor:', data);
            alert(`✅ Cadastro realizado com sucesso!\n\nID: ${data.id}\nNome: ${data.nomeCompleto}\nEmail: ${data.email}`);
            
            // Opcional: Redirecionar para login
            window.location.href = 'login.html';

        } catch (error) {
            console.error('❌ Erro ao cadastrar:', error);
            alert(`❌ Erro ao realizar o cadastro:\n\n${error.message}`);
        }
    });
});