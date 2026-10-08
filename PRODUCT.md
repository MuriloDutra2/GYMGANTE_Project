# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Stack
Delegated by the user. Chosen: plain static HTML/CSS/JS served by the existing Spring Boot app (`gymgante-api/src/main/resources/static`). No build step, so the Docker deploy on Render stays unchanged. Revisit only if a framework becomes necessary.

## Users
Primary: the general public who trains in a gym, mostly alone, who needs a workout plan and a way to follow it. They open it on a phone between sets and on a computer at home. Secondary audience (confirmed): the project is a university/portfolio piece, so recruiters and professors also judge the work; the real target audience is generic.

## Product Purpose
GymGante builds a personalized workout plan in seconds from three answers (goal, weekly frequency, experience level), then lets the student follow the plan day by day: today's workout, ticking exercises, week view, streak and history. Success: a first-time visitor understands it in seconds, gets a plan, and returns to tick exercises.

## Positioning
The plan is built instantly from the student's own answers, with no personal trainer and no waiting, and the student follows it in the same place. A medical restriction blocks the automatic plan and points to a human professional (a safety rule a neighboring product might not have).

## Operating Context
Runs at https://gymgante-api.onrender.com (Render free tier: first request after idle can take about 50 s). Flow: sign up, log in, answer the questionnaire, get the plan, follow it on the dashboard, regenerate with "Novo treino" (exercises vary each time). Pages: landing (`index.html`), `cadastro`, `login`, `anamnese`, `dashboard` (painel), `treino` (full plan). Interface language is Brazilian Portuguese.

## Capabilities and Constraints
- Plan built by a rules engine (split by 3x to 6x per week, parameters per goal, adjustments per level, about 69 exercises); AI generation (Gemini) exists but is switched off for now and planned for later phases. Do not promise AI in the interface.
- Goals: Hipertrofia / Ganho de Massa Muscular, Definição Muscular, Perda de Gordura. Levels: Iniciante, Intermediário, Avançado. Frequencies: 3x to 6x.
- Dashboard: today's workout by weekday, per-exercise checkboxes, weekly strip, streak, history.
- No real authentication yet (the user id lives in the browser).
- Undecided: pricing (the current landing lists plans, but no real billing exists); do not present prices as real.

## Brand Commitments
Name stays "GymGante". The logo and visual identity are to be redone from scratch (user's decision); the old logo in `img/logo.png` and the purple palette are not binding.

## Evidence on Hand
Real: the deployed app and three old screenshots in `readme/`. Absent and must not be fabricated: testimonials, customer logos, user counts, benchmarks, real prices, photography of real students.

## Product Principles
1. Speed to a first plan beats everything: the path from landing to a plan stays short.
2. At the gym the interface is used one-handed and fast: ticking an exercise must be effortless.
3. Be honest: no invented numbers, no AI claims while the engine is rules-based, a clear safety path for injuries.
4. One product, one voice, in plain Brazilian Portuguese.

## Accessibility & Inclusion
Readable in bright gym light and on small phones; keyboard and screen-reader support on forms and checkboxes; respect reduced motion.
