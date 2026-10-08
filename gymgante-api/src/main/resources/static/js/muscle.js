// Associa exercícios e dias do plano aos pictogramas de músculo do sprite (img/sprite.svg).
// Os nomes vêm do catálogo do servidor; o texto do dia vem do plano ("Peito e Tríceps", "Superiores"...).

const Muscle = (() => {
  const norm = (t) => String(t || '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');

  // Primeira regra que casar vence; a ordem resolve ambiguidades (ex.: "terra romeno" é posterior).
  const REGRAS = [
    [/cardio|esteira|bike|eliptico/, 'cardio'],
    [/panturrilha/, 'panturrilha'],
    [/prancha|abdominal|roda abdominal|elevacao de pernas/, 'core'],
    [/stiff|terra romeno|good morning|flexora/, 'posterior'],
    [/elevacao pelvica|sumo|passada|step-up|abducao|gluteo/, 'gluteos'],
    [/triceps|mergulho no banco|supino fechado/, 'triceps'],
    [/rosca/, 'biceps'],
    [/encolhimento|remada alta/, 'trapezio'],
    [/elevacao lateral|elevacao frontal|desenvolvimento|arnold|crucifixo inverso/, 'ombros'],
    [/supino|flexao|crucifixo|peck|crossover|mergulho nas paralelas/, 'peito'],
    [/puxada|remada|barra fixa|levantamento terra|pulldown|pullover/, 'costas'],
    [/agachamento|leg press|hack|afundo|extensora/, 'quadriceps']
  ];

  const PALAVRAS_DIA = [
    [/corpo completo/, ['peito', 'quadriceps']],
    [/superiores/, ['peito', 'costas']],
    [/inferiores/, ['quadriceps', 'gluteos']],
    [/bracos/, ['biceps', 'triceps']],
    [/peito/, ['peito']],
    [/costas/, ['costas']],
    [/ombro/, ['ombros']],
    [/trapezio/, ['trapezio']],
    [/biceps/, ['biceps']],
    [/triceps/, ['triceps']],
    [/quadriceps|anterior/, ['quadriceps']],
    [/posterior/, ['posterior']],
    [/glute/, ['gluteos']],
    [/panturrilha/, ['panturrilha']],
    [/core/, ['core']],
    [/pernas/, ['quadriceps', 'posterior']]
  ];

  function doExercicio(nome) {
    const n = norm(nome);
    const regra = REGRAS.find(([re]) => re.test(n));
    return regra ? regra[1] : null;
  }

  function doDia(texto) {
    const n = norm(texto);
    const grupos = [];
    PALAVRAS_DIA.forEach(([re, gs]) => {
      if (re.test(n)) gs.forEach((g) => { if (!grupos.includes(g)) grupos.push(g); });
    });
    return grupos.slice(0, 2);
  }

  // Marcação do pictograma; decorativo, então fica fora da leitura de tela.
  function icone(grupo, tamanho = 32) {
    if (!grupo) return '';
    return `<svg class="muscle" width="${tamanho}" height="${tamanho}" aria-hidden="true" focusable="false"><use href="img/sprite.svg#m-${grupo}"></use></svg>`;
  }

  return { doExercicio, doDia, icone };
})();
