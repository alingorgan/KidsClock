# Prompt 05: Routines library and editor, first slice (Phase 3, part 3)

Paste this into a fresh Claude Code session at the repo root.

---

You are running the next slice of Phase 3 of `docs/INITIAL_SETUP_PLAN.md` ("widen"). So far the app runs
one hardcoded routine (`DEFAULT_EVENING_ROUTINE`) with the gate and sheet, start policies, pause,
"More time", the quick timer ("Something else now"), the Sleep-time fade, the real synthesised chime,
the nearly-done note, breathing, repeat mode and reduced motion. A debug-only "KidsClock fast" launcher
makes a minute last one second. Decision 31 means a *run* is not persisted (a kill or reboot restarts the
routine); decision 25 means the **routine itself persists**. This slice adds the caregiver's **setup
screen**: choosing, previewing and editing **saved routines**, and the sound options. It does **not** add media, the age
setting, or the other progress-colour modes unless I say so in answer to the questions below.

## Read first (in order)
1. `CLAUDE.md` — all engineering, testing, UI architecture, design system, module and UI-automation
   rules apply in full. Features never use Material 3 directly; shared UI goes in `core/designsystem`.
2. `docs/SPEC.md` §2 (modes), §3, §6, §7, §8, §9, §12, §13.
3. `docs/DECISIONS.md` — #12 (handover policy), #21–#25, #30–#32.
4. `docs/OPEN_QUESTIONS.md` — do not guess anything still open; ask.
5. `docs/adr/0002-ui-architecture.md`, `0003-module-boundaries.md`, `0004-third-party-isolation.md`,
   `docs/DESIGN_SYSTEM.md`, `docs/UI_AUTOMATION.md`.
6. `prototype/now-next-v3.html` — behaviour reference for the library, preview and editor (do not
   translate it line by line; its demo helpers, side panel and tick timer must not appear).
   `now-next-v2.html` is the older single-routine version.

## What already exists — do not redo it
- `core/model`: `Routine` (`activities`, `final`, `minuteMillis`, `sound: SoundSettings`), `Activity`,
  `FinalActivity` (with `fadesToDark`), `StartPolicy`, `ActivityColor`, `QuickTimerPreset`,
  `SoundSettings`/`ChimeMode`, `eveningRoutine(minuteMillis)`, `RunReducer`, `ChimeSynth`.
- `core/designsystem`: `KcScreen`, `KcText`, `KcButton`, `KcChoice`, `KcSheet`, `KcGate`, `KcCircle`,
  `KcBreathing`, `rememberKcFade`, `KcTheme` tokens (including `reduceMotion`).
- `core/platform`: `AlertPlayer` (chime and nearly-done note), `AndroidClock`, `LockTaskController`.
- `feature/run/{api,impl}`, `app` (`AppContainer`, `MainActivity`, the fast launcher alias).
- Scaffold new features with `scripts/new-feature.sh routines` (library, preview) and, if it keeps the
  modules small, `setup` (editor); decide and say which (both modules, registered, baselines
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

## Answered (owner, 2026-10-03; recorded as decisions 33-36). Do not re-ask.
The design changed from one setup screen to a **library of saved routines**, prototyped first in
`prototype/now-next-v3.html` (open it; it is the behaviour reference for this slice).
1. **Entry point.** The app opens on the **Routines list** (always with "+ New routine"). Tap a routine
   for a read-only **preview** (activities, minutes, start policy, last screen, sound) with Start, Edit,
   Duplicate, Back. Edit is its own screen (Save/Cancel, Delete asks twice). Starting a routine starts
   the run from the beginning; "Back to routines" in the sheet leaves it (decision 31: runs are not persisted).
2. **Activity** = free-text name + one pictogram from a built-in set + one of the six colours + optional
   "We are ..." phrase (falls back to "Now it is <name>").
3. **Scope.** In: many routines (create, preview, edit, duplicate, delete), up to 8 activities (add,
   remove, reorder), minutes 1-60 step 1, start policy (not on the first), per-routine last screen
   ("All done!" or "Sleep time" with the fade) and per-routine sound (SPEC §7). Age and progress colour are
   app-wide, **shown on the list but not built here** beyond what already exists. Out: age behaviour,
   other colour modes, photos/clips, day strip changes, "App pinning" setup step, TalkBack polish.
4. **Limits.** As in the prototype: 8 activities, 1-60 min, names up to 30 characters, non-blank; routine
   needs a name and at least one activity.
5. **Storage.** Hand-written `SharedPreferences`, no new dependency, behind a `RoutineStore` interface in
   a new `core/data` module. A versioned record holding **all routines** and the selected-nothing state;
   whole minutes. Missing, corrupt, newer-version or invalid entries are dropped; if none survive, the
   two example routines (SPEC §12: Evening, Morning) are used.
6. **"Quick timer only"** stays where it is in the prototype (a footer on the list) but is **next slice**
   natively. Do not build it here.

Still ask me about anything not covered above (for example, whether the first launch should show the
examples or an empty list on a real device, and the exact pictogram art for the new icons).

## Scope (assuming my leanings; adjust to my answers)
**In scope**
1. **Pure editing logic in `core/model`.** A routine library (list of routines with add, duplicate,
   delete) and an editable routine draft with operations (add, remove, move,
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
4. **Wiring in `app`**: the Routines list is the start destination; Start opens Run with that routine
   (still scaled by the fast launcher); "Back to routines" in the sheet returns; Save returns to the list;
   Cancel changes nothing.
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
