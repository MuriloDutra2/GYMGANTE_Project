// Cole este código no Console do navegador (F12)
console.log("🧪 Testando endpoint de anamnese...");

// ⚠️ TROQUE O ID PELO ID DO USUÁRIO QUE VOCÊ CADASTROU
const USUARIO_ID = 1; // <-- AJUSTE AQUI!

// Teste SEM restrição (deve retornar TREINO)
fetch('http://localhost:8080/api/anamnese', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
        usuarioId: USUARIO_ID,
        objetivoPrincipal: "Ganho de Massa Magra",
        diasPorSemana: "5x por semana",
        nivel: "Intermediário",
        temRestricao: false
    })
})
.then(res => res.json())
.then(data => {
    console.log('✅ SUCESSO:', data);
    if (data.tipo === 'TREINO') {
        console.log('🏋️ Treino:', JSON.parse(data.conteudo));
    }
})
.catch(err => console.error('❌ ERRO:', err));