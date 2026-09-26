# Spike (b): Timekeeping across sleep/kill/reboot

Device: Moto G14, Android 14. Tested via `SpikeActivity`'s "Save anchor" / "Show elapsed", which
persists `{startedAtElapsed, bootCount, wallClockAtStart}` to `SharedPreferences` and recomputes
elapsed time exactly as `docs/ARCHITECTURE.md`'s Timekeeping section describes: elapsed-realtime
normally, falling back to a wall-clock delta when the boot id (here, `Settings.Global.BOOT_COUNT`)
changes.

## Results — all three cases confirmed on the real device

1. **Screen off → on.** Anchor saved, screen powered off for ~6s, woken and unlocked, "Show
   elapsed" re-opened: `elapsed-realtime path: 70300ms since anchor (bootCount=8 unchanged)`.
   Matches wall-clock time elapsed. Screen sleep does not disturb `SystemClock.elapsedRealtime()`.
2. **Process killed (`am force-stop`, not just backgrounded).** Anchor saved, app sent Home,
   force-stopped, relaunched fresh (new PID) ~2 minutes later: `elapsed-realtime path: 130902ms
   since anchor (bootCount=8 unchanged)`. The anchor survived on disk
   (`/data/data/com.kidsclock/shared_prefs/spike_run_anchor.xml`) and `elapsedRealtimeMillis()`
   kept counting correctly across the kill, because it's a kernel/hardware clock, not
   process-owned.
3. **Real reboot (`adb reboot`, not a simulated one).** Anchor saved at `bootCount=8`. Rebooted
   (~92s to `sys.boot_completed=1`). New session: `bootCount=9`. "Show elapsed" correctly detected
   the boot id changed and used the **wall-clock fallback**: `WALL-CLOCK FALLBACK: reboot detected
   (bootCount 8 -> 9), 262412ms since anchor` — 262.4s, consistent with the actual ~4m22s between
   save and check.

`Settings.Global.BOOT_COUNT` (queried via `Settings.Global.getInt(contentResolver,
Settings.Global.BOOT_COUNT, -1)`) is a reliable, always-available boot-id proxy on this device —
increments by exactly 1 per reboot, no permission required. This is a good candidate for the real
`bootId` field in the run anchor, in place of a hypothetical or invented ID.

## Recommendation

The Timekeeping design in `docs/ARCHITECTURE.md` is validated as designed, with one concrete
answer: **use `Settings.Global.BOOT_COUNT` as the boot id.** No changes needed to the design.
`core/platform`'s new `AndroidClock` (over `SystemClock.elapsedRealtime()`) is the real
implementation of `core/model`'s `Clock` and can be used as-is by the real run-anchor persistence
layer (`core/data`, not yet built).

## OPEN_QUESTIONS.md status

This spike wasn't one of the numbered open questions (13–16) but underpins all of them; no open
items remain from it. Recommend adding "boot id = `Settings.Global.BOOT_COUNT`" as a
`docs/DECISIONS.md` entry.
