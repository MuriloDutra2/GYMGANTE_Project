// ===== Painel do aluno =====
// Mostra o treino do dia (conforme o dia da semana), permite marcar exercícios como feitos
// e resume a semana, a sequência e o histórico.

const SIGLAS = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
const NOMES_DIAS = ['Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'];
const ORDEM_SEMANA = [1, 2, 3, 4, 5, 6, 0]; // segunda a domingo

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
    return new Date(d.getFullYear(), d.getMonth(), d.getDate() + n);
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

const bed = '<svg aria-hidden="true"><use href="img/sprite.svg#i-bed"></use></svg>';

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
                ? 'Não foi possível gerar seu treino agora. Tente novamente em instantes.'
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

    // Atualiza na hora (a linha anima no lugar); desfaz se a API falhar
    if (concluir) set.add(k); else set.delete(k);
    estado.conclusoes.set(dataStr, set);
    sincronizarHoje(dia, dataStr);
    renderLateral(new Date());

    try {
        const resp = await fetchWithTimeout(`${API_CONFIG.BASE_URL}/api/progresso/${estado.userId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ data: dataStr, diaTreino: dia.nome, exercicio: exercicio.nome, concluido: concluir })
        });
        if (!resp.ok) throw new Error('falha ao salvar');
    } catch (e) {
        if (concluir) set.delete(k); else set.add(k);
        sincronizarHoje(dia, dataStr);
        renderLateral(new Date());
        showToast('Não foi possível salvar. Tente novamente.', 'error');
    }
}

// ---------- renderização ----------
function renderizar() {
    const hoje = new Date();
    const nome = (JSON.parse(localStorage.getItem('usuarioLogado') || '{}').nomeCompleto || '').split(' ')[0];

    document.getElementById('saudacao').textContent = nome ? `Olá, ${nome}.` : 'Olá.';
    document.getElementById('data-hoje').textContent =
        hoje.toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' });

    renderResumo();
    renderHoje(hoje);
    renderLateral(hoje);
}

function renderLateral(hoje) {
    const temPlano = !!estado.plano;
    document.getElementById('estatisticas').classList.toggle('hidden', !temPlano);
    document.getElementById('sec-semana').classList.toggle('hidden', !temPlano);
    document.getElementById('sec-historico').classList.toggle('hidden', !temPlano);
    if (temPlano) {
        renderEstatisticas(hoje);
        renderSemana(hoje);
        renderHistorico(hoje);
    }
}

function renderResumo() {
    const a = estado.anamnese;
    const itens = [['Objetivo', a.objetivoPrincipal], ['Frequência', a.diasPorSemana], ['Nível', a.nivel]];
    document.getElementById('resumo').innerHTML = itens
        .map(([r, v]) => `<div><dt>${r}</dt><dd>${esc(v)}</dd></div>`)
        .join('');
}

function renderHoje(hoje) {
    const cartao = document.getElementById('hoje');

    // Restrição médica: sem treino automático
    if (estado.anamnese.tipo === 'AVISO') {
        cartao.innerHTML = `<div class="hoje-aviso"><h2>Aviso importante</h2><p>${esc(estado.anamnese.treino)}</p>
            <a class="btn btn-primary" href="anamnese.html?update=true">Atualizar anamnese</a></div>`;
        return;
    }

    if (!estado.plano) {
        cartao.innerHTML = `<div class="hoje-aviso"><h2>Treino de hoje</h2>
            <p>Não conseguimos interpretar o formato do seu plano. Veja-o completo na página do plano ou gere um novo.</p>
            <a class="btn btn-primary" href="treino.html">Ver plano completo</a></div>`;
        return;
    }

    const dataStr = fmtData(hoje);
    const dia = treinoDoDia(hoje);

    if (!dia) {
        cartao.innerHTML = `<div class="hoje-descanso">${bed}<h2>Hoje é dia de descanso</h2>
            <p class="muted">${esc(proximoTreinoTexto(hoje))}</p></div>`;
        return;
    }

    const icones = Muscle.doDia(dia.grupoMuscular).map((g) => Muscle.icone(g, 36)).join('');
    const lista = dia.exercicios.map((e, i) => {
        const detalhes = [
            e.series ? `Séries: ${esc(e.series)}` : '',
            e.repeticoes ? `Reps: ${esc(e.repeticoes)}` : '',
            e.descanso && e.descanso !== '-' ? `Descanso: ${esc(e.descanso)}` : ''
        ].filter(Boolean).join(' &middot; ');
        return `<li class="ex" data-i="${i}" role="checkbox" aria-checked="false" tabindex="0">
            <span class="box box--lg" aria-hidden="true" aria-pressed="false"></span>
            <span><span class="ex-nome">${esc(e.nome)}</span>
                ${detalhes ? `<span class="ex-det">${detalhes}</span>` : ''}
                ${e.observacoes ? `<span class="ex-obs">${esc(e.observacoes)}</span>` : ''}</span>
            ${Muscle.icone(Muscle.doExercicio(e.nome), 32)}</li>`;
    }).join('');

    cartao.innerHTML = `
        <div class="hoje-cab">
            <div class="muscles" aria-hidden="true">${icones}</div>
            <div><h2>Hoje: ${esc(dia.nome)}</h2>${dia.grupoMuscular ? `<span class="grupo">${esc(dia.grupoMuscular)}</span>` : ''}</div>
        </div>
        <div class="regua-linha"><div class="regua" aria-hidden="true"><i></i></div><span class="regua-txt" id="regua-txt"></span></div>
        <ul class="exercicios">${lista}</ul>
        <p class="hand hand--red fechamento hidden" id="fechamento">Treino de hoje concluído. Bom descanso.</p>`;

    cartao.querySelectorAll('.ex').forEach((li) => {
        const acionar = () => alternarExercicio(dia, dia.exercicios[Number(li.dataset.i)], dataStr);
        li.addEventListener('click', acionar);
        li.addEventListener('keydown', (ev) => {
            if (ev.key === ' ' || ev.key === 'Enter') { ev.preventDefault(); acionar(); }
        });
    });
    sincronizarHoje(dia, dataStr);
}

// Atualiza a página de hoje no lugar, para as transições (risco, régua) acontecerem de verdade
function sincronizarHoje(dia, dataStr) {
    const cartao = document.getElementById('hoje');
    const set = estado.conclusoes.get(dataStr) || new Set();
    const total = dia.exercicios.length;
    let feitos = 0;

    cartao.querySelectorAll('.ex').forEach((li) => {
        const feito = set.has(chave(dia, dia.exercicios[Number(li.dataset.i)]));
        if (feito) feitos++;
        li.classList.toggle('feito', feito);
        li.setAttribute('aria-checked', String(feito));
        li.querySelector('.box').setAttribute('aria-pressed', String(feito));
    });

    cartao.querySelector('.regua i').style.setProperty('--p', total ? feitos / total : 0);
    document.getElementById('regua-txt').textContent = `${feitos} de ${total}`;
    document.getElementById('fechamento').classList.toggle('hidden', feitos !== total);
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
        return { dia: null, classe: 'dia-col--folga', texto: '', antes: true };
    }
    const dia = treinoDoDia(data);
    if (!dia) return { dia: null, classe: 'dia-col--folga', texto: '' };

    const feitos = feitosNoDia(dataStr, dia);
    const total = dia.exercicios.length;
    const ehHoje = dataStr === fmtData(hoje);
    const futuro = data > hoje && !ehHoje;

    if (feitos >= total) return { dia, classe: 'dia-col--completo', texto: 'feito', feitos, total, completo: true };
    if (futuro) return { dia, classe: '', texto: 'a fazer', feitos, total };
    if (feitos > 0) return { dia, classe: '', texto: `${feitos} de ${total}`, feitos, total };
    if (ehHoje) return { dia, classe: '', texto: 'hoje', feitos, total };
    return { dia, classe: 'dia-col--perdido', texto: 'faltou', feitos, total };
}

function renderSemana(hoje) {
    const inicio = inicioDaSemana(hoje);
    const hojeStr = fmtData(hoje);
    let html = '';
    ORDEM_SEMANA.forEach((dow, i) => {
        const d = somarDias(inicio, i);
        const e = estadoDoDia(d, hoje);
        const classes = ['dia-col', e.classe, fmtData(d) === hojeStr ? 'dia-col--hoje' : ''].filter(Boolean).join(' ');
        if (e.dia) {
            html += `<li class="${classes}"><span class="sigla">${SIGLAS[dow]}</span>
                <span class="nome">${esc(e.dia.nome)}</span>
                <span class="grupo">${esc(e.dia.grupoMuscular || '')}</span>
                <span class="estado">${e.texto}</span>
                <span class="box" role="img" aria-pressed="${e.completo ? 'true' : 'false'}" aria-label="${e.texto}"></span></li>`;
        } else {
            html += `<li class="${classes}"><span class="sigla">${SIGLAS[dow]}</span>
                <span class="nome">${e.antes ? '-' : 'Descanso'}</span>${e.antes ? '' : bed}</li>`;
        }
    });
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
            if (e.completo) completos++;
        }
    }

    // Sequência de treinos completos (dias de descanso não quebram)
    let sequencia = 0;
    for (let i = 0; i < 60; i++) {
        const e = estadoDoDia(somarDias(hoje, -i), hoje);
        if (!e.dia) continue;
        if (e.completo) sequencia++;
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

    const linhas = [
        ['Treinos da semana', `${completos}/${planejados}`],
        [sequencia === 1 ? 'Treino seguido' : 'Treinos seguidos', `${sequencia}`],
        [`${treinosCompletos === 1 ? 'Treino completo' : 'Treinos completos'} em 30 dias`, `${treinosCompletos}`],
        [`${exercicios === 1 ? 'Exercício feito' : 'Exercícios feitos'} em 30 dias`, `${exercicios}`]
    ];
    document.getElementById('stats').innerHTML = linhas
        .map(([r, n]) => `<div><dt>${r}</dt><dd>${n}</dd></div>`)
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
        ul.innerHTML = '<li class="vazio">Nenhum exercício riscado ainda. Marque os de hoje para começar.</li>';
        return;
    }

    ul.innerHTML = datas.map((dataStr) => {
        const [a, m, dd] = dataStr.split('-').map(Number);
        const d = new Date(a, m - 1, dd);
        const dia = treinoDoDia(d);
        const feitos = estado.conclusoes.get(dataStr).size;
        const resumo = dia
            ? `${esc(dia.nome)}: ${feitosNoDia(dataStr, dia)} de ${dia.exercicios.length}`
            : `${feitos} ${feitos === 1 ? 'exercício' : 'exercícios'}`;
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
