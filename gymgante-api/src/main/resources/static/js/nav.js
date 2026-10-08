// Navegação comum: "Sair" limpa a sessão local e volta ao login.
// "Voltar" leva ao treino se o usuário já tiver um; senão, à página inicial.
document.addEventListener('DOMContentLoaded', () => {
    const sair = document.getElementById('link-sair');
    if (sair) {
        sair.addEventListener('click', (e) => {
            e.preventDefault();
            ['userId', 'usuarioLogado', 'anamneseData', 'treinoData'].forEach((k) => localStorage.removeItem(k));
            window.location.href = 'login.html';
        });
    }

    const voltar = document.getElementById('link-voltar');
    if (voltar) {
        let temTreino = false;
        try {
            temTreino = JSON.parse(localStorage.getItem('treinoData') || '{}').tipo === 'PLANO_TREINO';
        } catch (_) { /* ignora */ }
        voltar.setAttribute('href', temTreino ? 'treino.html' : 'index.html');
    }
});
