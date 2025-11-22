document.addEventListener('DOMContentLoaded', () => {
    const formCadastro = document.getElementById('form-cadastro');

    // Aplicar máscaras nos inputs
    const cpfInput = document.getElementById('cpf');
    const telefoneInput = document.getElementById('telefone');

    if (cpfInput) {
        cpfInput.addEventListener('input', (e) => {
            e.target.value = maskCPF(e.target.value);
        });
    }

    if (telefoneInput) {
        telefoneInput.addEventListener('input', (e) => {
            e.target.value = maskPhone(e.target.value);
        });
    }

    // Validação em tempo real
    const emailInput = document.getElementById('email');
    const senhaInput = document.getElementById('senha');

    if (emailInput) {
        emailInput.addEventListener('blur', (e) => {
            if (e.target.value && !isValidEmail(e.target.value)) {
                e.target.style.borderColor = '#ef4444';
                showToast('Email inválido', 'error', 2000);
            } else {
                e.target.style.borderColor = '';
            }
        });
    }

    if (senhaInput) {
        senhaInput.addEventListener('blur', (e) => {
            if (e.target.value && !isValidPassword(e.target.value)) {
                e.target.style.borderColor = '#ef4444';
                showToast('Senha deve ter no mínimo 8 caracteres', 'error', 2000);
            } else {
                e.target.style.borderColor = '';
            }
        });
    }

    formCadastro.addEventListener('submit', async (evento) => {
        evento.preventDefault();
        console.log("📝 Formulário interceptado. Capturando dados...");

        // Validações antes de enviar
        const email = document.getElementById('email').value;
        const senha = document.getElementById('senha').value;
        const cpf = document.getElementById('cpf').value.replace(/\D/g, '');

        if (!isValidEmail(email)) {
            showToast('Por favor, insira um email válido', 'error');
            return;
        }

        if (!isValidPassword(senha)) {
            showToast('A senha deve ter no mínimo 8 caracteres', 'error');
            return;
        }

        if (!isValidCPFFormat(cpf)) {
            showToast('Por favor, insira um CPF válido (11 dígitos)', 'error');
            return;
        }

        // 1. Capturar dados do formulário
        const formData = new FormData(formCadastro);
        const dadosBrutos = Object.fromEntries(formData);

        // 2. Mapear para o formato do DTO Java (camelCase)
        const dadosParaApi = {
            nomeCompleto: dadosBrutos.nome_completo,
            email: dadosBrutos.email,
            senha: dadosBrutos.senha,
            cpf: cpf, // CPF sem formatação
            dataNascimento: dadosBrutos.data_nascimento,
            telefone: dadosBrutos.telefone.replace(/\D/g, '') // Telefone sem formatação
        };

        console.log("📤 Enviando para o back-end:", { ...dadosParaApi, senha: '***' });

        showLoading('Cadastrando usuário...');

        try {
            // 3. Enviar requisição HTTP POST com timeout
            const response = await fetchWithTimeout(
                `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.USUARIOS.CADASTRO}`,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(dadosParaApi)
                }
            );

            // 4. Processar resposta
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.mensagem || 'Erro ao cadastrar usuário');
            }

            // 5. Sucesso!
            console.log('✅ Resposta do servidor:', data);
            showToast('Cadastro realizado com sucesso!', 'success', 3000);
            
            // Redirecionar para login após 1.5 segundos
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 1500);

        } catch (error) {
            console.error('❌ Erro ao cadastrar:', error);
            showToast(error.message || 'Erro ao realizar o cadastro', 'error');
        } finally {
            hideLoading();
        }
    });
});