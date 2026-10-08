// Utilitários gerais

/**
 * Mostra uma notificação (substitui alert). Tipos: success, error, warning, info.
 */
function showToast(message, type = 'info', duration = 3000) {
    const existente = document.getElementById('toast-container');
    if (existente) existente.remove();

    const cores = {
        success: ['#17284a', '#f4f8fc'],
        error:   ['#c8322a', '#ffffff'],
        warning: ['#f4f8fc', '#17284a'],
        info:    ['#17284a', '#f4f8fc']
    };
    const [fundo, texto] = cores[type] || cores.info;

    const toast = document.createElement('div');
    toast.id = 'toast-container';
    toast.setAttribute('role', 'status');
    toast.setAttribute('aria-live', 'polite');
    toast.style.cssText = `
        position: fixed;
        top: max(16px, env(safe-area-inset-top));
        right: 16px;
        left: 16px;
        margin-left: auto;
        max-width: 26rem;
        background: ${fundo};
        color: ${texto};
        border: 1.5px solid #17284a;
        border-radius: 3px;
        padding: 14px 18px;
        box-shadow: 0 12px 24px -12px rgba(23, 40, 74, 0.5);
        z-index: 200;
        font-family: "Atkinson Hyperlegible", "Segoe UI", sans-serif;
        font-weight: 700;
        line-height: 1.4;
        transition: transform 220ms cubic-bezier(0.23, 1, 0.32, 1), opacity 220ms cubic-bezier(0.23, 1, 0.32, 1);
        transform: translateY(-8px);
        opacity: 0;
    `;
    toast.textContent = message;
    document.body.appendChild(toast);
    requestAnimationFrame(() => { toast.style.transform = 'none'; toast.style.opacity = '1'; });

    setTimeout(() => {
        toast.style.transform = 'translateY(-8px)';
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 240);
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
 * Mostra o carregamento (um risco vermelho que se escreve)
 */
function showLoading(message = 'Carregando...') {
    if (document.getElementById('loading-overlay-global')) return;

    const overlay = document.createElement('div');
    overlay.id = 'loading-overlay-global';
    overlay.className = 'carregando-overlay';
    overlay.setAttribute('role', 'status');
    overlay.setAttribute('aria-live', 'polite');
    overlay.innerHTML = `
        <div class="loading-content">
            <p class="loading-titulo"></p>
            <div class="traco" aria-hidden="true"></div>
        </div>`;
    overlay.querySelector('.loading-titulo').textContent = message;
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




