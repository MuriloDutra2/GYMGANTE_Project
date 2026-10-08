// Configurações da API
// Em localhost o Spring serve o front e a API juntos (BASE_URL vazio).
// Em produção (Netlify) o front chama a API hospedada no Render.
const RENDER_API_URL = 'https://gymgante-api.onrender.com'; // ajuste se o Render gerar outra URL
const IS_LOCAL = ['localhost', '127.0.0.1'].includes(window.location.hostname);

const API_CONFIG = {

    BASE_URL: IS_LOCAL ? '' : RENDER_API_URL,
    
    ENDPOINTS: {
        USUARIOS: {
            CADASTRO: '/api/usuarios/cadastro',
            LOGIN: '/api/usuarios/login'
        },
        ANAMNESE: '/anamnese'
    },
    TIMEOUT: 60000 // 60 segundos (IA + cold start do Render)
};

// Função auxiliar para fazer requisições com timeout
async function fetchWithTimeout(url, options = {}, timeout = API_CONFIG.TIMEOUT) {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeout);
    
    try {
        const response = await fetch(url, {
            ...options,
            signal: controller.signal
        });
        clearTimeout(timeoutId);
        return response;
    } catch (error) {
        clearTimeout(timeoutId);
        if (error.name === 'AbortError') {
            throw new Error('A requisição demorou muito para responder. Tente novamente.');
        }
        throw error;
    }
}




