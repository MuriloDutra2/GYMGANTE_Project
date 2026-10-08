// Página inicial: o seletor escreve a página de exemplo e a semana ao vivo.
// Tudo aqui é demonstração; o plano de verdade vem do servidor depois do cadastro.

(() => {
  const PRE_KEY = 'gymgante:pre';
  const SIGLAS = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
  const ORDEM_SEMANA = [1, 2, 3, 4, 5, 6, 0]; // segunda a domingo
  const PADROES = { 3: [1, 3, 5], 4: [1, 2, 4, 5], 5: [1, 2, 3, 4, 5], 6: [1, 2, 3, 4, 5, 6] };

  // Primeiro dia do plano para cada frequência (c = composto, i = isolado)
  const PRIMEIRO_DIA = {
    3: { grupo: 'Superiores', ex: [['Supino reto com halteres', 'c'], ['Puxada frontal', 'c'], ['Desenvolvimento com halteres', 'c'], ['Crucifixo com halteres', 'i'], ['Rosca alternada com halteres', 'i'], ['Tríceps na polia com corda', 'i']] },
    4: { grupo: 'Peito e Tríceps', ex: [['Supino reto com barra', 'c'], ['Supino inclinado com halteres', 'c'], ['Crucifixo com halteres', 'i'], ['Tríceps na polia com corda', 'i'], ['Tríceps testa', 'i'], ['Peck deck', 'i']] },
    5: { grupo: 'Peito', ex: [['Supino reto com barra', 'c'], ['Supino inclinado com halteres', 'c'], ['Supino reto com halteres', 'c'], ['Crucifixo com halteres', 'i'], ['Peck deck', 'i'], ['Prancha', 'i']] }
  };
  PRIMEIRO_DIA[6] = PRIMEIRO_DIA[4];

  const NOMES_DIA = {
    3: [['Treino A', 'Superiores'], ['Treino B', 'Inferiores'], ['Treino C', 'Corpo completo']],
    4: [['Treino A', 'Peito e Tríceps'], ['Treino B', 'Costas e Bíceps'], ['Treino C', 'Pernas e Glúteos'], ['Treino D', 'Ombros e Core']],
    5: [['Treino A', 'Peito'], ['Treino B', 'Costas'], ['Treino C', 'Posterior'], ['Treino D', 'Ombros e Braços'], ['Treino E', 'Anterior e Glúteos']],
    6: [['Treino A', 'Peito e Tríceps'], ['Treino B', 'Costas e Bíceps'], ['Treino C', 'Quadríceps'], ['Treino D', 'Ombros e Trapézio'], ['Treino E', 'Posterior e Glúteos'], ['Treino F', 'Braços e Core']]
  };

  const OBJETIVOS = {
    'Hipertrofia':        { c: [4, '8-10'],  i: [3, '10-12'], cardio: false },
    'Definição Muscular': { c: [4, '10-12'], i: [3, '12-15'], cardio: false },
    'Perda de Gordura':   { c: [3, '12-15'], i: [3, '15'],    cardio: true }
  };
  const POR_NIVEL = { 'Iniciante': 4, 'Intermediário': 5, 'Avançado': 6 };

  const form = document.getElementById('form-inicio');
  const lista = document.getElementById('lp-lista');
  const gridSemana = document.getElementById('semana-grid');
  const reduz = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  const el = (tag, cls, texto) => {
    const n = document.createElement(tag);
    if (cls) n.className = cls;
    if (texto != null) n.textContent = texto;
    return n;
  };

  function respostas() {
    const d = new FormData(form);
    return { objetivo: d.get('objetivo'), dias: d.get('dias'), nivel: d.get('nivel') };
  }
  const frequencia = (dias) => Number(String(dias).charAt(0));

  function caixa(rotulo) {
    const b = el('button', 'box');
    b.type = 'button';
    b.setAttribute('aria-pressed', 'false');
    b.setAttribute('aria-label', rotulo);
    b.addEventListener('click', () => {
      b.setAttribute('aria-pressed', b.getAttribute('aria-pressed') === 'true' ? 'false' : 'true');
      if (b.closest('.semana-grid')) resumirSemana();
    });
    return b;
  }

  // ---------- a página de exemplo ----------
  function renderFolha() {
    const { objetivo, dias, nivel } = respostas();
    const f = frequencia(dias);
    const dia = PRIMEIRO_DIA[f];
    const params = OBJETIVOS[objetivo];
    const quantos = POR_NIVEL[nivel];

    document.getElementById('lp-grupo').textContent = dia.grupo;
    document.getElementById('lp-meta').textContent = `${f} dias por semana, ${nivel.toLowerCase()}, ${objetivo.toLowerCase()}`;

    lista.replaceChildren();
    dia.ex.slice(0, quantos).forEach(([nome, tipo]) => {
      let [series, reps] = params[tipo];
      if (nivel === 'Iniciante') series = Math.max(2, series - 1);
      if (nome === 'Prancha') reps = '30-45 s';

      const li = el('li', 'lp-row');
      li.append(el('span', 'lp-nome', nome), el('span', 'lp-serie num', `${series} × ${reps}`));
      const caixas = el('span', 'lp-caixas');
      for (let s = 1; s <= series; s++) caixas.append(caixa(`Série ${s} de ${nome}`));
      li.append(caixas);
      lista.append(li);
    });

    if (params.cardio) {
      const li = el('li', 'lp-row');
      li.append(el('span', 'lp-nome', 'Cardio (esteira, bike ou elíptico)'), el('span', 'lp-serie num', '1 × 15-20 min'));
      const caixas = el('span', 'lp-caixas');
      caixas.append(caixa('Cardio feito'));
      li.append(caixas);
      lista.append(li);
    }
  }

  // ---------- a semana ----------
  function renderSemana() {
    const { dias } = respostas();
    const f = frequencia(dias);
    const padrao = PADROES[f];
    const hoje = new Date().getDay();

    document.getElementById('semana-texto').textContent =
      `Com ${f} dias por semana, o painel mostra o treino certo para cada dia e deixa os outros como descanso.`;

    gridSemana.replaceChildren();
    ORDEM_SEMANA.forEach((dow) => {
      const idx = padrao.indexOf(dow);
      const li = el('li', 'dia-col' + (idx < 0 ? ' dia-col--folga' : '') + (dow === hoje ? ' dia-col--hoje' : ''));
      li.append(el('span', 'sigla', SIGLAS[dow]));
      if (idx >= 0) {
        const [nome, grupo] = NOMES_DIAS_DE(f)[idx];
        li.append(el('span', 'nome', nome), el('span', 'grupo', grupo), caixa(`${SIGLAS[dow]}: ${nome} feito`));
      } else {
        li.append(el('span', 'nome', 'Descanso'));
        const ic = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        ic.setAttribute('aria-hidden', 'true');
        ic.innerHTML = '<use href="img/sprite.svg#i-bed"></use>';
        li.append(ic);
      }
      gridSemana.append(li);
    });
    resumirSemana();
  }
  const NOMES_DIAS_DE = (f) => NOMES_DIA[f];

  function resumirSemana() {
    const feitos = gridSemana.querySelectorAll('.box[aria-pressed="true"]').length;
    const total = gridSemana.querySelectorAll('.box').length;
    document.getElementById('semana-resumo').textContent =
      feitos === 0 ? 'Toque nas caixas para riscar os treinos desta semana de exemplo.'
        : feitos === total ? 'Semana completa. Bom descanso.'
          : `${feitos} de ${total} treinos riscados.`;
  }

  // ---------- atualização ao vivo ----------
  function atualizar() {
    if (!reduz && lista.children.length) {
      lista.animate(
        [{ opacity: 0.35, filter: 'blur(2px)' }, { opacity: 1, filter: 'blur(0)' }],
        { duration: 200, easing: 'cubic-bezier(0.23, 1, 0.32, 1)' }
      );
    }
    renderFolha();
    renderSemana();
  }
  form.addEventListener('change', atualizar);

  // ---------- gerar meu treino: guarda as respostas e segue para o cadastro ----------
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const { objetivo, dias, nivel } = respostas();
    try {
      localStorage.setItem(PRE_KEY, JSON.stringify({ objetivoPrincipal: objetivo, diasPorSemana: dias, nivel }));
    } catch (_) { /* sem armazenamento: o aluno responde de novo na anamnese */ }

    const logado = (() => { try { return !!localStorage.getItem('userId'); } catch (_) { return false; } })();
    window.location.href = logado ? 'anamnese.html' : 'cadastro.html';
  });

  // ---------- revelar seções ao rolar (uma vez) ----------
  const alvos = document.querySelectorAll('[data-reveal]');
  if ('IntersectionObserver' in window && !reduz) {
    const io = new IntersectionObserver((entradas) => {
      entradas.forEach((en) => {
        if (en.isIntersecting) { en.target.classList.add('in'); io.unobserve(en.target); }
      });
    }, { rootMargin: '0px 0px -12% 0px', threshold: 0.08 });
    alvos.forEach((a) => io.observe(a));
  } else {
    alvos.forEach((a) => a.classList.add('in'));
  }

  atualizar();
})();
