# Open questions

Do not guess these in code. Ask the owner, or make them configurable and log the choice.

## Product
Questions 1-6 were answered by the owner on 2026-10-02 (recorded as decisions 20-25 in
`DECISIONS.md`). The follow-ups below are what those answers left open.

1. ~~**Interrupted activities.**~~ Resolved: the interrupted activity is paused and resumes after the new one finishes (decision 20).
2. ~~**Custom quick timers.**~~ Resolved: presets only, no custom name or photo (decision 21).
3. ~~**Ad-hoc dots in the day strip.**~~ Resolved: hide them (decision 22).
4. ~~**Final activity signal.**~~ Resolved: no chime when "Sleep time" is reached (decision 23).
5. ~~**Grace period.**~~ Resolved: the caregiver can optionally add one (decision 24).
6. ~~**Persistence.**~~ Resolved: yes (decision 25).

### Follow-ups (answered 2026-10-02 unless marked open)
- ~~**1a. Resume vs. skip.**~~ The interrupted activity **auto-resumes** when the quick timer ends; the routine then proceeds as before (decision 20). No caregiver choice.
- ~~**1b. "More time".**~~ Just extends the current activity. Caregiver-only (decision 26).
- ~~**1c. Interrupting an activity already in `transition`.**~~ Not paused; it hands over as usual. Confirmed "for now".
- ~~**1d. Nested interruptions.**~~ Not allowed.
- ~~**4a. Fade to dark.**~~ Yes: "Sleep time" fades slowly to dark, no chime (decision 23).
- ~~**5a. Grace period.**~~ The caregiver chooses 1, 3, 5 or 10 minutes, "for now" (decision 24).
- ~~**6a. Scope of persistence.**~~ Confirmed: routine, run anchor, later photos and clips (decision 25).

### Answered after that (2026-10-02)
- ~~**A. Quick-timer end.**~~ The chime plays and the screen goes red (`transition`); the interrupted activity then **auto-resumes after 5 seconds** with no interaction (decision 27).
- ~~**B. Grace vs. "More time".**~~ One feature: "More time" only. The separate grace period is dropped (decision 24 superseded). It can be started before the activity ends and after it has ended (decision 26).
- ~~**C/D. Fade to dark.**~~ Starts immediately when "Sleep time" appears (decision 23).

### Answered last (2026-10-02)
- ~~**E. "More time" durations.**~~ 5/10/15 min, for now.
- ~~**F. Display after "More time" from red.**~~ Progress is measured against the new, longer total, so it drops back (an 8 min activity plus 5 min is at about 62%, amber).
- ~~**G. Fade length.**~~ 5 seconds.
- ~~**H. Auto-resume and handover policy.**~~ Confirmed: the auto-resumed activity skips the policy.

No product questions from this round remain open.

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

### Decided 2026-10-02 (persistence)
- ~~**P1. Persisting a run across a kill or reboot.**~~ Not for now: the routine restarts from the beginning (decision 31). Open to revisit: full run persistence (the design is in `ARCHITECTURE.md` "Timekeeping") if kills happen mid-evening in real use.
- ~~**P2. Can the child finish an activity early?**~~ No, only the grown-up, through the sheet (decision 30). Revisit after testing with a child.
