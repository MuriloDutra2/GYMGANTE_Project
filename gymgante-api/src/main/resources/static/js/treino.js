document.addEventListener('DOMContentLoaded', async () => {
    console.log('🏋️ Iniciando carregamento da página de treino...');

    // Verificar se usuário está logado
    const userId = localStorage.getItem('userId');
    const usuarioLogado = JSON.parse(localStorage.getItem('usuarioLogado') || '{}');
    const anamneseData = JSON.parse(localStorage.getItem('anamneseData') || '{}');
    const treinoData = JSON.parse(localStorage.getItem('treinoData') || '{}');

    if (!userId) {
        console.log('⚠️ Usuário não está logado. Redirecionando para login...');
        showToast('Você precisa entrar para acessar seu treino.', 'error');
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);
        return;
    }

    // Preencher informações do usuário no header
    if (usuarioLogado.nomeCompleto) {
        document.getElementById('user-nome').textContent = usuarioLogado.nomeCompleto;
    }
    if (anamneseData.objetivoPrincipal) {
        document.getElementById('user-objetivo').textContent = anamneseData.objetivoPrincipal;
    }
    if (anamneseData.diasPorSemana) {
        document.getElementById('user-dias').textContent = anamneseData.diasPorSemana;
    }
    if (anamneseData.nivel) {
        document.getElementById('user-nivel').textContent = anamneseData.nivel;
    }

    // Verificar se já temos dados do treino no localStorage
    if (treinoData.treino && treinoData.tipo === 'PLANO_TREINO') {
        console.log('✅ Treino encontrado no localStorage. Renderizando...');
        console.log('📦 Tipo do treino no localStorage:', typeof treinoData.treino);
        console.log('📦 Primeiros 200 caracteres:', treinoData.treino.substring ? treinoData.treino.substring(0, 200) : 'Não é string');
        renderizarTreino(treinoData.treino);
        document.getElementById('loading').classList.add('hidden');
    } else if (treinoData.tipo === 'AVISO') {
        console.log('⚠️ Usuário tem restrição médica');
        mostrarAvisoRestricao(treinoData.treino);
        document.getElementById('loading').classList.add('hidden');
    } else {
        // Buscar treino do servidor
        console.log('🔍 Buscando treino do servidor...');
        await carregarTreinoDoServidor(userId);
    }

    // Configurar botão de gerar novo treino
    const btnNovoTreino = document.getElementById('btn-novo-treino');
    const modalConfirm = document.getElementById('modal-confirm');
    const btnCancel = document.getElementById('btn-cancel');
    const btnConfirm = document.getElementById('btn-confirm');

    btnNovoTreino.addEventListener('click', () => {
        modalConfirm.classList.remove('hidden');
    });

    btnCancel.addEventListener('click', () => {
        modalConfirm.classList.add('hidden');
    });

    btnConfirm.addEventListener('click', () => {
        modalConfirm.classList.add('hidden');
        // Redirecionar para anamnese com parâmetro de atualização
        window.location.href = 'anamnese.html?update=true';
    });
});

async function carregarTreinoDoServidor(userId) {
    try {
        const response = await fetchWithTimeout(
            `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.ANAMNESE}/${userId}`
        );
        
        if (response.status === 404) {
            console.log('ℹ️ Usuário não tem treino. Redirecionando para anamnese...');
            showToast('Você ainda não tem um treino cadastrado. Vamos criar um para você!', 'info');
            setTimeout(() => {
                window.location.href = 'anamnese.html';
            }, 2000);
            return;
        }

        if (!response.ok) {
            throw new Error(`Erro ao buscar treino: ${response.status}`);
        }

        const data = await response.json();
        console.log('✅ Treino carregado do servidor:', data);

        // Salvar no localStorage
        localStorage.setItem('anamneseData', JSON.stringify({
            anamneseId: data.anamneseId,
            objetivoPrincipal: data.objetivoPrincipal,
            diasPorSemana: data.diasPorSemana,
            nivel: data.nivel,
            temRestricao: data.temRestricao
        }));
        
        localStorage.setItem('treinoData', JSON.stringify({
            tipo: data.tipo,
            treino: data.treino
        }));

        // Atualizar informações do usuário
        if (data.objetivoPrincipal) {
            document.getElementById('user-objetivo').textContent = data.objetivoPrincipal;
        }
        if (data.diasPorSemana) {
            document.getElementById('user-dias').textContent = data.diasPorSemana;
        }
        if (data.nivel) {
            document.getElementById('user-nivel').textContent = data.nivel;
        }

        // Renderizar treino
        if (data.tipo === 'PLANO_TREINO') {
            console.log('📦 Tipo do treino do servidor:', typeof data.treino);
            console.log('📦 Primeiros 200 caracteres do servidor:', data.treino.substring ? data.treino.substring(0, 200) : 'Não é string');
            renderizarTreino(data.treino);
        } else if (data.tipo === 'AVISO') {
            mostrarAvisoRestricao(data.treino);
        }

        document.getElementById('loading').classList.add('hidden');

    } catch (error) {
        console.error('❌ Erro ao carregar treino:', error);
        document.getElementById('loading').classList.add('hidden');
        showToast('Erro ao carregar seu treino. Você será redirecionado para criar um novo.', 'error');
        setTimeout(() => {
            window.location.href = 'anamnese.html';
        }, 2000);
    }
}

function renderizarTreino(treinoData) {
    const container = document.getElementById('treino-container');
    
    if (!treinoData) {
        container.innerHTML = '<p class="erro">Treino não encontrado.</p>';
        return;
    }

    console.log('🔍 Tipo do treinoData:', typeof treinoData);
    console.log('🔍 Primeiros caracteres:', treinoData.substring(0, 100));

    try {
        // Se já for um objeto, usar diretamente
        let treinoJson = treinoData;
        
        // Se for string, tentar parsear
        if (typeof treinoData === 'string') {
            // Limpar possíveis markdown code blocks e caracteres especiais
            let jsonLimpo = treinoData.trim();
            
            // Remover markdown code blocks se existirem
            if (jsonLimpo.startsWith('```json')) {
                jsonLimpo = jsonLimpo.substring(7).trim();
            }
            if (jsonLimpo.startsWith('```')) {
                jsonLimpo = jsonLimpo.substring(3).trim();
            }
            if (jsonLimpo.endsWith('```')) {
                jsonLimpo = jsonLimpo.substring(0, jsonLimpo.length - 3).trim();
            }
            
            // Remover possíveis BOM (Byte Order Mark) e outros caracteres invisíveis
            jsonLimpo = jsonLimpo.replace(/^\uFEFF/, '').trim();
            
            // Encontrar o primeiro { e último } para extrair apenas o JSON
            const primeiroBrace = jsonLimpo.indexOf('{');
            const ultimoBrace = jsonLimpo.lastIndexOf('}');
            
            if (primeiroBrace !== -1 && ultimoBrace !== -1 && ultimoBrace > primeiroBrace) {
                jsonLimpo = jsonLimpo.substring(primeiroBrace, ultimoBrace + 1);
            }
            
            // Verificar se começa com { (JSON válido)
            if (jsonLimpo.startsWith('{') && jsonLimpo.endsWith('}')) {
                try {
                    treinoJson = JSON.parse(jsonLimpo);
                    console.log('✅ JSON parseado com sucesso!');
                    console.log('📊 Estrutura parseada:', {
                        temTitulo: !!treinoJson.titulo,
                        temDias: !!treinoJson.dias,
                        qtdDias: treinoJson.dias ? treinoJson.dias.length : 0
                    });
                } catch (parseError) {
                    console.error('❌ Erro ao parsear JSON:', parseError);
                    console.error('❌ JSON problemático:', jsonLimpo.substring(0, 500));
                    console.log('📄 Tentando renderizar como markdown...');
                    renderizarTreinoMarkdown(treinoData, container);
                    return;
                }
            } else {
                // Não é JSON, renderizar como markdown
                console.log('ℹ️ Não é JSON válido (não começa com { ou não termina com }), renderizando como markdown');
                console.log('📄 Primeiros 100 chars:', jsonLimpo.substring(0, 100));
                renderizarTreinoMarkdown(treinoData, container);
                return;
            }
        }
        
        // Verificar se é JSON estruturado válido
        if (treinoJson && typeof treinoJson === 'object' && treinoJson.titulo && treinoJson.dias && Array.isArray(treinoJson.dias)) {
            console.log('✅ Treino estruturado detectado!');
            console.log('📊 Estrutura:', {
                titulo: treinoJson.titulo,
                dias: treinoJson.dias.length
            });
            renderizarTreinoEstruturado(treinoJson, container);
            return;
        } else {
            console.log('⚠️ JSON não tem estrutura esperada, renderizando como markdown');
            renderizarTreinoMarkdown(treinoData, container);
        }

    } catch (error) {
        console.error('❌ Erro ao renderizar treino:', error);
        console.error('Stack:', error.stack);
        // Em caso de erro, mostrar o JSON formatado
        container.innerHTML = `
            <div class="erro-container">
                <h3>Erro ao renderizar treino</h3>
                <pre style="white-space: pre-wrap; font-family: monospace; background: var(--surface); padding: 20px; border-radius: 8px;">${JSON.stringify(treinoData, null, 2)}</pre>
            </div>
        `;
    }
}

// Função auxiliar para escapar HTML (prevenir XSS)
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function renderizarTreinoEstruturado(treino, container) {
    try {
        console.log('🎨 Renderizando treino estruturado:', treino);
        
        let html = `
            <div class="treino-header">
                <h2>${escapeHtml(treino.titulo || 'Plano de Treino')}</h2>
                ${treino.descricao ? `<p class="treino-descricao">${escapeHtml(treino.descricao)}</p>` : ''}
            </div>
            <div class="treino-dias-grid">
        `;

        if (!treino.dias || !Array.isArray(treino.dias)) {
            throw new Error('Estrutura de dias inválida');
        }

        treino.dias.forEach((dia, index) => {
            if (!dia || !dia.exercicios || !Array.isArray(dia.exercicios)) {
                console.warn(`⚠️ Dia ${index} inválido, pulando...`);
                return;
            }

            html += `
                <div class="dia-card-treino" data-dia="${index}">
                    <div class="dia-header">
                        <div class="dia-icones" aria-hidden="true">${Muscle.doDia(dia.grupoMuscular).map((g) => Muscle.icone(g, 34)).join('')}</div>
                        <div>
                            <h3>${escapeHtml(dia.nome || `Dia ${index + 1}`)}</h3>
                            ${dia.grupoMuscular ? `<span class="grupo-muscular">${escapeHtml(dia.grupoMuscular)}</span>` : ''}
                        </div>
                    </div>
                    <div class="exercicios-lista">
            `;

            dia.exercicios.forEach((exercicio, exIndex) => {
                if (!exercicio || !exercicio.nome) {
                    console.warn(`⚠️ Exercício ${exIndex} do dia ${index} inválido, pulando...`);
                    return;
                }

                html += `
                    <div class="exercicio-card">
                        <div class="exercicio-nome">
                            <strong>${escapeHtml(exercicio.nome)}</strong>
                        </div>
                        <div class="exercicio-info">
                            <div class="info-item">
                                <span class="info-label">Séries:</span>
                                <span class="info-value">${escapeHtml(exercicio.series || '-')}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">Repetições:</span>
                                <span class="info-value">${escapeHtml(exercicio.repeticoes || '-')}</span>
                            </div>
                            ${exercicio.descanso && exercicio.descanso !== '-' ? `
                            <div class="info-item">
                                <span class="info-label">Descanso:</span>
                                <span class="info-value">${escapeHtml(exercicio.descanso)}</span>
                            </div>
                            ` : ''}
                        </div>
                        ${exercicio.observacoes ? `
                        <div class="exercicio-obs">
                            <span>${escapeHtml(exercicio.observacoes)}</span>
                        </div>
                        ` : ''}
                    </div>
                `;
            });

            if (dia.observacoes) {
                html += `
                    <div class="dia-observacoes">
                        <strong>Observações</strong>
                        <p>${escapeHtml(dia.observacoes)}</p>
                    </div>
                `;
            }

            html += `
                    </div>
                </div>
            `;
        });

        html += `
            </div>
        `;

        container.innerHTML = html;
        console.log('✅ Treino renderizado com sucesso!');
        
    } catch (error) {
        console.error('❌ Erro ao renderizar treino estruturado:', error);
        container.innerHTML = `
            <div class="erro-container">
                <h3>Erro ao renderizar treino</h3>
                <p>${error.message}</p>
                <pre style="white-space: pre-wrap; font-family: monospace; background: var(--surface); padding: 20px; border-radius: 8px; margin-top: 20px;">${JSON.stringify(treino, null, 2)}</pre>
            </div>
        `;
    }
}

function renderizarTreinoMarkdown(treinoMarkdown, container) {
    // Converter markdown para HTML usando a biblioteca marked (já incluída no HTML)
    if (typeof marked !== 'undefined') {
        let html = marked.parse(treinoMarkdown);
        html = melhorarFormatacao(html);
        container.innerHTML = html;
    } else {
        // Fallback: exibir como texto pré-formatado
        container.innerHTML = `<pre style="white-space: pre-wrap; font-family: inherit;">${treinoMarkdown}</pre>`;
    }
}

function melhorarFormatacao(html) {
    // Adicionar classes CSS para melhor visualização
    html = html.replace(/<h1>/g, '<h1 class="treino-titulo">');
    html = html.replace(/<h2>/g, '<h2 class="treino-subtitulo">');
    html = html.replace(/<h3>/g, '<h3 class="treino-dia">');
    html = html.replace(/<ul>/g, '<ul class="treino-lista">');
    html = html.replace(/<ol>/g, '<ol class="treino-lista">');
    html = html.replace(/<p>/g, '<p class="treino-paragrafo">');
    html = html.replace(/<strong>/g, '<strong class="treino-destaque">');
    
    return html;
}

function mostrarAvisoRestricao(mensagem) {
    const container = document.getElementById('treino-container');
    container.innerHTML = `
        <div class="aviso-restricao">
            <h2>Aviso importante</h2>
            <p>${escapeHtml(mensagem)}</p>
            <a class="btn btn-primary" href="anamnese.html?update=true">Atualizar anamnese</a>
        </div>
    `;
}
