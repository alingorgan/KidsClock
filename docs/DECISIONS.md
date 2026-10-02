# Decisions and why

Confidence: **solid** = supported by evidence found; **plausible** = reasonable inference; **hypothesis** = untested, must be checked with real children. Almost everything about 2–4-year-olds specifically is at most *plausible*, because the literature on typical toddlers is thin.

| # | Decision | Why | Confidence |
|---|---|---|---|
| 1 | Do not teach clock time to this age group. Show *now / how much left / next*. | Conventional clock time has little influence on children under about 5; words like "in a minute" are not understood. | solid |
| 2 | Short horizon: current activity plus at most one "next". | Foresight is weak at 3 and spurts around 3½–4; delays over about 5 minutes are hard for 3-year-olds. | plausible |
| 3 | Show time left as **area that shrinks** (progress bubble), not numbers. | Duration knowledge does not transfer across contexts before about 4; magnitude-style representations are earlier than positional ones. | plausible |
| 4 | Caregiver and child use it **together**, caregiver in control. | Caregiver co-viewing closes much of the toddler "video deficit"; the display is a shared object for conversation. | plausible |
| 5 | Under 3: line pictogram. 3 and over: photo (and clip). | Photos are recognised far more reliably than line drawings; toddlers under about 2.5 often do not treat pictures as information about the real world, and this improves in the third year. Owner's daughter (just over 3) already understands video. | solid / plausible |
| 6 | Personalise: own toothbrush, the place, or a recording. | Closer match to the real thing. Ranking (place, then object, then self-recording) is a guess to test. | hypothesis |
| 7 | Video is optional and short, opened by tap, not autoplay. | Video self-modelling has moderate support in preschoolers (mostly special-education studies); motion competes with the time cue. | plausible |
| 8 | Media stays on the device. | Photos and video of a child. Also a selling point. | decision |
| 9 | Traffic-light colour, on the **whole screen** (radial gradient, stronger at the edges). | Glanceable from across a room. A ring or outline was tried and judged too weak a signal. Red = stop / green = go is learnable at 3 (evidence is preschool guides, weak). Red did not reliably distort time in the studies found. | hypothesis |
| 10 | Colour is never the only cue. | Red-green colour deficiency is roughly 4% of boys globally and 2.6–7.6% in US boys. Shape shrinks and the sound backs it up. | solid |
| 11 | Sound is a **gentle chime**, optionally repeating, not an alarm. | Arousal and emotion distort children's duration judgements. Harsh sounds risk startle or dread. Owner wanted something that gets attention without looking. | plausible |
| 12 | Handover is **grown-up controlled**, with a per-activity policy (child taps / unlock first / grown-up only). | Owner's requirement: caregiver decides who may start the next activity, and the child should not be able to skip. | decision |
| 13 | Grown-up gesture: press-and-hold a distinct dot top-left. | Stops stray toddler taps. Deliberately visible to the caregiver. Placeholder; may become a two-finger hold or pattern. | hypothesis |
| 14 | Carry-along phone, locked to the app. | A child moves between rooms; the display must travel. Screen time is acceptable because the screen provides real value. | decision |
| 15 | Quick timer for ad-hoc time (5/10/15 more minutes, or a new preset). | Real life has moments with no routine (playground). | decision |
| 16 | Act on pointer release, no text selection. | In the prototype a drag that began on text silently cancelled taps. | solid (observed bug) |
| 17 | Kiosk: screen pinning (`startLockTask`, no device-owner), self-unpinned by the app via `stopLockTask()` behind the grown-up gesture. | Real-device spike (Moto G14, Android 14, `docs/spikes/01-kiosk-screen-pinning.md`): blocks Home/Recents/Back/shade; app can exit cleanly without teaching the caregiver an OS gesture. Requires "App pinning" to be turned on in Settings first (off by default) — needs a setup-flow step. | solid (device-tested), pending toddler hands-on check |
| 18 | Boot id for the run anchor = `Settings.Global.BOOT_COUNT`. | Real-device spike confirmed it increments exactly once per reboot, needs no permission, and correctly drove the wall-clock fallback across a real reboot (`docs/spikes/02-timekeeping.md`). | solid |
| 19 | Chime plays on `STREAM_MUSIC`, unaffected by ringer silent/vibrate mode. | Confirmed via `dumpsys audio`: `STREAM_MUSIC` is not in the ringer-mode-affected stream group on the test device (`docs/spikes/03-audio-focus.md`). Behaviour under Do Not Disturb "Total silence" is still open. | solid |
| 20 | A quick timer (or other interruption) **pauses** the current activity and it **auto-resumes** with its remaining time when the quick timer ends; the routine then continues as before. Nested interruptions are not allowed. An activity already in `transition` is not paused (for now). | Owner: using up the current activity's time without the caregiver knowing is worse. Supersedes SPEC section 10's old "not resumed" line. | decision |
| 21 | Quick timers are presets only: no custom name or photo. | Owner: this is something the caregiver does on the spot, with minimal setup, so the focus stays on the child. | decision |
| 22 | Ad-hoc (quick-timer) entries do not add dots to the day strip. | The dots crowd the strip. | decision |
| 23 | "Sleep time" (the final activity) gets **no chime** and a **slow fade to dark that starts immediately** when it appears. | Owner. The fade takes 5 seconds. | decision |
| 24 | ~~Separate grace period when the screen turns red.~~ Superseded by decision 26: "More time" covers it. | Owner: they are the same thing. | superseded |
| 25 | The routine persists between sessions (and, later, photos and clips). | Owner. | decision |
| 26 | "More time on this activity" extends the current activity, is caregiver-only (behind the gate), and can be started before the activity ends or after it has ended (from the red screen). | Owner. Durations 5/10/15 min for now. Progress is measured against the new, longer total, so extending from red drops back (8 min + 5 min is about 62%, amber). | decision |
| 27 | When a quick timer ends: chime, screen goes red, then the interrupted activity **auto-resumes after 5 seconds** with no interaction. | Owner. Keeps the child out of the loop; the interrupted activity was already started, so no handover policy applies. | decision |
| 28 | "Something else now" is **not offered** while a quick timer is running or on its red screen, including a quick timer that was started from `transition` (and so has nothing to resume). | Owner. Keeps "no nesting" a simple rule: no quick-timer section anywhere in the sheet until the routine is back. | decision |
| 29 | An interrupted activity that the grown-up had **paused** before the quick timer **comes back running** when it auto-resumes. | Owner. Auto-resume needs no interaction (decision 27), so it cannot wait for a Resume tap. | decision |

## Things the prototype does that the app should NOT copy
See the list in `CLAUDE.md`. In particular, the prototype counts time in 100 ms ticks and starts the demo 20% in.
