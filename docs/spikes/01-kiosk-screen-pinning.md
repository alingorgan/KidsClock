# Spike (a): Kiosk / screen pinning

Device: Moto G14, Android 14 (API 34), stock-ish Motorola build. Tested via the debug-only
`SpikeActivity` (`app/src/debug/kotlin/com/kidsclock/SpikeActivity.kt`), calling
`AndroidLockTaskController.start()`/`stop()` (`core/platform`), driven over adb.

## What was tried

**Screen pinning (`Activity.startLockTask()`, no device-owner)** — the approach
`docs/INITIAL_SETUP_PLAN.md` said to try first.

1. Calling `startLockTask()` with the OS's "App pinning" toggle in its default (**off**) state
   silently did nothing: no exception, no dialog, `ActivityManager.lockTaskModeState` stayed
   `LOCK_TASK_MODE_NONE`. Nothing in logcat either. This is the real finding for this spike:
   **screen pinning does not work out of the box.** The user (or a setup flow the app drives) must
   first turn on Settings → Security & privacy → More security & privacy → **App pinning**, which
   is off by default on this device.
2. With App pinning turned on, `startLockTask()` worked as documented: the system showed a
   bottom-sheet ("App is pinned… Swipe up and hold to unpin") and `lockTaskModeState` became
   `LOCK_TASK_MODE_LOCKED`.
3. While pinned, tried to leave the app:
   - **Home button** — blocked, stayed pinned.
   - **Recents / Overview (`APP_SWITCH`)** — blocked, stayed pinned.
   - **Back** — blocked, stayed pinned.
   - **Pulling down the notification shade** — blocked; only a thin system status-bar sliver is
     reachable, with a toast reminding "To unpin this app, swipe up and hold" — no shade content,
     no quick settings.
   - **Swipe-up-and-hold from the bottom edge** (the documented unpin gesture) — not reliably
     reproduced over `adb shell input swipe` (it's a stop-and-hold gesture; a plain timed swipe
     doesn't replicate the hold). Needs a finger on a real screen to confirm the exact hold
     duration and how discoverable/accidental-proof it is for a toddler — flagged below as still
     open.
   - **Our own "Stop lock task" button, calling `stopLockTask()` from inside the app** — this
     **works cleanly** and exits pinning immediately, no gesture needed. This is the one we'd
     actually use for the grown-up gate: the app doesn't need to rely on the OS gesture at all,
     since it can unpin itself once the grown-up gesture succeeds.

**Device-owner lock task** — not attempted. Requires `adb shell dpm set-device-owner` on a device
with zero accounts/no other device-owner-incompatible apps, which is not viable on this owner's
already-set-up personal phone without a factory reset. Per the plan, this needed asking first
regardless of feasibility.

## Recommendation

Screen pinning (`startLockTask`, no device owner) is real and does block Home/Recents/Back/shade
for a child. Two consequences for the product:

1. **Setup must turn on "App pinning" for the user**, or walk them through it — it is off by
   default and there's no way for an app to flip it silently (it's a security setting). This
   belongs in the setup flow (`feature/setup`, not yet built) as an explicit step with instructions,
   not something we can assume.
2. **The app should drive its own unpin via `stopLockTask()`** behind the existing grown-up gesture
   (`docs/DECISIONS.md` #13), rather than teaching the caregiver the OS's swipe-and-hold gesture.
   That also sidesteps the swipe-and-hold's unconfirmed toddler-proofness — the caregiver never
   needs to know it exists.

## Still open (needs a hands-on test, not adb)

- Exact hold duration and force needed for swipe-up-and-hold, and whether a toddler can trigger it
  by accident during normal play (bouncing, hitting the bottom edge repeatedly). Recommend the
  owner spend 2 minutes trying to "accidentally" unpin it with their kid.
- "Lock device when unpinning" is an available OS option (seen, unchecked, in App pinning
  settings) — turning it on would require the device's screen-lock credential to unpin, closing
  the swipe-and-hold gap entirely at the cost of setup friction (the caregiver would need a
  device PIN/pattern set up as a prerequisite). Worth a product decision once we know whether
  swipe-and-hold alone is toddler-proof enough.
- Device-owner lock task remains unexplored; only worth revisiting if screen pinning turns out to
  be escapable in practice.

## OPEN_QUESTIONS.md #13 status

Answered for the "no device-owner" path: screen pinning works, blocks the obvious exits, requires
a one-time OS setting most users won't have on, and the app can self-unpin. Recommend closing #13
in favour of screen pinning + `core/platform`'s `LockTaskController`, pending the hands-on
toddler-proofness check above.
