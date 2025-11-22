document.addEventListener('DOMContentLoaded', () => {
    // Redirecionamento automático após login
    const userId = localStorage.getItem('userId');

    if (!userId) {
        window.location.href = '/login.html';
        return;
    }

    // Verificar automaticamente se tem treino
    verificarTreinoExistente();
});

async function verificarTreinoExistente() {
    const userId = localStorage.getItem('userId');
    const loading = document.getElementById('loading');

    loading.style.display = 'flex';

    try {
        const response = await fetch(`http://localhost:8080/anamnese/${userId}`);
        
        if (response.ok) {
            // Usuário TEM treino - Redirecionar para treino
            window.location.href = '/treino.html';
        } else if (response.status === 404) {
            // Usuário NÃO TEM treino - Redirecionar para anamnese
            window.location.href = '/anamnese.html';
        } else {
            throw new Error('Erro na verificação');
        }

    } catch (error) {
        console.error('Erro na verificação:', error);
        window.location.href = '/anamnese.html';
    } finally {
        loading.style.display = 'none';
    }
}
