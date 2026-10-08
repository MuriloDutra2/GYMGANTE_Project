// Configurações da API
// Front e API são servidos pelo mesmo serviço (Spring), então a URL base é relativa.
const API_CONFIG = {

    BASE_URL: '',
    
    ENDPOINTS: {
        USUARIOS: {
            CADASTRO: '/api/usuarios/cadastro',
            LOGIN: '/api/usuarios/login'
        },
        ANAMNESE: '/anamnese'
    },
    TIMEOUT: 60000 // 60 segundos (cold start do Render)
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




