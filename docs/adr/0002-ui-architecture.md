# ADR 0002: UI architecture — unidirectional data flow (MVI-style), not MVVM+C or VIPER

Status: accepted (2026-09-22).

## Context
`feature/run` has a `RunScreen` with no state yet. Before it grows one, we need a pattern for how
Compose UI connects to `core/model`. `docs/INITIAL_SETUP_PLAN.md` already specified the shape of
`core/model` itself: a single sealed `RunState`, events (`Tick(now)`, `ChildTap`, `GateOpened`,
`CaregiverAction(...)`, `QuickTimer(...)`), and a reducer `(state, event, now) -> state + effects`,
with effects (chime, persist, lock) returned as data for the platform layer to execute. This ADR is
about wiring that into Compose, not about changing it.

## Decision
**Unidirectional data flow (UDF), MVI-flavoured**, following Compose's own architecture guidance —
not classic MVVM, not MVVM+C, not VIPER.

```
core/model                RunState, Event, reduce(state, event, now) -> state + effects
     v
feature/*: a ViewModel     holds RunState (or a thin UI-state projection); exposes
                           StateFlow<RunUiState>; exposes one-shot effects via a
                           Channel/SharedFlow (replay = 0), never via StateFlow
     v
Screen (Compose)           collectAsStateWithLifecycle(); stateless below that — every
                           Kc* block takes plain parameters and lambdas, no ViewModel
                           reference of its own
```

- **State, not events, is what Compose observes.** The screen renders one immutable UI-state value;
  `core/model` types are not passed into `Kc*` blocks directly (keeps the existing
  `feature -> core/model, core/designsystem` dependency rule meaningful one layer further up).
- **One-shot effects use a `Channel`/`SharedFlow`, not `StateFlow`.** A `StateFlow` redelivers its
  last value on recomposition and config change, which is wrong for "play the chime once."
- **`androidx.lifecycle.ViewModel` is the host**, even though the app is single-Activity and
  portrait-locked: it still survives non-rotation config changes (locale, dark/light, font scale)
  for free, and it is the idiomatic place a `StateFlow` lives. It is a thin wrapper around the
  `core/model` reducer — logic stays in `core/model`, testable on the JVM with `FakeClock` — not a
  place new rules get written. Constructed manually (`AppContainer`), per ADR 0001's no-Hilt
  decision; add `androidx.lifecycle:lifecycle-viewmodel-compose` to the catalog when the first one
  is built.
- **No navigation library yet.** The app is one pinned screen plus a grown-up sheet, not a
  destination stack. Navigation Compose is the natural next step if that changes; nothing is
  adopted pre-emptively.

## Alternatives considered
- **Classic MVVM (LiveData-era, imperative View observing mutable state):** built for imperative
  Views with two-way binding; Compose is already a pure function of state, so MVVM's "View" half
  does no work Compose needs. Classic MVVM also tends to grow logic into the ViewModel directly,
  which conflicts with keeping business logic in `core/model`.
- **MVVM+C (Coordinator):** the Coordinator exists on iOS/UIKit to solve navigation between view
  controllers that don't know about each other. Navigation Compose (or, here, no navigation
  library at all) already owns that declaratively — a Coordinator layer would solve a problem this
  app doesn't have.
- **VIPER:** designed for UIKit's imperative, delegate/protocol-heavy world (View–Interactor–
  Presenter–Entity–Router). It fights Compose's recomposition model, has no idiomatic Kotlin/Android
  equivalent, and its per-screen boilerplate outweighs anything it buys for an app that is
  deliberately one screen plus a sheet.

## Consequences
- The existing `core/model` reducer design needs no change; this ADR only adds the Compose-facing
  layer above it.
- The first `ViewModel` (`feature/run`) should be built test-first in `core/model` (the reducer and
  its effects), with the `ViewModel` itself kept thin enough that it needs no more than a light
  smoke test — the unit-test policy in `CLAUDE.md` still applies to any logic that ends up in it.
- Snapshot tests (`docs/DESIGN_SYSTEM.md`) keep testing `Kc*` blocks with fixed parameters; they
  never construct a real `ViewModel` or drive it from `core/model`.
