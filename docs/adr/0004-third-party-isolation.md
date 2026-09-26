# ADR 0004: Third-party and platform API isolation

Status: accepted (2026-09-22).

## Decision
Any third-party library or Android platform API that a **capability** depends on is accessed
through an interface we own — defined and, where it needs Android, implemented in the
`core/<concern>` module that owns that capability (ADR 0003) — mirroring the library's shape, not
re-exporting it. `Clock` (over `SystemClock`) is the existing example. **Model the interface only
when there's a real call site** — this is not "wrap everything in `core/*` up front"; a capability
gets an interface the first time production code needs to touch the library or platform API
directly, not speculatively ahead of that.

This covers Android *platform* APIs (`WindowManager`, `AudioManager`, `SystemClock`) the same as
third-party *libraries* (Room, CameraX, Media3, when they arrive) — both are "someone else's API
shape we don't want scattered through our own code," and both get the same treatment.

## Exceptions
Four categories are used directly, never wrapped:

1. **Test-only frameworks** — JUnit, `kotlin.test`, MockK, Roborazzi, Robolectric, Espresso,
   `androidx.test.uiautomator`. Used straight in test source sets. No swap risk (nobody is going
   to replace JUnit mid-project), every Android engineer already knows these APIs, and wrapping a
   test DSL adds a layer with no payoff. `CLAUDE.md`'s testing rules already establish this.
2. **Build-time tooling** — the AGP, Kotlin, Compose-compiler and ktlint Gradle plugins. They
   configure the build; application code never calls them, so there's nothing to mirror.
3. **The UI framework substrate** — Compose's own runtime/`ui`/`foundation` (`Modifier`, `Box`,
   layout), and the Activity-Compose hosting scaffolding (`ComponentActivity`, `Bundle`,
   `setContent`, `enableEdgeToEdge`). This is the framework the app is built *with*, not a
   replaceable service sitting behind a capability — wrapping it would mean wrapping Compose
   itself. **Material 3 is the one part of this substrate that *is* mirrored** — not via a
   classic Kotlin `interface` (that's not how Compose expresses a contract), but via
   `core/designsystem`'s `Kc*` composable functions, confined there and never used directly by a
   `feature/*/impl` (ADR 0003). In Compose, a composable function *is* the interface.
4. **The Kotlin standard library** — not a third-party dependency in the sense this ADR means.

## Action taken: auditing the current dependency graph
Every production (non-test, non-build-tooling) dependency in `gradle/libs.versions.toml`, checked
against the exceptions above:

| Dependency | Category | Wrapped? |
|---|---|---|
| Compose `ui`, `foundation`, `ui-tooling-preview` | UI substrate (exception 3) | No — by design |
| Compose `material3` | UI substrate, mirrored form (exception 3) | Yes — `core/designsystem`'s `Kc*` blocks, confined there |
| `androidx.activity:activity-compose` | Activity-hosting scaffolding (exception 3) | No — by design |
| (Kotlin stdlib) | exception 4 | No — by design |

That's the whole production dependency list today — nothing else is pulled in yet. Every entry is
already either correctly an exception or already wrapped. **Nothing in the dependency catalog
needed action.**

Auditing *platform API usage in production code* (not a listed dependency, but the same rule
applies) found one candidate: `MainActivity.kt` calls
`window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)` directly. Keeping the screen on
while the run screen is shown is a named product requirement (`CLAUDE.md` Engineering rules), and
`core/platform` is already earmarked for it eventually
(`docs/INITIAL_SETUP_PLAN.md`: "AlertPlayer, AudioFocus, KeepAwake, LockTask, ReduceMotion impls").

**This ADR originally wrapped it anyway (`core/platform` + `KeepAwakeController`); that was
reverted.** It was a misapplication of this ADR's own "model only when needed" rule: a product
rule naming the *behaviour* ("keep the screen on") isn't the same as a real trigger to wrap the
*API* — there is no second implementation, no conditional logic, no test asserting on it, and no
call site anywhere else. It's one line with no variability yet. Wrapping it produced a module, an
interface, an implementation and a fake for a single unconditional call — exactly the premature
abstraction this ADR argues against elsewhere. **`MainActivity` calls `window.addFlags(...)`
directly, as before.** Wrap it into `core/platform` the first time something actually needs to
vary the behaviour (conditional enable/disable, a test that needs to assert on it, a second
caller) — not because the requirement is named in `CLAUDE.md`.

`core/model`'s `Clock` interface is a different case and stays as-is: "time is derived from
timestamps, never counted" isn't just a named requirement, the reducer design in
`docs/INITIAL_SETUP_PLAN.md` and ADR 0002 already depends on `Clock` being swappable
(`FakeClock` in tests) from day one — the interface was modeled ahead of a concrete
*implementation*, not ahead of a *need*.

## Consequences
- No `core/platform` module yet. The next platform capability (`AlertPlayer`, most likely — SPEC
  §7) is the first real test of this ADR: build the interface only once a real second
  implementation, fake, or conditional caller exists — not because SPEC/CLAUDE.md names the
  requirement.
