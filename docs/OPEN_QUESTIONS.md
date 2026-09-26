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
13. **Screen pinning / kiosk lock.** Android screen pinning versus a true lock-task or device-owner kiosk. Needs an early spike on a real device to see how much of the "child cannot leave the app" goal is achievable without special setup.
14. Battery and heat with the screen always on, and overnight charging behaviour.
15. Sound: volume control, quiet hours, silent mode and audio focus behaviour.
16. Accessibility: TalkBack labels, reduced motion, contrast on the traffic colours.
