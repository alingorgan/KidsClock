# Spike (c): Audio focus and the end chime

Device: Moto G14, Android 14. Tested via `SpikeActivity`'s "Play chime", which calls
`AndroidAlertPlayer.playChime()` (`core/platform`): requests
`AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` per `docs/DECISIONS.md` #11, plays a short
`ToneGenerator` beep on `STREAM_MUSIC`, then abandons focus.

## What was confirmed on-device

- **The focus request/release cycle works cleanly.** No errors, no focus denial, in every ringer
  state tried. `dumpsys audio`'s focus stack was empty immediately after each call, consistent
  with a transient request that's granted and released within ~600ms as coded.
- **`STREAM_MUSIC` is not part of the ringer-mode-affected stream group on this device.**
  `dumpsys audio` reports: `ringer mode affected streams = 0x1a6 (STREAM_SYSTEM, STREAM_RING,
  STREAM_NOTIFICATION, STREAM_SYSTEM_ENFORCED, STREAM_DTMF)` — `STREAM_MUSIC` is absent from that
  list. **This means putting the phone in silent or vibrate mode will *not* silence the chime**,
  because it's played on the music stream, not the ring/notification streams silent mode targets.
  This is standard, well-documented Android behaviour (confirmed here against this real device's
  actual reported stream groups, not just assumed) and directly answers part of
  `OPEN_QUESTIONS.md` #15.

## Follow-up: audibility at real volume levels

First on-device listening test (owner present) at the device's ambient media volume (5/15) — **not
audible**, or audible enough to not be noticed. Raised `STREAM_MUSIC` to max (15/15) and replayed:
clearly audible. This is a real product finding: a 400ms `ToneGenerator` beep at
`ToneGenerator.MAX_VOLUME` is still quiet at everyday media-volume levels, on this device's
speaker. For a chime that's meant to get a child's attention "without looking" (`docs/DECISIONS.md`
#11), the real implementation likely needs either a louder/longer synthesised tone, more than one
repetition, or to run at a floor volume regardless of the phone's current media volume setting
(with the obvious caveat that ignoring the user's volume choice is its own tradeoff) — a product
decision, not something to guess at in code. Left the device at a moderate volume (10/15) afterwards.

## What's still open (adb couldn't confirm these)

- **Do Not Disturb, specifically "Total silence."** Unlike plain ringer-silent, Android's
  "Total silence" DND mode is documented to mute media/alarm streams too unless explicitly
  allowed. `adb shell settings put global zen_mode 2` and `adb shell cmd notification set_dnd
  total_silence` were both rejected on this device/build (permission and argument-format issues
  respectively) — DND could not be toggled from the shell here. **Needs a manual on-device check**:
  turn on Total Silence from the notification shade, tap "Play chime," and listen. If it's
  silenced, the chime may need to be promoted to a stream/attribute category DND treats as an
  alarm, which is a real design tradeoff (alarms interrupt more aggressively) — a product decision,
  not a default to guess at.
- **Another app already holding audio focus** (e.g. a podcast or music app playing). Not exercised
  — would need a second real audio-playing app running concurrently. Expected behaviour per the
  `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` request is that the other app ducks its volume rather than
  stopping, for the chime's short duration, then regains full volume — this is the documented OS
  contract for that focus type, but wasn't independently observed here.

## Recommendation

The `AlertPlayer` interface and `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` choice both check out
mechanically. Before shipping, someone needs to manually confirm the Total Silence DND case on a
real device and decide (with the owner) whether the chime should be allowed to break through it —
this is exactly the kind of design open question `docs/OPEN_QUESTIONS.md` says not to guess.

## OPEN_QUESTIONS.md #15 status

Partially answered: silent/vibrate ringer mode does not affect the chime (confirmed). Total
Silence DND behaviour and cross-app ducking remain open — sharpened above, not resolved.
