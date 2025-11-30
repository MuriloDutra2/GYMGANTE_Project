// Utilitários gerais

/**
 * Mostra uma notificação toast (substitui alert)
 */
function showToast(message, type = 'info', duration = 3000) {
    // Remove toast anterior se existir
    const existingToast = document.getElementById('toast-container');
    if (existingToast) {
        existingToast.remove();
    }

    // Cria container do toast
    const toast = document.createElement('div');
    toast.id = 'toast-container';
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#10b981' : type === 'error' ? '#ef4444' : '#8B5CF6'};
        color: white;
        padding: 16px 24px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
        z-index: 10000;
        animation: slideIn 0.3s ease-out;
        max-width: 400px;
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    `;
    toast.textContent = message;

    // Adiciona animação
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideIn {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        @keyframes slideOut {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(100%);
                opacity: 0;
            }
        }
    `;
    if (!document.getElementById('toast-animations')) {
        style.id = 'toast-animations';
        document.head.appendChild(style);
    }

    document.body.appendChild(toast);

    // Remove após duração
    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

/**
 * Aplica máscara de CPF
 */
function maskCPF(value) {
    return value
        .replace(/\D/g, '')
        .replace(/(\d{3})(\d)/, '$1.$2')
        .replace(/(\d{3})(\d)/, '$1.$2')
        .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
}

/**
 * Aplica máscara de telefone
 */
function maskPhone(value) {
    return value
        .replace(/\D/g, '')
        .replace(/(\d{2})(\d)/, '($1) $2')
        .replace(/(\d{4,5})(\d{4})$/, '$1-$2');
}

/**
 * Valida CPF básico (formato)
 */
function isValidCPFFormat(cpf) {
    const cleanCPF = cpf.replace(/\D/g, '');
    return cleanCPF.length === 11;
}

/**
 * Valida email básico
 */
function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

/**
 * Valida senha (mínimo 8 caracteres)
 */
function isValidPassword(password) {
    return password && password.length >= 8;
}

/**
 * Mostra loading overlay
 */
function showLoading(message = 'Carregando...') {
    const existing = document.getElementById('loading-overlay-global');
    if (existing) return;

    const overlay = document.createElement('div');
    overlay.id = 'loading-overlay-global';
    overlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(26, 13, 46, 0.95);
        backdrop-filter: blur(10px);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 9999;
        flex-direction: column;
        gap: 20px;
    `;
    overlay.innerHTML = `
        <div style="
            width: 60px;
            height: 60px;
            border: 4px solid rgba(139, 92, 246, 0.3);
            border-top: 4px solid #8B5CF6;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        "></div>
        <h3 style="color: #E0E0E0; font-size: 1.2em; margin: 0;">${message}</h3>
    `;
    document.body.appendChild(overlay);
}

/**
 * Esconde loading overlay
 */
function hideLoading() {
    const overlay = document.getElementById('loading-overlay-global');
    if (overlay) {
        overlay.remove();
    }
}



