// ===== Painel do aluno =====
// Mostra o treino do dia (conforme o dia da semana), permite marcar exercícios como feitos
// e resume a semana, a sequência e o histórico.

const SIGLAS = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
const NOMES_DIAS = ['Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'];

// Em quais dias da semana (0 = domingo) cada quantidade de treinos é distribuída.
const PADROES = {
    1: [3],
    2: [2, 5],
    3: [1, 3, 5],
    4: [1, 2, 4, 5],
    5: [1, 2, 3, 4, 5],
    6: [1, 2, 3, 4, 5, 6],
    7: [0, 1, 2, 3, 4, 5, 6]
};

const estado = {
    userId: null,
    anamnese: null,
    plano: null,          // { titulo, dias: [...] } ou null
    conclusoes: new Map() // 'YYYY-MM-DD' -> Set('dia|exercicio')
};

// ---------- utilidades ----------
function esc(texto) {
    const d = document.createElement('div');
    d.textContent = texto == null ? '' : String(texto);
    return d.innerHTML;
}

function fmtData(d) {
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const dia = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${m}-${dia}`;
}

function somarDias(d, n) {
    const r = new Date(d.getFullYear(), d.getMonth(), d.getDate() + n);
    return r;
}

function chave(dia, exercicio) {
    return `${dia.nome}|${exercicio.nome}`;
}

function parsePlano(texto) {
    if (!texto) return null;
    try {
        const ini = texto.indexOf('{');
        const fim = texto.lastIndexOf('}');
        if (ini === -1 || fim <= ini) return null;
        const obj = JSON.parse(texto.substring(ini, fim + 1));
        if (!obj || !Array.isArray(obj.dias)) return null;
        const dias = obj.dias
            .filter((d) => d && Array.isArray(d.exercicios) && d.exercicios.some((e) => e && e.nome))
            .map((d, i) => ({
                ...d,
                nome: d.nome || `Treino ${i + 1}`,
                exercicios: d.exercicios.filter((e) => e && e.nome)
            }))
            .slice(0, 7);
        if (dias.length === 0) return null;
        return { titulo: obj.titulo || 'Seu plano', dias };
    } catch (_) {
        return null;
    }
}

// Qual dia do plano cai em determinada data (ou null se for descanso).
function treinoDoDia(data) {
    const dias = estado.plano.dias;
    const padrao = PADROES[dias.length];
    const idx = padrao.indexOf(data.getDay());
    return idx >= 0 ? dias[idx] : null;
}

function feitosNoDia(dataStr, dia) {
    const set = estado.conclusoes.get(dataStr);
    if (!set) return 0;
    return dia.exercicios.filter((e) => set.has(chave(dia, e))).length;
}

// ---------- dados ----------
async function carregar() {
    const carregando = document.getElementById('carregando');
    const erro = document.getElementById('erro');
    const conteudo = document.getElementById('conteudo');
    carregando.classList.remove('hidden');
    erro.classList.add('hidden');
    conteudo.classList.add('hidden');

    try {
        const resp = await fetchWithTimeout(
            `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.ANAMNESE}/${estado.userId}`);

        if (resp.status === 404) {
            window.location.href = 'anamnese.html';
            return;
        }
        if (!resp.ok) {
            throw new Error(resp.status === 503
                ? 'O serviço de IA está indisponível no momento. Tente novamente em instantes.'
                : 'Erro ao buscar seu treino.');
        }

        estado.anamnese = await resp.json();
        estado.plano = estado.anamnese.tipo === 'PLANO_TREINO' ? parsePlano(estado.anamnese.treino) : null;

        // Mantém o cache usado pela página "Plano completo"
        localStorage.setItem('anamneseData', JSON.stringify({
            objetivoPrincipal: estado.anamnese.objetivoPrincipal,
            diasPorSemana: estado.anamnese.diasPorSemana,
            nivel: estado.anamnese.nivel,
            temRestricao: estado.anamnese.temRestricao
        }));
        localStorage.setItem('treinoData', JSON.stringify({
            tipo: estado.anamnese.tipo, treino: estado.anamnese.treino
        }));

        const progresso = await fetchWithTimeout(
            `${API_CONFIG.BASE_URL}/api/progresso/${estado.userId}?dias=60`);
        if (!progresso.ok) throw new Error('Erro ao buscar seu progresso.');
        const lista = await progresso.json();

        estado.conclusoes = new Map();
        lista.forEach((c) => {
            if (!estado.conclusoes.has(c.data)) estado.conclusoes.set(c.data, new Set());
            estado.conclusoes.get(c.data).add(`${c.diaTreino}|${c.exercicio}`);
        });

        renderizar();
        carregando.classList.add('hidden');
        conteudo.classList.remove('hidden');
    } catch (e) {
        console.error('Erro ao carregar painel:', e);
        carregando.classList.add('hidden');
        document.getElementById('erro-msg').textContent = e.message || 'Tente novamente.';
        erro.classList.remove('hidden');
    }
}

async function alternarExercicio(dia, exercicio, dataStr) {
    const set = estado.conclusoes.get(dataStr) || new Set();
    const k = chave(dia, exercicio);
    const concluir = !set.has(k);

    // Atualiza na hora; desfaz se a API falhar
    if (concluir) set.add(k); else set.delete(k);
    estado.conclusoes.set(dataStr, set);
    renderizar();

    try {
        const resp = await fetchWithTimeout(`${API_CONFIG.BASE_URL}/api/progresso/${estado.userId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ data: dataStr, diaTreino: dia.nome, exercicio: exercicio.nome, concluido: concluir })
        });
        if (!resp.ok) throw new Error('falha ao salvar');
    } catch (e) {
        if (concluir) set.delete(k); else set.add(k);
        renderizar();
        showToast('Não foi possível salvar. Tente novamente.', 'error');
    }
}

// ---------- renderização ----------
function renderizar() {
    const hoje = new Date();
    const nome = (JSON.parse(localStorage.getItem('usuarioLogado') || '{}').nomeCompleto || '').split(' ')[0];

    document.getElementById('saudacao').textContent = nome ? `Olá, ${nome}!` : 'Olá!';
    const dataTexto = hoje.toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' });
    document.getElementById('data-hoje').textContent = dataTexto.charAt(0).toUpperCase() + dataTexto.slice(1);

    renderResumo();
    renderHoje(hoje);

    const temPlano = !!estado.plano;
    document.getElementById('estatisticas').classList.toggle('hidden', !temPlano);
    document.getElementById('semana').closest('section').classList.toggle('hidden', !temPlano);
    document.getElementById('historico').closest('section').classList.toggle('hidden', !temPlano);
    if (temPlano) {
        renderEstatisticas(hoje);
        renderSemana(hoje);
        renderHistorico(hoje);
    }
}

function renderResumo() {
    const a = estado.anamnese;
    const itens = [
        ['Objetivo', a.objetivoPrincipal],
        ['Frequência', a.diasPorSemana],
        ['Nível', a.nivel]
    ];
    document.getElementById('resumo').innerHTML = itens
        .map(([r, v]) => `<div class="item"><div class="rotulo">${r}</div><div class="valor">${esc(v)}</div></div>`)
        .join('');
}

function renderHoje(hoje) {
    const cartao = document.getElementById('hoje');
    cartao.classList.remove('aviso');

    // Restrição médica: sem treino automático
    if (estado.anamnese.tipo === 'AVISO') {
        cartao.classList.add('aviso');
        cartao.innerHTML = `<h2>⚠️ Aviso importante</h2><p>${esc(estado.anamnese.treino)}</p>
            <p style="margin-top:12px"><a class="btn" style="text-decoration:none;display:inline-block" href="anamnese.html?update=true">Atualizar anamnese</a></p>`;
        return;
    }

    if (!estado.plano) {
        cartao.innerHTML = `<h2>Treino de hoje</h2>
            <p>Não conseguimos interpretar o formato do seu plano. Veja-o completo na página do plano ou gere um novo.</p>
            <p style="margin-top:12px"><a class="btn" style="text-decoration:none;display:inline-block" href="treino.html">Ver plano completo</a></p>`;
        return;
    }

    const dataStr = fmtData(hoje);
    const dia = treinoDoDia(hoje);

    if (!dia) {
        cartao.innerHTML = `<div class="descanso"><div class="emoji">😴</div><h2>Hoje é dia de descanso</h2>
            <p>${esc(proximoTreinoTexto(hoje))}</p></div>`;
        return;
    }

    const total = dia.exercicios.length;
    const feitos = feitosNoDia(dataStr, dia);
    const pct = Math.round((feitos / total) * 100);

    const lista = dia.exercicios.map((e, i) => {
        const feito = (estado.conclusoes.get(dataStr) || new Set()).has(chave(dia, e));
        const detalhes = [
            e.series ? `Séries: ${esc(e.series)}` : '',
            e.repeticoes ? `Reps: ${esc(e.repeticoes)}` : '',
            e.descanso ? `Descanso: ${esc(e.descanso)}` : ''
        ].filter(Boolean).join(' · ');
        return `<li class="exercicio ${feito ? 'feito' : ''}" data-i="${i}" role="checkbox" aria-checked="${feito}" tabindex="0">
            <span class="caixa">✓</span>
            <div><div class="nome">${esc(e.nome)}</div>
            ${detalhes ? `<div class="detalhes">${detalhes}</div>` : ''}
            ${e.observacoes ? `<div class="obs">💡 ${esc(e.observacoes)}</div>` : ''}</div></li>`;
    }).join('');

    cartao.innerHTML = `
        <div class="hoje-topo">
            <div><h2>Treino de hoje: ${esc(dia.nome)}</h2></div>
            ${dia.grupoMuscular ? `<span class="selo">${esc(dia.grupoMuscular)}</span>` : ''}
        </div>
        <div class="barra"><div style="width:${pct}%"></div></div>
        <div class="barra-legenda">${feitos} de ${total} exercícios concluídos (${pct}%)</div>
        <ul class="exercicios">${lista}</ul>
        ${feitos === total ? '<div class="parabens">🎉 Treino de hoje concluído! Bom descanso.</div>' : ''}`;

    cartao.querySelectorAll('.exercicio').forEach((li) => {
        const acionar = () => alternarExercicio(dia, dia.exercicios[Number(li.dataset.i)], dataStr);
        li.addEventListener('click', acionar);
        li.addEventListener('keydown', (ev) => {
            if (ev.key === ' ' || ev.key === 'Enter') { ev.preventDefault(); acionar(); }
        });
    });
}

function proximoTreinoTexto(hoje) {
    for (let i = 1; i <= 7; i++) {
        const d = somarDias(hoje, i);
        const dia = treinoDoDia(d);
        if (dia) {
            const quando = i === 1 ? 'amanhã' : `na ${NOMES_DIAS[d.getDay()].toLowerCase()}`;
            return `Próximo treino ${quando}: ${dia.nome}${dia.grupoMuscular ? ` (${dia.grupoMuscular})` : ''}.`;
        }
    }
    return 'Aproveite para recuperar.';
}

// Segunda-feira da semana de `hoje`
function inicioDaSemana(hoje) {
    const diff = (hoje.getDay() + 6) % 7;
    return somarDias(hoje, -diff);
}

function estadoDoDia(data, hoje) {
    const dataStr = fmtData(data);
    // Antes de o plano existir não há treino nem falta
    if (estado.anamnese.planoDesde && dataStr < estado.anamnese.planoDesde) {
        return { dia: null, classe: 'descanso-dia', icone: '', antes: true };
    }
    const dia = treinoDoDia(data);
    if (!dia) return { dia: null, classe: 'descanso-dia', icone: '' };

    const feitos = feitosNoDia(dataStr, dia);
    const total = dia.exercicios.length;
    const ehHoje = dataStr === fmtData(hoje);
    const futuro = data > hoje && !ehHoje;

    if (feitos >= total) return { dia, classe: 'completo', icone: '✅', feitos, total };
    if (futuro) return { dia, classe: '', icone: '⏳', feitos, total };
    if (feitos > 0) return { dia, classe: 'parcial', icone: '🟡', feitos, total };
    if (ehHoje) return { dia, classe: '', icone: '⏳', feitos, total };
    return { dia, classe: 'perdido', icone: '❌', feitos, total };
}

function renderSemana(hoje) {
    const inicio = inicioDaSemana(hoje);
    const hojeStr = fmtData(hoje);
    let html = '';
    for (let i = 0; i < 7; i++) {
        const d = somarDias(inicio, i);
        const e = estadoDoDia(d, hoje);
        const classes = ['dia', e.classe, fmtData(d) === hojeStr ? 'hoje' : ''].filter(Boolean).join(' ');
        html += `<div class="${classes}">
            <div class="sigla">${SIGLAS[d.getDay()]}</div>
            <div class="num">${d.getDate()}</div>
            <div class="treino">${e.dia ? esc(e.dia.nome) : (e.antes ? '—' : 'Descanso')}</div>
            <div class="estado">${e.icone || '&nbsp;'}</div></div>`;
    }
    document.getElementById('semana').innerHTML = html;
}

function renderEstatisticas(hoje) {
    // Treinos da semana
    const inicio = inicioDaSemana(hoje);
    let planejados = 0;
    let completos = 0;
    for (let i = 0; i < 7; i++) {
        const e = estadoDoDia(somarDias(inicio, i), hoje);
        if (e.dia) {
            planejados++;
            if (e.classe === 'completo') completos++;
        }
    }

    // Sequência de treinos completos (dias de descanso não quebram)
    let sequencia = 0;
    for (let i = 0; i < 60; i++) {
        const d = somarDias(hoje, -i);
        const e = estadoDoDia(d, hoje);
        if (!e.dia) continue;
        if (e.classe === 'completo') sequencia++;
        else if (i === 0) continue; // hoje ainda pode ser concluído
        else break;
    }

    // Totais dos últimos 30 dias
    const limite = fmtData(somarDias(hoje, -30));
    let exercicios = 0;
    let treinosCompletos = 0;
    estado.conclusoes.forEach((set, dataStr) => {
        if (dataStr < limite) return;
        exercicios += set.size;
        const [a, m, dd] = dataStr.split('-').map(Number);
        const dia = treinoDoDia(new Date(a, m - 1, dd));
        if (dia && feitosNoDia(dataStr, dia) >= dia.exercicios.length) treinosCompletos++;
    });

    const stats = [
        [`${completos}/${planejados}`, 'treinos da semana'],
        [`${sequencia}`, sequencia === 1 ? 'treino seguido' : 'treinos seguidos'],
        [`${treinosCompletos}`, `${treinosCompletos === 1 ? 'treino completo' : 'treinos completos'} (30 dias)`],
        [`${exercicios}`, `${exercicios === 1 ? 'exercício feito' : 'exercícios feitos'} (30 dias)`]
    ];
    document.getElementById('estatisticas').innerHTML = stats
        .map(([n, r]) => `<div class="stat"><div class="numero">${n}</div><div class="rotulo">${r}</div></div>`)
        .join('');
}

function renderHistorico(hoje) {
    const datas = Array.from(estado.conclusoes.entries())
        .filter(([, set]) => set.size > 0)
        .map(([d]) => d)
        .sort()
        .reverse()
        .slice(0, 8);

    const ul = document.getElementById('historico');
    if (datas.length === 0) {
        ul.innerHTML = '<li class="vazio">Nenhum exercício concluído ainda. Marque os de hoje para começar!</li>';
        return;
    }

    ul.innerHTML = datas.map((dataStr) => {
        const [a, m, dd] = dataStr.split('-').map(Number);
        const d = new Date(a, m - 1, dd);
        const dia = treinoDoDia(d);
        const feitos = estado.conclusoes.get(dataStr).size;
        const resumo = dia
            ? `${esc(dia.nome)} — ${feitosNoDia(dataStr, dia)}/${dia.exercicios.length} exercícios`
            : `${feitos} exercício(s)`;
        const quando = dataStr === fmtData(hoje)
            ? 'Hoje'
            : `${SIGLAS[d.getDay()]}, ${String(dd).padStart(2, '0')}/${String(m).padStart(2, '0')}`;
        return `<li><span>${resumo}</span><span class="quando">${quando}</span></li>`;
    }).join('');
}

// ---------- início ----------
document.addEventListener('DOMContentLoaded', () => {
    estado.userId = localStorage.getItem('userId');
    if (!estado.userId) {
        window.location.href = 'login.html';
        return;
    }
    document.getElementById('btn-tentar').addEventListener('click', carregar);
    carregar();
});
