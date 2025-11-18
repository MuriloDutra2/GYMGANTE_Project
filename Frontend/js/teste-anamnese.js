console.log("Rodando Teste Final (TREINO) com ID: 8...");
fetch('http://localhost:8080/api/anamnese', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
        usuarioId: 8, 
        objetivoPrincipal: "Ganho de Massa Magra", 
        diasPorSemana: "3x por semana", 
        nivel: "Iniciante", 
        temRestricao: false
    })
})
.then(res => res.ok ? res.json() : res.json().then(err => Promise.reject(err)))
.then(data => console.log('>>> SUCESSO FINAL (TREINO):', data))
.catch(err => console.error('>>> ERRO FINAL (INESPERADO):', err));