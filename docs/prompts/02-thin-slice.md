# Prompt 02: Thin vertical slice (Phase 2)

Paste this into a fresh Claude Code session at the repo root.

---

You are running Phase 2 of `docs/INITIAL_SETUP_PLAN.md`: the first thin vertical slice. Groundwork
(Phase 0) and the real-device spikes (Phase 1) are both done. The goal here is one hardcoded
routine running end-to-end on a real activity bubble, driven entirely by `core/model`'s reducer —
not a finished app, not setup UI, not persistence.

## Read first (in order)
1. `CLAUDE.md` — engineering rules, testing rules, UI architecture, module rules all apply in full.
2. `docs/SPEC.md` §1, §4–§8 (run screen anatomy, phases, progress colour, sounds, handover). Skip
   §2–3 (setup, age modes), §9–13 (grown-up sheet, quick timer, media, default routine, out of
   scope) — not this phase.
3. `docs/ARCHITECTURE.md` — module shape, dependency rules, the Timekeeping section, UI
   architecture summary.
4. `docs/adr/0002-ui-architecture.md` — the reducer → ViewModel → Compose data flow this slice
   implements for the first time. Follow it exactly; this prompt does not re-decide it.
5. `docs/DECISIONS.md` — in particular #2 (short horizon), #9 (whole-screen colour), #10 (colour
   never the only cue), #11 (chime, not alarm), #12 (grown-up-controlled handover), #16 (act on
   pointer release), and the three added after the Phase 1 spikes: #17 (kiosk), #18 (boot id), #19
   (chime volume — the spike found the chime is too quiet at everyday media volume; not this
   phase's problem to solve, but don't make it worse).
6. `docs/OPEN_QUESTIONS.md` — items 1 (interrupted-activity resume) and 4 (final-activity signal)
   are directly relevant to phase/reducer design below; both are pre-decided *for this slice only*
   under Scope, so you don't need to ask, but do not generalize that choice into the reducer as if
   it were settled product behaviour.
7. `docs/spikes/*` — Phase 1's findings. Nothing here blocks this phase (kiosk, real audio focus,
   and battery are runtime-integration concerns for a later phase), but `core/platform`'s
   `AndroidClock` and `AlertPlayer` already exist and are what this phase's `app`/`feature/run`
   wiring should use — do not re-invent a clock or a chime player.

## What already exists — do not redo it
- `core/model`: `Clock`/`FakeClock`, `Rgb`/`TrafficColourStops`/`trafficColour(progress)`
  (`TrafficColour.kt`), `progressBubbleScale(progress)`, both already tested
  (`Spec06_TrafficColourTest`, `Spec04_ShrinkScaleTest`). Reuse these; extend only if the reducer
  needs something they don't provide.
- `core/platform`: `AndroidClock` (real `Clock`), `AlertPlayer`/`AndroidAlertPlayer`,
  `LockTaskController` (not needed this phase — no kiosk wiring yet).
- `core/designsystem`: `KidsClockTheme`, `KcTheme` tokens, `KcScreen`, `KcButton`, `KcText`,
  `KcCircle`. Build the run screen from these; add new `Kc*` blocks (with their snapshot tests) only
  for what's genuinely missing (e.g. a layered orb block, if `KcCircle` alone can't express the
  activity-bubble-inside-progress-bubble shape).
- `feature/run/api` (placeholder `RunFeature.ID`), `feature/run/impl` (placeholder `RunScreen`) —
  this phase gives both real content.

## Scope for this slice (say no to everything else)
**One hardcoded routine**, in code, no setup UI: reuse `docs/SPEC.md` §12's default evening
routine (Playtime 8 min → Tidy up 3 → Bath 10 → Brush teeth 3 → Story 8 → Sleep, final). Use
placeholder pictograms/colours per §12 if real art isn't ready — flat coloured circles are fine.

**In scope:**
- `core/model`: a `RunState` sealed type, `Event` (`Tick(now)`, `ChildTap` at minimum), and
  `reduce(state, event, now) -> (RunState, List<Effect>)` covering: `active` → `transition` on
  progress reaching 1 (SPEC §5), the tap-to-advance handover policy only (SPEC §8 policy 1 — skip
  policies 2/3, no grown-up sheet exists yet), and the `final` phase for the last item. `Effect` at
  minimum needs `PlayChime`. Table-test every branch against `docs/SPEC.md` §5/§8, named
  `Spec05_...`/`Spec08_...`.
- **Interrupted-activity resume (OPEN_QUESTIONS #1) and final-activity signal (OPEN_QUESTIONS #4):
  hardcode the simplest option for this slice** (no resume; final activity gets the same
  `transition`-less treatment as SPEC §5 describes, no special fade) **and say so explicitly in the
  report** — do not treat either as decided product behaviour, and do not spend time making them
  configurable yet.
- A `RunViewModel` (manual DI, per ADR 0002) wrapping the reducer: `StateFlow<RunUiState>` +
  a `Channel` for the chime effect, ticking off `AndroidClock`.
- `RunScreen` (Compose, `feature/run/impl`) rendering one activity at a time: activity bubble +
  shrinking progress bubble (SPEC §4), activity name, whole-screen traffic colour (SPEC §6, default
  mode only — skip the other two colour modes), next tile (dimmed, SPEC §4), tap-to-advance in
  `transition`. Skip: day strip, grown-up gate/sheet, media, quick timer, reduced-motion handling
  beyond what's trivial.
- Wire `AndroidAlertPlayer` to the `PlayChime` effect in `app`'s `AppContainer`/`MainActivity`.
- Every new `Kc*` block gets its snapshot test (`docs/DESIGN_SYSTEM.md`); every `Kc*` interactive
  element gets its `testTag` (`docs/UI_AUTOMATION.md`).

**Out of scope — ask before touching:** setup UI, persistence (routine or run-anchor; the anchor
design is proven from Phase 1 but wiring real `core/data` is a later phase), grown-up gate/sheet,
quick timer, media, kiosk/lock-task wiring, non-default colour modes, age modes, day strip, real
audio-focus-aware ducking beyond what `AlertPlayer` already does.

## Constraints
- Every rule in `CLAUDE.md`'s Testing rules section applies in full: reducer logic lives in
  `core/model` with `FakeClock`, hand-written doubles (a `SpyAlertPlayer` or similar) where needed,
  no shared mutable test state, tests named after SPEC sections.
- `./gradlew check` stays green throughout; run it before declaring done.
- Follow ADR 0002's data flow exactly — no `core/model` type passed into a `Kc*` block, no
  `StateFlow` used for the chime effect.
- Smallest change that satisfies scope. Don't build configurability (colour modes, handover
  policies 2/3, resume behaviour) ahead of a real need — that's the premature-abstraction trap
  `docs/adr/0004-third-party-isolation.md` warns about for platform code, and it applies here too.

## Done when (verify and show output)
- `./gradlew check` and `:core:model:test` pass.
- `./gradlew :app:installDebug` + a real run on-device (or the AVD): the default routine plays
  through at least two activities, the progress bubble visibly shrinks, the screen colour shifts
  green → amber → red, tapping in `transition` advances to the next activity, the chime fires at
  time-up (confirm via `AndroidAlertPlayer` being invoked — audibility itself was already spiked).
  Screenshot or short recording.
- New snapshot baselines recorded and committed (`./gradlew recordRoborazziDebug` if needed),
  inspected before accepting.
- Final report: what was built, the two hardcoded/simplified decisions called out above (resume
  behaviour, final-activity signal) restated as still-open, anything skipped, and a proposed prompt
  for Phase 3 (grown-up gate + sheet, handover policies 2/3, quick timer, persistence).
