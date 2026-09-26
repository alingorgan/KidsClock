# UI automation

Every element built from a `core/designsystem` block is reachable by automation (Maestro,
Espresso/Compose UI tests) and by accessibility services, by construction. This is groundwork:
`RunScreen` is the only real screen so far, but the pattern and guardrails are in place before
there is more to click.

## How it works
- **`KcScreen`** turns on `testTagsAsResourceId` for everything below it. A Compose `testTag` is
  then exposed two ways:
  - to Compose UI tests, via `onNodeWithTag(...)` (what an in-process Espresso/Compose instrumented
    test uses — see `androidx.compose.ui.test.junit4`);
  - to **UiAutomator-based tools**, as the accessibility node's *resource-id* string. Maestro,
    `adb shell uiautomator dump`, and `androidx.test.uiautomator` all read elements this way. This
    is the mechanism that makes the app automatable from outside the process, which plain Compose
    testing does not give you.
- **Every `Kc*` block takes a `testTag`.** `KcScreen`, `KcCircle`, `KcText` and `KcButton` all
  require one; there is no way to add a block to a screen without naming it.
- **Icon-only controls need a `contentDescription`.** So far there are no icon-only blocks, but the
  guardrail below is ready for when there are.

## Naming convention
`<screen>.<element>`, lower camel or plain words, e.g. `run.screen`, `run.title`,
`gate.button` (future), `sheet.close` (future). Stable and content-independent: a tag must not
change when translated text or a photo changes, since Maestro flows and instrumented tests key off
it.

## Guardrails (part of `./gradlew check`)
- **`verifyAccessibleInteractions`** (every module with Compose): scans each `@Composable` function
  for interactive calls (`Button`, `IconButton`, `.clickable`, `Switch`, `Slider`, ...) and fails if
  the function has no `testTag(...)`; and for any `Icon(...)` call, fails if it has no non-null
  `contentDescription`. It is a heuristic text scan, the same approach as `verifyModelPurity`, not a
  type checker. A composable that is genuinely decorative can opt out with a
  `// kc-a11y-ignore: <reason>` comment in its body.
- **Compile-time:** since `Kc*` blocks require `testTag` as a normal (non-default) parameter, code
  that forgets it fails to compile, before the guardrail even runs.

## On-device (instrumented) tests
- `kidsclock.android.uitest` (applied to `app` and every `feature/*/impl` module) adds Compose UI test,
  Espresso and UiAutomator dependencies and sets the JUnit instrumentation runner.
- Pattern: `app/src/androidTest/kotlin/com/kidsclock/MainActivitySmokeTest.kt`. It finds the same
  element two ways — `onNodeWithTag` (Compose/Espresso) and a raw `UiDevice`/`By.res(...)` lookup
  (UiAutomator, what Maestro does) — to prove the resource-id mapping actually works, not just that
  Compose can see its own tree.
- Run: `./gradlew :app:connectedDebugAndroidTest` (needs a running emulator or device: see
  `.claude/skills/android-run/SKILL.md`). Not wired into the commit/push hooks: it needs a device,
  which the hooks cannot assume is present. Run it by hand before a PR that touches UI, and add it
  to CI once CI exists.

## Maestro
Maestro is an external CLI; nothing in the app depends on it. Install it yourself
(`curl -Ls "https://get.maestro.mobile.dev" | bash`, from https://maestro.mobile.dev — not run by
Claude, since it is a curl-pipe-to-shell installer). Flows live in `.maestro/`.

```
maestro test .maestro/smoke.yaml
```

`smoke.yaml` launches the app and asserts `run.screen` and `run.title` are visible. Add a flow per
user-visible behaviour once there is more than one screen; keep ids as the only selector (never
match on translated text).

## AI-driven verification
The same ids let an agent (Claude driving `adb`/UiAutomator, or a Maestro flow written by an agent)
assert on specific elements instead of screen-scraping text or coordinates:
```
adb shell uiautomator dump && adb shell cat /sdcard/u.xml   # resource-id="run.title" is present
```
This is what `.claude/skills/android-run/SKILL.md` already does for manual verification; the ids
this document defines are what make that reliable instead of guesswork.
