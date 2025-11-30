// Configurações da API
const API_CONFIG = {
    BASE_URL: 'http://localhost:8080',
    ENDPOINTS: {
        USUARIOS: {
            CADASTRO: '/api/usuarios/cadastro',
            LOGIN: '/api/usuarios/login'
        },
        ANAMNESE: '/anamnese'
    },
    TIMEOUT: 30000 // 30 segundos
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



