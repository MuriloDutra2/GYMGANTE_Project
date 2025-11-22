🏋️ **GYMGATE - Sistema de Treinos com IA**

O front-end está quase pronto! Aqui está o passo a passo para testar:

## 🚀 **Passo a Passo para Testar o Sistema**

### 1. **Verificar se o Back-End Está Rodando**
- O Spring Boot deve estar na porta **8080**
- Url base: `http://localhost:8080`

### 2. **Iniciar Servidor Front-End (Para testes locais)**
Execute este comando no diretório `Frontend`:
```bash
cd Frontend
python -m http.server 8000
```
- Agora acesse: `http://localhost:8000/index.html`

### 3. **Fluxo de Teste Completo**

**a) Cadastro** (`cadastro.html`)
- Crie uma conta com email válido
- Deve redirecionar para `login.html` após sucesso

**b) Login** (`login.html`)
- Faça login com seus dados
- ✅ **Novo:** Após login, o sistema **automaticamente** verifica se você tem treino e redireciona:
  - **Se NÃO tem treino** → `anamnese.html` (Formulário)
  - **Se TEM treino** → `treino.html` (Visualizar)

**c) Anamnese** (`anamnese.html`) - Para novos usuários
- Preenche o formulário (4 campos obrigatórios)
- Clica "🤖 Gerar Treino com IA"
- **Loading** aparece por 5-10 segundos
- Redireciona para `treino.html`

**d) Treino** (`treino.html`)
- Mostra o treino gerado em HTML formatado
- Botão "**🔄 Gerar Novo Treino**" (sempre visível, modal de confirmação)
- Botão "**🖨️ Imprimir**"

---

## 📋 **Arquivos Criados/Modificados**

### ✅ **Páginas HTML:**
- `anamnese.html` - Formulário de perguntas
- **Login** alterado para verificar treino automaticamente

### ✅ **Arquivos CSS:**
- `css/anamnese.css` - Estilos do formulário
- `css/treino.css` - Estilos da visualização (futuro)
- `css/forms.css` - Estilos gerais dos formulários

### ✅ **Arquivos JavaScript:**
- `js/anamnese.js` - Lógica do formulário e geração de treino
- `@login.js` - Modificado com verificação automática
- `js/treino.js` - Futuro (renderizar markdown)
- `js/dashboard.js` - Futuro (página de verificação)

---

## ⚠️ **Possíveis Problemas e Soluções**

### **1. Erro de CORS:**
Se der erro de CORS, configure no Spring Boot:
```java
@CrossOrigin(origins = "http://localhost:8000")
```

### **2. Servidor HTTP Local:**
Para Chrome/Safari permitirem `fetch()`, precisa de servidor (não arquivo local).

### **3. Login não redireciona:**
Verifique se o `userId` está sendo salvo no `localStorage`:
```javascript
console.log(localStorage.getItem('userId'));
```

### **4. API de Anamnese:**
Certifique-se que o endpoint `/anamnese/{userId}` existe e retorna:
- `200` com dados (tem treino)
- `404` (não tem treino)

---

## 🎯 **Próximos Passos**
Aguardo você testar e informar os erros. Quando funcionar, crio os arquivos de `treino.html` e `js/treino.js` para renderizar o treino em HTML formatado.

Vamos lá! 💪🚀
