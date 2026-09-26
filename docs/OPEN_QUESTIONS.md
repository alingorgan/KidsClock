# Open questions

Do not guess these in code. Ask the owner, or make them configurable and log the choice.

## Product
1. **Interrupted activities.** Starting a quick timer mid-activity uses up the current activity; it does not resume afterwards. Should the interrupted activity resume with its remaining time?
2. **Custom quick timers.** Presets only today. Want a custom name and an on-the-spot photo?
3. **Ad-hoc dots in the day strip.** Each extension adds a dot; they crowd the strip. Hide ad-hoc dots?
4. **Final activity signal.** Should "Sleep time" get a different ending (slow fade to dark, no chime) instead of the standard treatment?
5. **Grace period.** When the screen turns red, can the caregiver add a short grace period, or is red always "move on now"?
6. **Persistence.** Should photos, clips and the routine persist between sessions? (Assumed yes on native.)

## Design (to test with real children)
7. Does the full-screen saturated red read as "we are done, something new is next", or as being told off? Watch for protest.
8. Are the green and red shades strong enough from across a room, in bright light? Peak red may be too harsh; back it off if so.
9. Is a full-colour background tiring over long activities (bath, story)?
10. Does the shrinking circle really read as "nearly done" for a 2–3-year-old?
11. Does the hold-a-corner gesture stop toddlers, or attract them? Consider a different gesture.
12. Ranking of media: the place (sink) vs the object (toothbrush) vs a self-recording.

## Engineering
13. ~~**Screen pinning / kiosk lock.**~~ Spiked on a real device (Moto G14, Android 14):
    `docs/spikes/01-kiosk-screen-pinning.md`. Screen pinning (no device-owner) blocks Home,
    Recents, Back and the notification shade, and the app can cleanly self-unpin via
    `stopLockTask()`. Requires the OS's "App pinning" setting to be turned on first (off by
    default) — a setup-flow step, not something the app can enable silently. Still open: whether
    the OS's swipe-and-hold unpin gesture is discoverable/triggerable by a toddler by accident
    (needs a hands-on test with a real child, not adb) — the app-side gate does not depend on this
    either way, since it drives its own unpin.
14. Battery and heat with the screen always on, and overnight charging behaviour. Spiked:
    `docs/spikes/04-battery-heat.md`. 30 minutes screen-on while charging showed no heat buildup
    (31.6°C → 28.1°C) and charged normally. Still open: unplugged screen-on drain rate wasn't
    measured (the test device happened to be on the charger throughout) — needs a short follow-up
    run off the charger.
15. Sound: volume control, quiet hours, silent mode and audio focus behaviour. Spiked:
    `docs/spikes/03-audio-focus.md`. Confirmed the chime (played on `STREAM_MUSIC`) is unaffected
    by ringer silent/vibrate mode on this device. Still open: behaviour under Do Not Disturb
    "Total silence" (couldn't be toggled from adb on this device/build) and ducking behaviour
    against another app already holding audio focus — both need a manual on-device check.
16. Accessibility: TalkBack labels, reduced motion, contrast on the traffic colours.
