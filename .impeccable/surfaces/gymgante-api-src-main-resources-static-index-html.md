---
version: 1
slug: "gymgante-api-src-main-resources-static-index-html"
primary_target: "gymgante-api/src/main/resources/static/index.html"
related_targets: ["gymgante-api/src/main/resources/static/login.html","gymgante-api/src/main/resources/static/cadastro.html","gymgante-api/src/main/resources/static/anamnese.html","gymgante-api/src/main/resources/static/dashboard.html","gymgante-api/src/main/resources/static/treino.html"]
---

# Surface brief: GymGante (landing and the whole app)

Scope and mode: Persuade for `index.html` (landing); Operate for `anamnese`, `dashboard`, `treino`; auth pages (`login`, `cadastro`) are a short Persuade-to-Operate bridge. One visual world for all of them.

Audience and job: general public who trains in a gym, mostly alone, on a phone between sets and on a computer at home; secondary audience is recruiters and professors judging a portfolio piece. Job: get a plan in seconds and follow it. Real action on the landing: pick goal, weekly frequency and level, then "Gerar meu treino" (answers carry to sign-up and prefill the questionnaire).

Proof and content: the real mechanism demonstrated live (the sample plan on the page rewrites itself from the three answers, labeled as an example). No testimonials, user counts or prices (none exist).

Constraints: plain HTML/CSS/JS served by Spring, no build step; fonts self-hosted in `static/fonts` (Archivo variable, Atkinson Hyperlegible, Kalam); Brazilian Portuguese; reduced motion respected; phone and desktop equal.

Chosen direction: Caderno de Treino (the lifter's logbook on graph paper). Memorable moment: ticking a set draws a red pen stroke across its box.

Unresolved: logo mark (tally-mark G is the working idea); icon set (authored SVG, one stroke weight); whether the sample plan on the landing reuses the real server generator or a small client copy.

## Direction contract

THESIS: The training log is the interface: every workout is a page of graph paper and every set is a box you strike through. It refuses the dark fitness app with a hero card, metric tiles and a glowing accent.

OWN-WORLD: Cool graph paper ground #f4f8fc with an 8px grid in #d9e4ef and a darker line every fifth square; navy ink #17284a for all text; one red pen #d3392e for strikes, checks, the margin rule and the primary action; graphite #6b7a8f for secondary text; short handwritten annotations in Kalam, ink blue. Components are ruled lines and 1.5px ink boxes, not shadowed cards. Archivo at a condensed width and heavy weight for headings, Atkinson Hyperlegible for reading and numbers.

STORY: A visitor understands in one look that this is a workout log that writes itself: three answers go into the page header, the plan below rewrites to match, and the red action turns that page into theirs. They believe it because the demonstration changes live; they act by pressing "Gerar meu treino".

FIRST VIEWPORT: A left-aligned ruled page filling the screen, red margin rule at 64px. Top left, a two-line Archivo headline. Under it three numbered handwritten lines (Objetivo, Frequência, Nível) as the real choice controls, each a row of tickable boxes. Under those, the red "Gerar meu treino" plate. On the right, one tall log page of a sample day with set boxes, rewriting from the answers. No hero card, no badge row, no scroll cue.

FORM: Notebook page, candidate 1 of my ordered list of 7, picked by the user (seed key a7032672, the roll had assigned candidate 4, the user chose the pick card).

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
