# Prompt 01: Real-device spikes (Phase 1)

Paste this into a fresh Claude Code session at the repo root.

---

You are running Phase 1 of `docs/INITIAL_SETUP_PLAN.md`: real-device spikes, before any more product code gets built on assumptions. This is investigation, not a feature. The output is findings, written down, plus whichever platform interfaces the findings force into existence for real — not a finished feature.

## Read first (in order)
1. `CLAUDE.md`
2. `docs/ARCHITECTURE.md`, `docs/DESIGN_SYSTEM.md`, `docs/UI_AUTOMATION.md`
3. `docs/adr/0001-groundwork.md`, `0002-ui-architecture.md`, `0003-module-boundaries.md`, `0004-third-party-isolation.md`
4. `docs/INITIAL_SETUP_PLAN.md` (Phase 1's four spikes, and the Risks section)
5. `docs/OPEN_QUESTIONS.md` — items 13 (kiosk), 14 (battery/heat), 15 (sound), 16 (accessibility). Do not guess these; ask, or report the spike's finding and let me decide.
6. `docs/DECISIONS.md`, for what's already settled (so you don't re-litigate it)

This needs a **real Android phone over USB**, not the emulator: kiosk/lock-task behaviour, reboot survival, real audio hardware, and battery/heat cannot be validated on the AVD.

## The four spikes
Report findings for each; do not pick a final approach without asking, except where noted.

**(a) Kiosk / screen pinning (OPEN_QUESTIONS 13).** Try screen pinning (`startLockTask`) first — no special provisioning, works on any device. Try device-owner lock task (`adb shell dpm set-device-owner`) as the fallback if pinning is too easy for a toddler to exit. Report exactly what a child can and can't do to leave the app in each mode, and what setup each requires (device-owner needs a factory-reset device or `adb`, not viable if the phone already has other apps/accounts on it). **Ask before committing to one** — this is a real product tradeoff (setup friction vs. escape-proofness), not a technical call.

**(b) Timekeeping across sleep/kill/reboot.** Persist a run anchor `{activityIndex, startedAtElapsed, bootId, pausedAccumulated}` (as speced in `docs/ARCHITECTURE.md`'s Timekeeping section) and confirm elapsed time computed from it survives: screen off and back on, the process being killed (not just backgrounded — use `adb shell am kill` or Don't Keep Activities), and a real reboot. Confirm the boot-id-changed fallback to a wall-clock timestamp actually triggers correctly across a reboot, not just in theory.

**(c) Audio focus and the end chime (OPEN_QUESTIONS 15).** A short test tone via `AudioFocusRequest` (`AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`, per `docs/DECISIONS.md`). Report what actually happens in silent mode, in Do Not Disturb, and while another app (e.g. a podcast) holds audio focus — does the chime play, get ducked, or get silently dropped, and is that the right behaviour for a child-facing timer.

**(d) Keep-awake battery and heat (OPEN_QUESTIONS 14).** Run the screen on and awake for about 30 minutes at a comfortable brightness; note battery drain and whether the device gets noticeably warm. Note anything relevant about overnight charging-while-running behaviour (the last routine item is likely "Sleep time").

## How to build the spikes, given what exists now
- This is the **first real trigger** for `core/platform` (`docs/adr/0004-third-party-isolation.md`): a `LockTaskController` and an `AlertPlayer` (over `AudioFocusRequest`) now have a real call site and real variability (spike (a)'s two approaches; spike (c)'s focus-loss handling), so model them for real — interface + real implementation, per ADR 0004 and the `core/<concern>` shape in `docs/adr/0003-module-boundaries.md`. Keep them spike-quality (they don't need a `FakeX` or unit tests yet if nothing in `core/model` depends on them), but put them in `core/platform`, not scattered in `app`.
- Host the spike screens behind a debug-only entry point in `app` (a second launcher activity or a debug menu — your call), not a real `feature/*` module: this is throwaway, and `feature/*` per `docs/adr/0002-module-boundaries.md` is for real product screens.
- Still respect the guardrails: `./gradlew check` must stay green, no `INTERNET` permission, `core/model` stays pure. Spike code failing ktlint/lint is fine to fix or to `// ui-a11y-ignore` past, but the guardrails themselves don't get weakened for this.
- No new dependency without checking `docs/adr/0004-third-party-isolation.md`'s exceptions first.

## Constraints
- Ask before choosing the kiosk approach (spike (a)). Everything else, report and propose; I'll confirm.
- Don't build the real Run screen, setup UI, or persistence layer yet — that's Phase 2/3. This is spikes only.
- Do not touch `prototype/`.

## Done when (verify and show output)
- Findings written to `docs/spikes/` (one file per spike, or one file total — your call), each with what was tried, what happened, and a recommendation.
- `docs/OPEN_QUESTIONS.md` updated: items 13–15 either answered (with the evidence) or sharpened (if still open, say what specifically is still unresolved). Propose `docs/DECISIONS.md` entries for anything the spikes settle.
- `./gradlew check` still passes.
- Screenshots or a short screen recording of the kiosk-exit attempts on the real device.
- Final report: what was learned, what's still open, and a proposed prompt for Phase 2 (the thin vertical slice: `core/model` timeline + phase transitions with tests, the Run screen for one hardcoded routine).
