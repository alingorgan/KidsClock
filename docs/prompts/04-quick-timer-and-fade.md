# Prompt 04: Quick timer and the Sleep-time fade (Phase 3, part 2)

Paste this into a fresh Claude Code session at the repo root.

---

You are running part 2 of Phase 3 of `docs/INITIAL_SETUP_PLAN.md`. Part 1 (commit `2bfc492`) added
the grown-up gate and sheet, start policies, pause and "More time". This part adds the quick timer's
**"Something else now"**, which pauses the current activity and brings it back by itself, and the
**Sleep-time fade to dark**. It does **not** add persistence or the real chime (part 3), setup UI,
or anything in `docs/SPEC.md` §10 beyond what is listed under "In scope".

## Read first (in order)
1. `CLAUDE.md` — all engineering, testing, UI architecture, design system, module and
   UI-automation rules apply in full.
2. `docs/SPEC.md` §5 (phases), §9 (sheet), §10 (quick timer: read the whole section, it was updated
   for the owner's decisions), §8 only for how the start policies work today.
3. `docs/DECISIONS.md` — #15 (quick timer), #20 (pause and auto-resume), #23 (Sleep time: no chime,
   fade), #26 ("More time"), #27 (the 5-second auto-resume).
4. `docs/OPEN_QUESTIONS.md` — product questions 1–6 and their follow-ups are all answered. Do not
   re-ask them and do not guess anything still open: ask.
5. `docs/adr/0002-ui-architecture.md` — the reducer → ViewModel → Compose flow. Extend it, don't
   redesign it.
6. `prototype/now-next-v2.html` — behaviour reference only (`startElse`, `resumeInterrupted`, the
   `fade` overlay). Its speed menu, demo buttons, side panel and phone frame must not appear.

## What already exists (from Phases 2 and 3 part 1) — do not redo it
- `core/model`: `Routine`/`Activity` (with `startPolicy` and `doing`)/`FinalActivity`,
  `StartPolicy`, `DEFAULT_EVENING_ROUTINE`, `RunState` (`Active` with pause and `extraMillis`,
  `Transition`, `Final`), `Event` (`Tick`, `ChildTap`, `Pause`, `Resume`, `UnlockNext`,
  `GrownUpStartNext`, `AddTime`), `Effect` (`PlayChime`, `ShowHint`), `RunReducer`, traffic colour
  and shrink maths.
- `core/designsystem`: `KcScreen` (optional `background: Brush?`), `KcCircle`, `KcText`
  (with `KcTextAlign`), `KcButton`, `KcGate`, `KcSheet`, `KcTheme` tokens.
- `feature/run/impl`: `RunViewModel`, `RunUiState`, `RunActions`, `RunRoute`, stateless
  `RunScreen` with the gate, sheet and Next tile; `app`: `AppContainer`, `MainActivity`.

Lessons that apply here (they cost time before):
- `verifySnapshotCoverage` has no opt-out: every file with a public `@Composable` needs a
  `<File>SnapshotTest.kt`.
- Anything automation must reach has to be a **descendant** of the `KcScreen` root
  (`testTagsAsResourceId` only reaches descendants).
- `verifyAccessibleInteractions` looks for a literal `testTag(`. Controls built from `Kc*` blocks
  (whose `testTag` parameter is required) may use `// kc-a11y-ignore: <reason>` as `RunScreen.kt`
  already does; do not weaken the check.
- ViewModel tests with a ticking `viewModelScope`: `runTest` reuses the dispatcher installed with
  `Dispatchers.setMain`, so cancel `viewModel.viewModelScope` before the test ends.
- Run `./gradlew ktlintFormat` before `check`.

## Scope for this part
**In scope**

1. **"Something else now" (SPEC §10, decisions 20, 27).** The grown-up picks a preset (Playground,
   Outside time, Free play, Snack time) and minutes (5, 10, 15, 20, 30) in the sheet and starts it.
   - The quick timer is an extra activity that runs next. **Do not mutate the `Routine`**; keep
     the interrupted activity's state alongside the run state.
   - From `active`: the current activity is **paused** with its remaining time. Reuse the pause
     mechanics (an interrupted activity is an `Active` with a pause started at the moment of
     interruption) so progress and minutes left stay correct.
   - From `transition` (red already): nothing is paused, there is nothing left to resume. It just
     runs, and the routine then hands over as usual (decision 20, "for now").
   - **No nesting:** while a quick timer is running, "Something else now" is not offered, and the
     reducer ignores the event.
   - When the quick timer's time is up: the chime plays and the screen goes red (`transition`),
     as for any activity. **5 seconds later the interrupted activity resumes by itself, with no
     interaction**, running, from where it left off. The 5 seconds is derived from a stored
     timestamp (like all time in this app), checked on `Tick`; no tick-counting or sleeps. The
     handover policy (SPEC §8) does not apply to the resumed activity, it was already started.
   - While a quick timer runs, the Next tile shows the **interrupted** activity (it comes back
     next), not the routine's next one. During the 5-second wait the line under the name reads
     "All done! Back to <activity> in a moment.", the gate dot does **not** pulse, and a child tap
     does nothing and shows no hint.
   - "Start <next>" in the sheet during a quick timer, or during the 5-second wait, resumes the
     interrupted activity immediately ("Back to <activity> now").
   - "More time" works on a quick timer, and from its red screen it returns the quick timer to
     `active` and cancels the pending auto-resume.
   - Quick-timer activities get a "We are ..." phrase and a colour: playground "at the playground",
     outside "playing outside", free play "playing freely", snack "having a snack"; colours from the
     prototype (`#C2468B`, `#2E9BB3`, `#D98214`, `#A8742F`) as **placeholders**. There are no
     pictograms yet (flat coloured circles, as in Phase 2). Add the colours as design-system tokens;
     `core/model` keeps only names.
2. **Sleep-time fade (decision 23).** Entering "Sleep time" starts a slow fade of the whole screen
   to near-black over **5 seconds**, immediately, with **no chime**. The "Goodnight" text turns
   light as the screen darkens. This is specific to the Sleep-time final item (add a flag on
   `FinalActivity`; do not hard-code the name). Use Compose's standard animation APIs: they
   already collapse to instant when the system animator scale is 0, which is all the reduced-motion
   handling this part needs. The gate stays visible and usable on the dark screen.
3. New `Kc*` blocks as needed, each with a required `testTag` (`<screen>.<element>`, e.g.
   `run.sheet.else.playground`, `run.sheet.else.minutes.5`), a `contentDescription` on icon-only
   controls and a `<File>SnapshotTest`. Features never use Material 3 directly and never hard-code
   colours or dp values. User-visible text goes in string resources.

**Out of scope — ask before touching:** persistence of any kind, the real bell chime, setup UI,
"Quick timer only" (no routine), custom names or photos (decision 21: presets only), pictograms,
media, kiosk wiring, the "nearly done" note and breathing, the day strip, any new product decision.
If SPEC and the prototype disagree, SPEC wins; tell me.

## Constraints
- The reducer in `core/model` is the only place decisions are made. The ViewModel wraps it;
  Compose only renders. No `core/model` type passed into a `Kc*` block.
- Every new rule ships with unit tests in the same change, named after SPEC sections
  (`Spec10_...`, `Spec05_...`). `FakeClock` and hand-written doubles in `core/model`, no MockK
  there. Cover boundaries: interrupting at progress 0, at progress just under 1 and while already
  paused; the auto-resume at 4 999 ms, 5 000 ms and much later; interruption from `transition`;
  nesting refused; invalid minutes refused; "More time" on a quick timer during the wait;
  "Start next" during a quick timer and during the wait; the resumed activity's remaining time
  equal to what it had; the Next tile pointing at the interrupted activity; Sleep time marked to
  fade and a normal final item not.
- Smallest change. Do not make the 5 s, the fade length or the preset list configurable.
- `./gradlew check` stays green throughout. Do not use `--no-verify`. Do not commit or push unless
  asked.

## Done when (verify and show output)
- `./gradlew check` and `:core:model:test` pass.
- `./gradlew :app:installDebug`, then a real run on a device or the emulator: start a quick timer
  mid-activity and see the interrupted activity paused (minutes left unchanged afterwards); when the
  quick timer ends hear the chime (or confirm `AlertPlayer` was invoked), see red, and 5 seconds
  later see the interrupted activity back by itself with its progress preserved; try "More time"
  on the quick timer's red screen; try starting a second quick timer during one (not offered); reach
  Sleep time and see it fade to dark over about 5 seconds with no chime. The smallest quick-timer
  length is 5 minutes, so use a **temporary** scale (for example minutes treated as seconds) and
  **revert it before finishing**. Screenshots of the sheet section and the faded screen.
- `adb shell dumpsys package com.kidsclock | grep -i INTERNET` prints nothing; the new controls
  appear by resource-id in `adb shell uiautomator dump`.
- New snapshot baselines recorded (`./gradlew recordRoborazziDebug`) and **inspected before
  accepting**.
- Final report: what was built; assumptions you made (state them, in particular: the resumed
  activity runs, even if the grown-up had paused it before the interruption); anything skipped; any
  `docs/OPEN_QUESTIONS.md` items touched.

## Part 3 (do not start)
Persistence (`core/data`: the routine and the run anchor from `docs/spikes/02-timekeeping.md`,
including the quick timer and an interrupted activity surviving a process kill) and the real
SPEC §7 bell chime replacing the spike beep (decision 19 found it too quiet at everyday volume).
