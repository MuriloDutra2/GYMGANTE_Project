document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('form-anamnese');
    const loadingOverlay = document.getElementById('loading-overlay');
    const btnSubmit = document.getElementById('btn-submit');
    const userId = localStorage.getItem('userId');

    if (!userId) {
        alert('Usuário não logado. Redirecionando para login.');
        window.location.href = 'login.html';
        return;
    }

    const isUpdate = window.location.href.includes('?update=true');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // Validar campos
        const objetivo = document.getElementById('objetivo').value;
        const diasPorSemana = document.getElementById('diasPorSemana').value;
        const nivel = document.getElementById('nivel').value;

        if (!objetivo || !diasPorSemana || !nivel) {
            alert('Por favor, preencha todos os campos obrigatórios.');
            return;
        }

        // Mostrar loading
        loadingOverlay.classList.remove('hidden');
        btnSubmit.disabled = true;

        const payload = {
            usuarioId: parseInt(userId),
            objetivoPrincipal: objetivo,
            diasPorSemana: diasPorSemana,
            nivel: nivel,
            temRestricao: document.getElementById('temRestricao').checked
        };

        try {
            const url = isUpdate
                ? `http://localhost:8080/anamnese/${userId}`
                : 'http://localhost:8080/anamnese';

            const method = isUpdate ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (response.ok) {
                if (data.tipo === 'PLANO_TREINO') {
                    localStorage.setItem('treinoGerado', data.conteudo);
                    alert('Treino gerado com sucesso!');
                    window.location.href = 'treino.html';
                } else if (data.tipo === 'AVISO') {
                    alert(data.conteudo);
                    // Redirecionar de volta ou deixar na mesma página
                }
            } else if (response.status === 409) {
                alert(data.conteudo || 'Este usuário já possui um treino.');
            } else {
                throw new Error('Erro na geração do treino.');
            }

        } catch (error) {
            console.error('Erro:', error);
            alert('Erro ao gerar treino. Tente novamente.');
        } finally {
            loadingOverlay.classList.add('hidden');
            btnSubmit.disabled = false;
        }
    });
});
