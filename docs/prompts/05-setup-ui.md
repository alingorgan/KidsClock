# Prompt 05: Setup UI, first slice (Phase 3, part 3)

Paste this into a fresh Claude Code session at the repo root.

---

You are running the next slice of Phase 3 of `docs/INITIAL_SETUP_PLAN.md` ("widen"). So far the app runs
one hardcoded routine (`DEFAULT_EVENING_ROUTINE`) with the gate and sheet, start policies, pause,
"More time", the quick timer ("Something else now"), the Sleep-time fade, the real synthesised chime,
the nearly-done note, breathing, repeat mode and reduced motion. A debug-only "KidsClock fast" launcher
makes a minute last one second. Decision 31 means a *run* is not persisted (a kill or reboot restarts the
routine); decision 25 means the **routine itself persists**. This slice adds the caregiver's **setup
screen**: choosing and editing the routine, and the sound options. It does **not** add media, the age
setting, or the other progress-colour modes unless I say so in answer to the questions below.

## Read first (in order)
1. `CLAUDE.md` — all engineering, testing, UI architecture, design system, module and UI-automation
   rules apply in full. Features never use Material 3 directly; shared UI goes in `core/designsystem`.
2. `docs/SPEC.md` §2 (modes), §3, §6, §7, §8, §9, §12, §13.
3. `docs/DECISIONS.md` — #12 (handover policy), #21–#25, #30–#32.
4. `docs/OPEN_QUESTIONS.md` — do not guess anything still open; ask.
5. `docs/adr/0002-ui-architecture.md`, `0003-module-boundaries.md`, `0004-third-party-isolation.md`,
   `docs/DESIGN_SYSTEM.md`, `docs/UI_AUTOMATION.md`.
6. `prototype/now-next-v2.html` — behaviour reference only (its setup panel is the nearest thing to a
   design; do not translate it line by line; its demo scaffolding must not appear).

## What already exists — do not redo it
- `core/model`: `Routine` (`activities`, `final`, `minuteMillis`, `sound: SoundSettings`), `Activity`,
  `FinalActivity` (with `fadesToDark`), `StartPolicy`, `ActivityColor`, `QuickTimerPreset`,
  `SoundSettings`/`ChimeMode`, `eveningRoutine(minuteMillis)`, `RunReducer`, `ChimeSynth`.
- `core/designsystem`: `KcScreen`, `KcText`, `KcButton`, `KcChoice`, `KcSheet`, `KcGate`, `KcCircle`,
  `KcBreathing`, `rememberKcFade`, `KcTheme` tokens (including `reduceMotion`).
- `core/platform`: `AlertPlayer` (chime and nearly-done note), `AndroidClock`, `LockTaskController`.
- `feature/run/{api,impl}`, `app` (`AppContainer`, `MainActivity`, the fast launcher alias).
- Scaffold a new feature with `scripts/new-feature.sh setup` (both modules, registered, baselines
  recorded). It never wires the feature into `app`: that wiring is part of this slice.

Lessons that apply (they cost time before):
- `verifySnapshotCoverage` has no opt-out: every file with a public `@Composable` needs a
  `<File>SnapshotTest.kt`. `verifyAccessibleInteractions` wants a literal `testTag(` on interactive
  composables; `Kc*`-only controls may use `// kc-a11y-ignore: <reason>` as `RunScreen.kt` does.
- Anything automation must reach has to be a **descendant** of the `KcScreen` root.
- With infinite animations in snapshot tests, set `compose.mainClock.autoAdvance = false` first.
- Capturing a snapshot idles the clock, so a "halfway through an animation" baseline is not possible.
- Import order is enforced by ktlint (`./gradlew ktlintFormat` before `check`).
- With two devices attached, install with `ANDROID_SERIAL=<serial> ./gradlew :app:installDebug`.

## Questions — ask me before building, then record the answers in `docs/DECISIONS.md`
1. **Where does setup live?** My leaning: the app opens on the Run screen as now, and setup is reached
   from the grown-up sheet ("Edit routine"), so the child never lands in it. Alternatives: a launch
   screen, or both. And what happens to a run in progress when the routine is saved (my leaning:
   the routine restarts from the beginning, consistent with decision 31).
2. **What is an activity in setup?** (a) Pick from presets with fixed names, colours and, later,
   pictograms; (b) a free-text name plus one of the six SPEC §12 colours. (b) needs a text field block and
   a "We are ..." phrase (today `doing`). My leaning: (b) with the phrase defaulting to the lower-cased
   name, but ask.
3. **Scope of this slice.** My proposal: edit the list (add, remove, reorder, up to a limit), each
   activity's duration and start policy, the final item, and the sound options (SPEC §7: gentle,
   repeating, none; nearly-done note). **Out:** age setting and the under-3 behaviour, the two other
   progress-colour modes, photos and clips, the day strip, the kiosk "App pinning" setup step
   (decision 17), TalkBack polish. Confirm or change.
4. **Limits and steps.** Duration range and step (my leaning: whole minutes, 1–60, step 1), maximum
   number of activities (my leaning: 8), the first activity has no start policy (SPEC §8), the final item's
   choices (Sleep time with the fade, or "All done!" with no fade).
5. **Storage.** The routine is small. A library (DataStore, kotlinx.serialization) or a hand-written
   mapping over `SharedPreferences`? Give your recommendation and wait for a yes before adding any
   dependency. My leaning: no new dependency. Store **whole minutes**, not milliseconds, so the fast
   launcher still scales them via `minuteMillis`.
6. **"Quick timer only"** (SPEC §10: no routine, pick an activity and minutes, then an "All done!" check
   mark the child can tap). In this slice or the next? My leaning: next.

## Scope (assuming my leanings; adjust to my answers)
**In scope**
1. **Pure editing logic in `core/model`.** An editable routine draft with operations (add, remove, move,
   rename, set duration, set policy, set colour, set final item, set sound) and validation (at least one
   activity, limits, non-blank names), each returning a new draft. Mapping between the draft and the stored
   form, with a **version number** so a newer or corrupt record is discarded and the default routine is
   used. No Android imports. Time and storage come in through interfaces we own.
2. **`core/data`**: a new module (convention plugins; `verifyDependencyRules` must cover it) with a
   `RoutineStore` interface and one Android implementation; a hand-written fake beside the interface.
   Writes are off the main thread. A missing, corrupt or newer-version record never crashes: it falls
   back to `DEFAULT_EVENING_ROUTINE`.
3. **`feature/setup/{api,impl}`**: a `SetupViewModel` over the pure editor (state in, one `StateFlow`,
   one-shot effects via a `Channel`), a stateless `SetupScreen`, `Kc*` blocks as needed (list row, number
   stepper, switch or checkbox, text field), each with a required `testTag` (`setup.<element>`),
   `contentDescription` on icon-only controls and a `<File>SnapshotTest`.
4. **Wiring in `app`**: reachable from the grown-up sheet; Save returns to Run with the new routine, run
   restarted; Cancel returns without change. `RunViewModelFactory` takes the stored routine (still scaled
   by the fast launcher).
5. User-visible text in string resources. No hard-coded colours or dp values in features.

**Out of scope — ask before touching:** anything listed under Out in question 3, any change to run
behaviour, the reducer's rules, the chime or fade, persisting a run, quiet hours or volume policy.

## Constraints
- The reducer in `core/model` stays the only place run decisions are made. The editor is a second pure
  unit; the ViewModel only wraps it. No `core/model` type passed into a `Kc*` block.
- Every new rule ships with tests named after SPEC sections (`Spec02_...`, `Spec08_...`, `Spec12_...`).
  Cover: every editing operation and its boundaries (min and max activities, min and max minutes, blank
  names, moving the first and last), validation, the mapping both ways, a version mismatch, corrupt data,
  a store that throws, and that Save with an invalid draft is refused.
- UI is snapshot-tested: light and dark, and empty, default, full and error states. Inspect every new
  PNG before accepting. Always include snapshot PNGs in a commit.
- Keep the screen awake and the app privacy-clean: no network permission
  (`verifyNoInternetPermission`), nothing leaves the device.
- Smallest change. `./gradlew check` stays green throughout. Never `--no-verify`. Do not commit or push
  unless asked.

## Done when (verify and show output)
- `./gradlew check` and `:core:model:test` pass; `verifyDependencyRules` covers `core/data` and
  `feature/setup`.
- On the **phone** (`adb devices` for the serial), from the normal and the fast launcher:
  open the sheet, edit the routine (rename, change a duration, change a start policy, reorder, add and
  remove), Save, and see the Run screen use it; force-stop and relaunch (`adb shell am force-stop
  com.kidsclock`): the **routine** is kept and the run restarts from the beginning; Cancel changes
  nothing; change the sound option to "repeats every 20 s" and hear it repeat; an invalid draft cannot be
  saved. Screenshots of the setup screen and of one error state.
- `adb shell dumpsys package com.kidsclock | grep -i INTERNET` prints nothing; the new controls appear by
  resource-id in `adb shell uiautomator dump`.
- Final report: what was built; the answers to the questions; assumptions; anything skipped; which
  `docs/OPEN_QUESTIONS.md` items were touched.

## Next (do not start)
Age setting and the under-3 behaviour, the other progress-colour modes, "Quick timer only", then photos
and clips, then the accessibility pass (TalkBack, contrast) and testing with a child.
