---
name: GymGante
description: A lifter's logbook on graph paper; every workout is a page and every set is a box you strike through with a red pen.
colors:
  paper: "#f4f8fc"
  paper-2: "#ebf1f8"
  grid: "#d9e4ef"
  grid-strong: "#c3d3e4"
  ink: "#17284a"
  ink-2: "#4a5b78"
  red-pen: "#c8322a"
  red-ink: "#a52620"
  red-wash: "#f0e4e7"
typography:
  display:
    fontFamily: "Archivo, Arial Narrow, sans-serif"
    fontSize: "clamp(2.6rem, 6.4vw, 5.25rem)"
    fontWeight: 800
    lineHeight: 1.02
    letterSpacing: "-0.012em"
    fontVariation: "'wdth' 78"
  headline:
    fontFamily: "Archivo, Arial Narrow, sans-serif"
    fontSize: "clamp(2rem, 4.4vw, 3.4rem)"
    fontWeight: 800
    lineHeight: 1.02
    letterSpacing: "-0.012em"
    fontVariation: "'wdth' 78"
  title:
    fontFamily: "Archivo, Arial Narrow, sans-serif"
    fontSize: "clamp(1.35rem, 2.2vw, 1.75rem)"
    fontWeight: 800
    lineHeight: 1.1
    fontVariation: "'wdth' 78"
  body:
    fontFamily: "Atkinson Hyperlegible, Segoe UI, sans-serif"
    fontSize: "1.0625rem"
    fontWeight: 400
    lineHeight: 1.55
  label:
    fontFamily: "Atkinson Hyperlegible, Segoe UI, sans-serif"
    fontSize: "0.95rem"
    fontWeight: 700
    lineHeight: 1.3
  annotation:
    fontFamily: "Kalam, Segoe Print, cursive"
    fontSize: "1.4rem"
    fontWeight: 400
    lineHeight: 1.2
rounded:
  base: "3px"
  box: "2px"
spacing:
  cell: "24px"
  gutter: "28px"
  margin: "64px"
  margin-mobile: "18px"
components:
  button-primary:
    backgroundColor: "{colors.red-pen}"
    textColor: "#ffffff"
    typography: "{typography.title}"
    rounded: "{rounded.base}"
    padding: "12px 22px"
    height: "48px"
  button-primary-hover:
    backgroundColor: "{colors.ink}"
    textColor: "#ffffff"
  button-ghost:
    backgroundColor: "transparent"
    textColor: "{colors.ink}"
    rounded: "{rounded.base}"
    padding: "8px 16px"
    height: "42px"
  button-ghost-hover:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.paper}"
  set-box:
    backgroundColor: "{colors.paper}"
    rounded: "{rounded.box}"
    size: "28px"
  input:
    backgroundColor: "{colors.paper}"
    textColor: "{colors.ink}"
    rounded: "{rounded.base}"
    padding: "10px 12px"
    height: "48px"
  input-invalid:
    backgroundColor: "{colors.red-wash}"
  folha:
    backgroundColor: "{colors.paper}"
    rounded: "{rounded.base}"
---

# Design System: GymGante

## Overview

**Creative North Star: "Caderno de Treino"**

The product is a lifter's logbook on graph paper. The whole app (landing, sign-up, questionnaire, today's workout, full plan) is one ruled page: a cool blue-white sheet with a faint grid, navy ink for everything that is written, and a single red pen for everything that is done, chosen or primary. A set is a 1.5px ink box; ticking it draws a red check, and a finished exercise is struck through with a red line. Structure comes from ruled lines, not from boxes and shadows.

Density is calm and left-aligned. A red margin rule runs down the page at 64px (18px on phones); content sits to its right. Handwriting (Kalam) appears only where a person would actually annotate: the three numbered questions and short notes. Everything else is condensed heavy Archivo for headings and Atkinson Hyperlegible for reading and numbers.

The system is code-led: no raster assets, all pictograms and marks are authored SVG, all fonts are self-hosted.

**Key Characteristics:**
- Graph-paper ground, 24px minor grid with a stronger line every 120px.
- One red pen (#c8322a) for strikes, checks, the margin rule, focus and the primary action.
- Ruled lines and 1.5px ink outlines instead of cards; shadow is reserved for three "loose sheet" cases.
- 3px radius everywhere (2px on set boxes), no pills except the circled question numeral.
- Ticking a set is the signature moment: a red pen stroke drawn left to right.

### Deliberate deviations from the original direction contract

These were chosen during the build and are intended, not drift.
1. **Grid is 24px, strong line every 120px** (contract: 8px). Chosen for legibility so text stays readable over the lines.
2. **Red pen is #c8322a** (contract: #d3392e). Darker to reach 5:1 contrast with white button text; **red-ink #a52620** is the variant for small red text on paper.
3. **Secondary text is ink-2 #4a5b78** (contract: graphite #6b7a8f), about 6:1 on paper.
4. **Questions are lettered in Kalam as numbered legends with a circled numeral**, while option labels stay Atkinson bold. The landing questionnaire and anamnese.html share one tick-box radio vocabulary (`fieldset.ask > .choices > label.choice`).
5. **The red margin rule is an SVG background on the page container**, not a pseudo-element, so contrast tooling does not treat it as a text background.
6. **Plan days are ruled sections** (4px ink rule on top, no box, no shadow).
7. **Pictograms are 12 authored SVG symbols** (see Components).

## Colors

A cool blue-white paper, navy ink, and exactly one warm accent.

### Primary
- **Red Pen** (#c8322a): the only accent. Primary button fill, set-box check, exercise strike-through, margin rule (at 50% opacity), focus outline, text selection, progress ruler fill, invalid-field border. Used sparingly so that red always means "done, chosen or act here".
- **Red Ink** (#a52620): red for small text on paper (exercise notes, missed-day state, `.hand--red`). Never used as a fill.

### Neutral
- **Paper** (#f4f8fc): page ground, and the fill of every box, input and sheet.
- **Paper Shade** (#ebf1f8): scrollbar track and completed-day background in the weekly strip.
- **Grid** (#d9e4ef): minor graph lines (24px).
- **Grid Strong** (#c3d3e4): major graph lines (120px) and hairline row dividers inside lists.
- **Ink** (#17284a): all primary text, all outlines, rules and icons.
- **Ink 2** (#4a5b78): secondary text, labels, hints, placeholders.
- **Red Wash** (#f0e4e7): red at 10% over paper as a solid color. "Today" column, pressed exercise row, highlighter swipe under chosen options and the safety `mark`, invalid-field background.

### Named Rules
**The One Pen Rule.** There is one accent and it is red. No second hue, no gradient, no tints other than Red Wash. If something is blue it is ink.

**The Solid Wash Rule.** Red Wash is a solid color, never an alpha overlay, so contrast against text is computed against a real background.

## Typography

**Display Font:** Archivo variable (width axis 78%, weight 800), fallback Arial Narrow
**Body Font:** Atkinson Hyperlegible 400 and 700, fallback Segoe UI
**Annotation Font:** Kalam 400, fallback Segoe Print

**Character:** Condensed heavy headlines read like block capitals on a notebook cover; Atkinson keeps numbers and instructions unambiguous between sets; Kalam is the lifter's own pen.

### Hierarchy
- **Display** (800, width 78%, clamp(2.6rem, 6.4vw, 5.25rem), 1.02): h1. Landing hero uses clamp(2.5rem, 4.9vw, 4.2rem); dashboard greeting clamp(2.2rem, 5vw, 3.6rem).
- **Headline** (800, width 78%, clamp(2rem, 4.4vw, 3.4rem), 1.02): h2 sections; panel and day headings are set smaller (1.6rem to 2.6rem).
- **Title** (800, width 78%, clamp(1.35rem, 2.2vw, 1.75rem), 1.1): h3; buttons use Archivo at width 88%, 1.15rem; stat values use width 84%.
- **Body** (400, 1.0625rem, 1.55): paragraphs capped at 62ch; `.lede` is ink-2, up to 1.3rem, 46ch.
- **Label** (700, 0.95rem): field labels, option labels, exercise names, nav links. Detail lines are 0.92rem ink-2 with tabular numerals.
- **Annotation** (Kalam 400, 1.4rem question legends, 1.2rem `.hand` notes rotated -1.2deg): handwriting, with a function every time.

### Named Rules
**The Handwriting Has A Job Rule.** Kalam is for the numbered questions and short human notes only. It never sets headings, buttons, labels or body text.

**The Tabular Numbers Rule.** Sets, reps, loads, dates and counts use `font-variant-numeric: tabular-nums`.

## Layout

The page container is a single left-aligned column, max 1280px, centered, with the red margin rule at 64px from its left edge. Inline padding is margin + gutter (64 + 28 = 92px on desktop on the left, 28px on the right). At 720px and below, margin becomes 18px and gutter 16px (left padding 36px). Body copy never exceeds 62ch.

The grid unit is 24px (`--cell`); ruled sections are separated by 1.5px ink rules and generous block padding (clamp 56px to 120px on the landing). Compositions: the landing hero is 1.65fr text / 1fr sample page; the dashboard is 1.6fr today / 1fr side; the plan is two columns of day sections; auth screens are text plus a 28rem sheet. All collapse to one column at 960px (860px for auth screens). The 7-column weekly strip becomes a stacked list of rows at 720px. Header is 72px (64px mobile), with `scroll-padding-top` set for the sticky anchor offset.

## Elevation & Depth

Flat by default. Depth is conveyed by ruled lines and ink outlines, not by shadow or blur. Only a "loose sheet of paper" may lift off the page.

### Shadow Vocabulary
- **Sheet** (`box-shadow: 0 1px 0 rgba(23,40,74,0.18), 0 18px 28px -18px rgba(23,40,74,0.4)`): `.folha` only. Used on the landing sample page (also tilted 0.6deg on desktop), the dashboard "today" page, and forms/sheets (auth, modal content).

### Named Rules
**The Three Sheets Rule.** Shadow belongs to the landing sample page, the dashboard today page and forms. Plan days, lists, weekly strip, buttons and inputs get none.

**The No Blur Rule.** No decorative blur, glow or backdrop filter. The only translucent layers are the paper-colored overlays behind the modal and loading screen (rgba of paper, 92 to 95%).

## Shapes

Small, nearly square corners: 3px radius on buttons, inputs, sheets and the weekly strip, 2px on set boxes and the progress ruler. The only circle is the 32px numeral on a question. Outlines are 1.5px solid ink (`--line`); inner list dividers are 1px Grid Strong; major section rules are 1.5px ink; each plan day opens with a 4px ink rule. Authored marks are intentionally irregular, like a pen: the red check inside a set box is a hand-drawn SVG path (data URI), and the exercise strike-through is a slightly wavy variable-thickness SVG stroke.

## Components

### Buttons
- **Shape:** 3px radius, 1.5px ink border, min height 48px, Archivo 800 at width 88%, 1.15rem.
- **Primary (`.btn .btn-primary`):** Red Pen fill, white text (5:1). Hover (pointer devices only) and active turn the fill to Ink.
- **Ghost (`.btn--ghost`):** transparent, ink text and border, 42px high, 1rem. Hover and active invert to Ink fill and Paper text.
- **Press:** `transform: scale(0.97)` on active; disabled is 55% opacity.

### Set box (`.box`)
28px square (36px `--lg`), 1.5px ink border, 2px radius, Paper fill. When pressed (`aria-pressed="true"` or a checked input) a red check, drawn as an irregular SVG, is revealed left to right with `clip-path` (90ms in, 200ms to draw). Hit area is expanded to 44px with a `::before` inset of -9px on `button.box`. Active scale 0.92.

### Choice and questions (`.ask`, `.choices`, `.choice`)
A `fieldset.ask` holds a Kalam legend (1.4rem) with a circled numeral `.ask-n` (32px, 1.5px ink circle), and a wrapping row of `label.choice` (min height 44px, 700 weight) each with a visually hidden input and a `.box`. Choosing an option draws a Red Wash highlighter swipe under its text (background-size animates 240ms). Shared by the landing and anamnese. `.choice--check` is the multi-line checkbox variant for forms.

### Fields (`.field`)
Label 700 0.95rem above; optional hint 0.9rem ink-2. Input and select: 48px high, 1.5px ink border, 3px radius, Paper fill, 16px text (never smaller, to avoid iOS zoom). Focus: 2.5px Red Pen outline, 2px offset. Invalid: Red Pen border and Red Wash fill. Select has an ink chevron data-URI.

### Sheet (`.folha`)
Paper fill, 1.5px ink border, 3px radius, Sheet shadow. Padding clamp(18px to 32px). See Elevation for where it may appear.

### Weekly strip (`.semana-grid` / `.dia-col`)
Seven columns inside one 1.5px ink outline, columns separated by 1px Grid Strong lines. Each column: day abbreviation in Archivo, name, muscle group in ink-2, and a set box at the bottom. "Today" is Red Wash; completed is Paper Shade; rest days are ink-2 with a bed glyph; missed state text is Red Ink. Stacks to rows on phones.

### Exercise rows (`.ex`) and plan sections
Dashboard: a grid of [box, text, pictogram], min height 72px, 1px Grid Strong divider, entire row is the tap target; done rows turn the name ink-2 and strike it with the red stroke (scaleX 240ms). Details are 0.92rem ink-2, notes are Red Ink. A progress ruler (10px, 1.5px ink outline, red fill via scaleX) shows completion. Plan page: each day is a ruled section with a 4px ink rule on top, a heading row with a 1.5px ink underline, and lightly ruled exercise rows; no box, no shadow.

### Ruled lists (`.rol`, `.resumo-linha`, `.historico`)
Summary data is shown as lines on the page: a 1.5px ink top rule, rows with 1px Grid Strong dividers, labels in ink-2 and values in Archivo 800 (width 84%). `.resumo-linha` is a single flex line of term/value pairs with a 1.5px ink bottom rule. These replace metric tiles.

### Pictograms (`img/sprite.svg`, `js/muscle.js`)
12 authored SVG symbols: 11 muscle groups (peito, ombros, biceps, core, quadriceps, panturrilha, costas, trapezio, triceps, gluteos, posterior) plus cardio. Each is a front or back figure with the target region in red. Used in the day header (plan and today) and beside each dashboard exercise row, not repeated per row on the plan page. 32 to 36px, ink. Hidden on phones in the headers. Utility symbols (logo-mark, check, arrow, back, bed) share the sprite at one stroke weight.

### Navigation
72px header (64px mobile): tally-mark logo and Archivo wordmark left, links right in 700 weight. A 2px Red Pen underline scales in under the current page (`aria-current`) and on hover for pointer devices only.

### Loading
A 150px by 4px red bar that writes itself left to right then erases, over a paper-colored overlay.

### Motion
- Ease: `--ease-out: cubic-bezier(0.23, 1, 0.32, 1)` for nearly everything; `--ease-in-out: cubic-bezier(0.77, 0, 0.175, 1)` for the loading stroke.
- One entrance only: the landing first fold rises 10px and fades in over 480ms, staggered 70ms per item (`--i`). Lower sections reveal once (14px, 520ms) with JS.
- Feedback: tick draws via clip-path, strike scales in, press is scale .97 (buttons) or .92 (boxes), 120 to 140ms.
- `prefers-reduced-motion: reduce` collapses all animation and transition durations to near zero and disables smooth scroll.

### Touch
Hit areas are at least 44px (boxes expanded by `::before`, choices 44px min, buttons 48px). Inputs are 16px. Hover effects are gated by `(hover: hover) and (pointer: fine)`; `:active` states carry the feedback on touch. `touch-action: manipulation` on tappables. Focus is a 2.5px Red Pen outline, 3px offset.

## Do's and Don'ts

### Do:
- **Do** express structure with ruled lines: 1.5px ink for outlines and section rules, 1px Grid Strong for row dividers.
- **Do** use Red Pen only for what is done, chosen, focused or the single primary action per screen; use Red Ink for small red text.
- **Do** keep radius at 3px (2px on boxes) and borders at 1.5px ink.
- **Do** keep headings in Archivo 800 at width 78% and body in Atkinson; keep inputs at 16px or more.
- **Do** make every tappable target 44px or larger and gate hover behind `(hover: hover) and (pointer: fine)`.
- **Do** honor `prefers-reduced-motion`.
- **Do** author new pictograms as SVG symbols in the sprite, in the same front/back figure with a red target region.
- **Do** show only real content; the sample plan is labeled as an example.

### Don't:
- **Don't** put an eyebrow, kicker or small label above a heading.
- **Don't** use emoji or glyph characters as icons; use the authored SVG sprite.
- **Don't** nest cards inside cards, and don't box plan days, lists or summaries; use rules.
- **Don't** use gradient text, decorative blur, glow, or any shadow outside the three sheet cases.
- **Don't** add a second accent color, or an alpha-tinted wash over text.
- **Don't** invent prices, testimonials, user counts or metric tiles.
- **Don't** set Kalam on headings, buttons or running text.
- **Don't** use a dark theme, a hero card, a badge row or a scroll cue; this is a paper world.
