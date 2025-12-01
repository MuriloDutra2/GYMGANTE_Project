# 🏋️ GymGante - Sistema de Treinos Inteligentes com IA

> **TCC - Curso de Análise e Desenvolvimento de Sistemas**

O **GymGante** é uma plataforma web full-stack que revoluciona a prescrição de treinos em academias. Utilizando **Inteligência Artificial Generativa (Google Gemini)**, o sistema analisa o perfil físico e restrições do aluno para criar, em segundos, um plano de treino periodizado e personalizado, algo que levaria horas para ser feito manualmente.

![Status do Projeto](https://img.shields.io/badge/Status-Concluído-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green)
![AI](https://img.shields.io/badge/AI-Google_Gemini-blue)
![Azure](https://img.shields.io/badge/Cloud-Azure_SQL-0078D4)

---

## 📸 Screenshots

*(Coloque aqui prints das telas: Login, Anamnese e a Tela de Treino Gerado)*
<div style="display: flex; gap: 10px;">
  <img src="login.png" width="300" />
  <img src="treinos.png" width="300" />
</div>

---

## 🧠 Diferenciais Técnicos (A "Mágica")

O sistema não utiliza apenas um banco de dados estático. Ele implementa uma **Arquitetura Híbrida**:

1.  **Engenharia de Prompt Contextual:** O Back-end não apenas "pede um treino". Ele injeta regras de fisiologia (séries, repetições, descanso) no prompt com base no objetivo do aluno (Hipertrofia vs Emagrecimento) antes de consultar a IA.
2.  **Segurança e Responsabilidade:** Possui uma trava lógica de segurança. Se o aluno relata lesões na anamnese, o sistema bloqueia a geração automática e direciona para um profissional humano.
3.  **Armazenamento Híbrido (SQL + JSON):** Utilizamos Azure SQL para dados estruturados (Usuários) e armazenamento JSON para a flexibilidade dos roteiros de treino, garantindo performance e escalabilidade.
4.  **Resiliência:** O Front-end possui parsers defensivos que conseguem renderizar o treino mesmo se a IA variar o formato da resposta (JSON ou Markdown).

---

## 🛠️ Stack Tecnológica

### Back-End (API RESTful)
- **Linguagem:** Java 21 (LTS)
- **Framework:** Spring Boot 3
- **Segurança:** Spring Security + BCrypt (Hash de senhas)
- **Banco de Dados:** Microsoft Azure SQL Database (Serverless)
- **Integração IA:** Google Gemini API (REST Template)
- **Boilerplate:** Lombok

### Front-End
- **Linguagem:** JavaScript (ES6+), HTML5, CSS3
- **Design:** CSS Grid/Flexbox, Responsivo (Mobile-First)
- **Comunicação:** Fetch API (Assíncrono)
- **Renderização:** Marked.js (Markdown para HTML)

---

## 🚀 Como Rodar o Projeto

### Pré-requisitos
- Java JDK 21 instalado.
- Maven instalado.
- Uma chave de API do Google Gemini (AI Studio).

### Passo 1: Clonar
```bash
git clone [https://github.com/SeuUsuario/GYMGANTE_Project.git](https://github.com/SeuUsuario/GYMGANTE_Project.git)
cd GYMGANTE_Project
