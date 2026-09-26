# ADR 0003: Module boundaries — a reusable template

Status: accepted (2026-09-22). Supersedes the module-count reasoning in ADR 0001 where they
conflict (ADR 0001's four-module count stands; this ADR fixes the *shape* every module of each
type takes, going forward, in this project and in future ones built the same way).

## Context
`docs/ARCHITECTURE.md` described the four modules that exist, but not the pattern they're
instances of: what makes something a `core` module versus a `feature` module, when a new module is
warranted, and who is allowed to depend on whom. That gap gets expensive exactly when it matters
least to think about it — mid-feature, under time pressure. This ADR is written to be copied
whole into a new project, not just to describe this one: names like `<concern>` and `<name>` below
are placeholders, not KidsClock specifics.

## Decision: three module types, no more
**`app`**, **`core/<concern>`**, and **`feature/<name>/{api,impl}`**. No fourth category (a
"capability" module, a "shared" module) — see Rejected alternatives.

### `app`
Wiring only. `MainActivity`, manual DI (an `AppContainer`), deciding which `feature` screen is
shown when. No business logic, no look-and-feel decisions, no third-party API surface of its own.

### `core/<concern>`
One capability, wrapped behind an interface we own, mirroring the third-party/platform API it
sits on (`Clock` over `SystemClock`, `AlertPlayer` over `SoundPool`/`AudioTrack`, a future
`RoutineStore` over Room/DataStore). Never contains a screen. `core/model` is the one `core`
module with no dependencies at all — pure Kotlin, the shared vocabulary (types, rules) every other
module can reference. Every other `core/<concern>` module may depend on `core/model` and nothing
else internal — no lateral `core`-to-`core` dependency, ever (see Dependency rules).

`core/designsystem` is a `core` module and follows the same rule (depends on nothing internal —
not even `core/model`, for now), but is the **one deliberate exception to "no UI in `core`"**: it
is the design system, tokens + theme + reusable blocks, not a business capability. It is also
where **shared UI across features lives** — a component two `feature/*/impl` modules both need is
a `core/designsystem` block, not a new module. It is the only module allowed to depend on the
underlying UI framework's non-primitive layer (Material 3); every `feature/*/impl` builds from its
blocks only, compile-enforced.

**Shared data types go in `core/model`.** A type two features need to exchange belongs in
`core/model`, not in either feature's `api` — that keeps `core/model` the actual shared vocabulary
instead of one feature's `api` becoming a back door into it.

### `feature/<name>/api` and `feature/<name>/impl`
Every feature is **two modules from day one**, not promoted later:
- **`api`**: the feature's public contract. Pure Kotlin (the same `kidsclock.kotlin.jvm` plugin
  `core/model` uses) — no Android, no Compose (not even the `@Composable` annotation), no
  `core/designsystem`. Depends on `core/model` only. Kept deliberately cheap and stable: something
  that wants to *refer to* a feature (another feature, or `app` wiring against a stable type)
  depends on this, never on `impl`, so a change inside the screen never forces a rebuild of
  everything that merely refers to the feature.
- **`impl`**: the actual screen(s). Android library + Compose, built only from `core/designsystem`
  blocks, holding its own thin state layer over `core/model` (ADR 0002). Depends on `core/model`,
  `core/designsystem`, whichever `core/<concern>` modules it needs, and its own sibling `api`
  (automatic — the convention plugin derives the sibling path from the module's own Gradle path).
  May also depend on another feature's `api`, to reference it — never another feature's `impl`.

**Applied to `feature/run` now**, even though nothing yet depends on its `api` — `app` currently
depends on `feature/run/impl` directly, which is fine for a single caller. `feature/run/api` today
holds only a placeholder (`RunFeature.ID`); it earns real content — a route, args, a result
callback — the first time something needs to reference the feature without depending on its
implementation. The point of applying the split now, before it's exercised, is exactly what this
ADR is for: the shape is decided once, not re-decided per feature or per project.

## Dependency rules
```
core/model            -> nothing
core/<other concern>   -> core/model only                          (never another core/<concern>)
core/designsystem      -> nothing (see above)
feature/<x>/api        -> core/model only                          (never Compose, never designsystem, never another feature)
feature/<x>/impl       -> core/model, core/designsystem,
                           any core/<concern> it needs,
                           feature/<x>/api (itself, automatic),
                           feature/<y>/api  (0..n, to reference another feature)
                                                                    (never feature/<y>/impl)
app                     -> anything
```
- **No lateral `core`-to-`core` dependency.** If two `core/<concern>` modules seem to need each
  other, the shared thing belongs in `core/model`.
- **No `feature`-to-`feature` dependency except `impl -> other's api`.** This is the one rule
  everything else here exists to protect: it's what stops a screen change three features away from
  forcing a rebuild of this one, and what stops the dependency graph from becoming unreviewable.
- **`implementation`, not `api` (the Gradle configuration), by default everywhere.** A module's
  own dependencies shouldn't leak into its consumers' compile classpath by accident — that's a
  quiet way the graph above gets violated in practice even when every `build.gradle.kts` file
  looks correct on its own.
- **Enforced mechanically, not just by review.** `kidsclock.dependency.rules` (applied at the
  root, `build.gradle.kts`) reads every subproject's declared `implementation`/`api` dependencies
  and checks them against the table above, by Gradle-path pattern (`:core:model`,
  `:core:<x>`, `:feature:<x>:api`, `:feature:<x>:impl`, `:app`) — so a new `core/<concern>` or
  `feature/<name>` module is covered automatically, no per-module guardrail code needed. Wired into
  `check` as `verifyDependencyRules`. It only reads declared production dependencies
  (`implementation`/`api`); test configurations are out of scope.

## Rejected alternatives
- **A fourth "capability" module type** (a networking/navigation/security namespace separate from
  `core`) — rejected. `core/*` here is already a namespace of single-purpose modules, not one
  monolithic module; a second namespace solves nothing that `core/<concern>` doesn't already
  solve, and adds a decision ("core or capability?") with no rule to answer it every time a module
  is created.
- **A "shared" feature module** for code used by more than one feature — rejected as a category.
  Split by what the shared thing actually is: UI → `core/designsystem`; data → `core/model`;
  behaviour one feature exposes to another → that feature's own `api`. "Shared" as a name invites
  becoming a dumping ground with no natural boundary.
- **A networking module of any kind** — not deferred, rejected outright. `CLAUDE.md` requires no
  `INTERNET` permission, enforced by `verifyNoInternetPermission`; a networking module would
  contradict a decided product constraint, not anticipate a future one.

## Tooling
`scripts/new-feature.sh <name>` scaffolds a new `feature/<name>/{api,impl}` pair — both build files, the placeholder contract object, a placeholder screen (tagged per `docs/UI_AUTOMATION.md`), its snapshot test with baselines recorded, and the `settings.gradle.kts` entries — then runs `check` on both to prove it. It does not wire the feature into `app`, and does not invent real screen content: replacing the placeholder is a product decision, made separately. This is the mechanical half of "never re-decide this"; the ADR is the half that explains why.

## Consequences
- `feature/run` is now `feature/run/api` + `feature/run/impl`; `app` depends on the latter.
- The next feature module (`feature/setup`, when it's built) gets the same shape from its first
  commit — no decision to make, per the goal of this being a template.
- `core/data` and `core/platform`, when they're built, get `verifyDependencyRules` coverage for
  free (no lateral dependency between them, `core/model` only) without any new guardrail code.
