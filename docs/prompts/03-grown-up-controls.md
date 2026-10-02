# Prompt 03: Grown-up controls (Phase 3, part 1)

Paste this into a fresh Claude Code session at the repo root.

---

You are running part 1 of Phase 3 of `docs/INITIAL_SETUP_PLAN.md` ("widen"). Phase 2 (the thin
slice, commit `4d21d54`) runs one hardcoded routine end to end. This part adds the caregiver's
side of it: the gate, the sheet, pause, the three handover policies, and "More time". It does
**not** add the quick timer's "Something else now", persistence, the real chime or the Sleep-time
fade (those are parts 2 and 3, listed at the end). Do not build ahead of this scope.

## Read first (in order)
1. `CLAUDE.md` — all engineering, testing, UI architecture, design system, module and
   UI-automation rules apply in full.
2. `docs/SPEC.md` §5 (phases), §8 (handover), §9 (gate and sheet), §10 (**only** the "More time"
   bullet). Skip §2–3, §7, §11–13.
3. `docs/DECISIONS.md` — #12 (grown-up controlled handover), #13 (gate gesture), #16 (act on
   release), #20–27 (new: pause/resume, "More time"; only #26 is in scope now).
4. `docs/OPEN_QUESTIONS.md` — product questions 1–6 are answered; read the "Answered" lists so you
   don't re-ask them. Do not guess anything still open; ask.
5. `docs/adr/0002-ui-architecture.md` — the reducer → ViewModel → Compose flow Phase 2 implemented.
   Extend it, don't redesign it.
6. `docs/ARCHITECTURE.md` Timekeeping section — pause must stay derived from timestamps.
7. `prototype/now-next-v2.html` — the behaviour reference, now updated for the latest decisions
   (More time before/after red, hold-to-open gate, "Not yet" hints, pulsing gate dot, sheet
   contents). Read behaviour, don't port code. Its speed menu, demo buttons, side panel and phone
   frame are scaffolding and must not appear (see `CLAUDE.md`).

## What already exists (from Phase 2) — do not redo it
- `core/model`: `Routine`/`Activity`/`FinalActivity`/`ActivityColor`, `DEFAULT_EVENING_ROUTINE`,
  `RunState` (`Active`/`Transition`/`Final`), `Event` (`Tick`, `ChildTap`), `Effect` (`PlayChime`),
  `RunReducer.reduce(state, event, now)`, `progress(...)`, traffic colour and shrink maths.
- `feature/run/impl`: `RunViewModel` (ticks off `Clock`, `StateFlow<RunUiState>`, `Channel` for
  effects), `RunUiState`, `RunRoute` (the only place the ViewModel is referenced), stateless
  `RunScreen`, `RunViewModelFactory`; `app`: `AppContainer`, `MainActivity`.
- `core/designsystem`: `KcScreen` (now with an optional `background: Brush?`), `KcCircle`,
  `KcText`, `KcButton`, `KcTheme` tokens.

Lessons from Phase 2 that apply here:
- `verifySnapshotCoverage` has no opt-out: every file with a public `@Composable` needs a
  `<File>SnapshotTest.kt`, including thin wrappers like `RunRoute`.
- Anything that must be reachable by UiAutomator/Maestro must be a **descendant** of the
  `KcScreen` root, because `testTagsAsResourceId` only reaches descendants. A sibling of
  `KcScreen` is invisible to automation. The sheet and gate must live inside it.
- ViewModel tests with a ticking `viewModelScope`: `runTest` reuses the dispatcher installed with
  `Dispatchers.setMain`, so cancel `viewModel.viewModelScope` before the test ends or `runTest`'s
  final `advanceUntilIdle()` spins forever. See `RunViewModelTest`.

## Scope for this part
**In scope**

1. **Handover policies (SPEC §8).** Add `StartPolicy { ChildTaps, GrownUpUnlocksThenChildTaps,
   GrownUpOnly }` to `Activity` (the policy of *starting* that activity; the first activity has
   none). Set the default routine to SPEC §8's demo values: Tidy up = child taps, Bath = unlock,
   Brush teeth = child taps, Story = grown-up only; Sleep time (final) = grown-up only.
   `ChildTap` in `transition` advances only if the next activity's policy allows it (policy 2
   only once unlocked). Table-test every branch, named `Spec08_...`.
2. **Pause / Resume.** New events; time stays derived from timestamps. Track paused time so that
   elapsed = now − start − paused (including an in-progress pause). No tick-counting. Must stay
   correct across a long pause. Named `Spec05_...`/`Spec09_...`.
3. **"More time" (decision 26).** Caregiver-only event with 5, 10 or 15 minutes. It extends the
   current activity's total. From `active` it just extends. From `transition` it returns the
   activity to `active`. Progress is measured against the new, longer total, so it drops back
   (an 8 min activity plus 5 min is at about 62%, amber). Not available in `final`.
4. **Grown-up gate (SPEC §9).** A small distinct dot, top-left. Press and hold about 1.2 s opens
   the sheet; a ring fills while holding; releasing early cancels. Keyboard/accessibility
   activation opens it immediately. Act on pointer release rules from decision 16 still apply to
   the child's controls. The dot pulses in a warning colour when the grown-up is needed
   (`transition` and the child cannot start the next activity).
5. **Grown-up sheet (SPEC §9), reduced.** A bottom sheet with: "Say together" (a sentence and a
   question using the colour words, e.g. "We are playing. The big circle is getting smaller and
   the screen is green. When it is red, we are out of time."), minutes left ("grown-up only",
   never shown to the child), Pause/Resume, "Let child start next" (only when the next policy is
   unlock and we are in `transition`), "Start <next>", "More time" (5/10/15), Close. Reach the
   wording from `prototype/now-next-v2.html`. Put user-visible strings in string resources.
6. **Early/locked tap hint (SPEC §8).** A tap on the Next tile that cannot start anything shows a
   short line for about 2.5 s: "Not yet. Watch the circle get small." (before `transition`) or
   "Your grown-up will help with this one." (locked). It is a one-shot effect, shown via the
   ViewModel's effect channel, never via `StateFlow`.
7. New `Kc*` blocks as needed (a gate dot with hold ring, a bottom sheet), each with a required
   `testTag` (convention `<screen>.<element>`, e.g. `run.gate`, `run.sheet`, `run.sheet.pause`),
   a `contentDescription` on icon-only controls, and a `<File>SnapshotTest`. Features never use
   Material 3 directly and never hard-code colours or dp values.

**Out of scope — ask before touching:** the quick timer's "Something else now" and its
pause/auto-resume (part 2), the Sleep-time fade to dark (part 2), persistence of any kind
(part 3), the real bell chime (part 3), setup UI, "Back to setup", "Show what's next" (age modes),
media, kiosk/lock-task wiring, non-default colour modes, day strip, reduced-motion handling beyond
what is trivial, any new product decision. If SPEC and the prototype disagree, SPEC wins; tell me.

## Constraints
- The reducer in `core/model` is the only place decisions are made. The ViewModel wraps it; Compose
  only renders. No `core/model` type passed into a `Kc*` block. Effects through a `Channel`.
- Every new rule ships with unit tests in the same change, named after SPEC sections, using
  `FakeClock` and hand-written doubles in `core/model` (no MockK there). Cover boundaries: pause
  at progress 0 and at the end of an activity, "More time" at the exact moment of `transition`,
  "More time" in `final`, policy 2 locked and unlocked, repeated pause/resume.
- Prefer the smallest change. Do not make policies, durations or the hold time configurable yet.
  The hold duration is a named constant (SPEC marks it tunable).
- `./gradlew check` stays green throughout. Do not use `--no-verify`.
- Do not commit or push unless asked (`CLAUDE.md`).

## Done when (verify and show output)
- `./gradlew check` and `:core:model:test` pass.
- `./gradlew :app:installDebug`, then a real run on the device: press-and-hold opens the sheet
  (and an early release does not); Pause freezes the bubble and colour and Resume continues from
  the same point; at the end of Tidy up the child can tap on; Bath only after "Let child start
  next"; Story only from "Start story time"; a premature tap shows the hint; "More time" from the
  red screen returns to amber at about 62% for an 8 min activity plus 5. Use a temporary short
  routine to see this in seconds and **revert it before finishing**. Screenshots of the sheet and
  the hint.
- `adb shell dumpsys package com.kidsclock | grep -i INTERNET` prints nothing, and new elements
  appear by resource-id in `adb shell uiautomator dump` (gate, sheet and its buttons included).
- New snapshot baselines recorded (`./gradlew recordRoborazziDebug`) and **inspected before
  accepting**.
- Final report: what was built; anything skipped; the assumptions you made; and the status of
  `docs/OPEN_QUESTIONS.md` items you touched.

## Parts 2 and 3 (do not start)
- **Part 2:** "Something else now" with pause and 5 s auto-resume (decisions 20, 27; chime, red,
  then back with no interaction; no nesting), the Sleep-time fade to dark over 5 s with no chime
  (decision 23), reduced motion.
- **Part 3:** persistence (`core/data`: routine and the run anchor from spike 02), and the real
  SPEC §7 bell chime replacing the spike beep (decision 19 found it too quiet at everyday volume).
