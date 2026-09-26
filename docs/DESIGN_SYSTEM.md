# Design system ("Lego blocks")

`core/designsystem` is the only place that decides how things look. Screens in `feature/*/impl` are assembled from its blocks, the way a model is built from bricks. It contains no business logic and no knowledge of routines, phases or time.

It is also **where shared UI across features lives** — a component two `feature/*/impl` modules both need becomes a block here, not a new module (`docs/adr/0003-module-boundaries.md`). Shared *data* types go in `core/model` instead; a block never takes a `RunState` or other `core/model` type (see Rules below).

## Layers
1. **Tokens** (`tokens/`): colours (`KcColors`, activity palette from SPEC §12, light and dark), spacing (`KcSpacing`). Read them with `KcTheme.colors` / `KcTheme.spacing`. Never write a raw colour or dp value in a feature.
2. **Theme** (`KidsClockTheme`): provides the tokens. Every screen and every snapshot test is wrapped in it.
3. **Blocks** (`components/`): `KcScreen` (full-screen stage), `KcCircle` (solid or translucent circle, SPEC §4), `KcText` (the only text block), `KcButton` (the one tappable block, for grown-up controls). Add new blocks here, small and single-purpose; bigger pieces are composed from smaller blocks.

## Rules
- Features use `Kc*` blocks only. Material 3 is a private dependency of `core/designsystem`; feature modules do not have it on their classpath, so bypassing the blocks is a compile error.
- Blocks take plain parameters (text, size, colour tokens, slots). No `RunState`, no `core/model` types, no callbacks that carry decisions. A feature maps state to block parameters.
- **Every block takes a required `testTag`** (id convention `<screen>.<element>`), and icon-only blocks also take a `contentDescription`. This is how every element stays reachable by UI automation and accessibility services; see `docs/UI_AUTOMATION.md`. `verifyAccessibleInteractions` (part of `check`) backs this up for interactive blocks.
- If a feature needs a look that no block provides, add or extend a block (with its snapshot test) instead of styling inline.
- Colour is never the only signal (CLAUDE.md): blocks that convey state must also carry it in shape or sound.
- Respect reduced motion (SPEC §7b) in any animated block.

## Snapshot tests (presentation only)
- **Every UI component has a snapshot test**: each source file with a public `@Composable` needs `<FileName>SnapshotTest.kt` in the same module. `verifySnapshotCoverage` (part of `check`) fails otherwise.
- Snapshot tests check how things look, not what they decide. Business logic is tested in `core/model` unit tests, not here. Feed components fixed parameters; do not drive them from a real state machine.
- Cover the meaningful visual variants: light and dark, and states that change the look (translucent vs solid, long text, etc.).
- Stack: Roborazzi + Robolectric on the JVM (no emulator). Baselines are PNGs committed in `src/test/snapshots/`. Pattern: copy `core/designsystem/src/test/.../KcCircleSnapshotTest.kt`.
- `./gradlew check` (and the pre-commit hook) verifies against baselines and fails on any pixel difference.
- After an intended visual change: `./gradlew recordRoborazziDebug`, look at the changed PNGs, commit them with the change. Never re-record to make a failing test pass without checking why it changed.
