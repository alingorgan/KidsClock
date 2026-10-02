# KidsClock behaviour spec

Platform-neutral. Values marked **(tunable)** are starting points to test with children, not fixed truths. Source of truth for exact numbers: `prototype/now-next-v2.html`.

## 1. Product in one paragraph
A phone the child carries shows the **current activity** as one large picture, **how much of it is left** as a shrinking circle plus a full-screen colour, and **what comes next** as a small dimmed picture. The caregiver sets up activities and durations, and controls handovers. The child never needs to read time or numbers.

## 2. Modes
1. **Setup** (caregiver): child's age, activities, durations, photo/clip per activity, handover policy per activity, progress colour, end sound.
2. **Run** (child-facing): the display described below. Caregiver controls are behind a hidden gesture.

## 3. Age setting
| Setting | Circle shows | "Next" | Day strip |
|---|---|---|---|
| Under 3 | Line pictogram only | Hidden until the caregiver reveals it, or time is up | No |
| 3 and over | Photo if set (else pictogram); play badge if a clip exists | Always visible, dimmed | Yes: one dot per activity, current highlighted, finished dimmed |

Photos/clips stay stored but unused for "Under 3".

## 4. Run screen anatomy (portrait)
- **Activity bubble**: solid circle in the activity's colour, 46% of the orb width, centred. Contains pictogram or photo.
- **Progress bubble**: a larger translucent circle behind it. Its size goes from 100% to 46% of the orb width as the activity runs (area shrinks to meet the activity bubble). Scale = 0.46 + 0.54 × (1 − progress). Shows no numbers.
- **Activity name** under the orb, large, rounded font.
- **Next tile**: bottom-right. Small picture of the next activity with the label "Next" and its name. Dimmed (about 50%) while an activity runs.
- **Status line** under the name: empty while running.
- **Grown-up gate**: small distinct dot, top-left (see §9).
- **Day strip**: top row (3 and over only).

## 5. Activity phases
- `active`: timer running (or paused by the caregiver).
- `transition`: time is up. Progress bubble fully shrunk, screen red, chime plays. Waiting for the handover to the next activity.
- `final`: the last item. No timer, no progress bubble. Shows its own word and prompt (e.g. "Goodnight", or "All done!"). No chime. "Sleep time" fades slowly to dark, starting immediately when it appears over 5 seconds (decision 23).

Progress = elapsed ÷ duration, from 0 to 1. Reaching 1 enters `transition`.

## 6. Progress colour (three modes, setup option, default "Whole screen")
- **Whole screen (default)**: the whole background is a radial gradient (centre at 50% width, 42% height) whose colour follows progress. Centre is mixed into the neutral stage colour at 34% + 24% × progress; the edge at 56% + 38% × progress, so the edges are stronger than the centre. The progress bubble becomes neutral translucent white (about 42%). Text uses the normal ink colour for legibility.
- **Ring around the picture**: activity background stays tinted in the activity colour; the progress bubble takes the traffic colour, and a 9 px ring in that colour surrounds the activity bubble.
- **Activity colour only**: no traffic colouring; background is a light tint of the activity colour.

Traffic colour stops by progress **(tunable)**: 0.00–0.30 green rgb(38,182,96) → 0.65 amber rgb(240,180,30) → 1.00 red rgb(224,44,36). Linear blend between stops. The final activity uses no traffic colour.

Colour is a bonus signal only. Shape and sound carry the same information (red-green colour deficiency is common in boys).

## 7. Sounds
Setup options: **Gentle chime** (default), **Chime, repeats every 20 s**, **No sound**. Plus a checkbox **Soft note when nearly done** (default on).
- Nearly done: at progress ≥ 0.85, one soft bell note (C5, low volume) **(tunable)**. The picture also "breathes" slowly (scale 1 ↔ 1.07, 3.2 s cycle).
- Time up: four ascending bell notes C5, E5, G5, C6, 0.28 s apart, each with a long soft decay (about 2.2 s) **(tunable)**. A bell is a sine with a quieter partial at about 2.01× frequency.
- Repeat mode: replay the time-up chime every 20 s while still in `transition`. Stop when the caregiver opens the sheet or the next activity starts.
- It is a chime, not an alarm, on purpose (startle and arousal distort children's time sense). Plan quiet hours and volume for the app.

## 7b. Breathing and reduced motion
Respect the system "reduce motion" setting: no breathing, no pulsing, no scale animations.

## 8. Handover to the next activity
Each activity (except the first) has a **starting policy**, set in setup:
1. **Child taps**: in `transition`, the Next tile grows and pulses; a child tap starts the next activity.
2. **Grown-up unlocks, child taps**: the child's tap does nothing until the caregiver taps "Let child start next" in the sheet; then it behaves as (1).
3. **Grown-up only**: only the caregiver's "Start …" button starts it.

Taps before `transition`, or when locked, do nothing to the routine but give a short line: "Not yet. Watch the circle get small." / "Your grown-up will help with this one." When the grown-up is needed, the gate dot pulses in a warning colour.

**Input handling:** act on pointer *release*, not click, so a stray drag or text selection cannot swallow a tap. No text selection on the child screen. The **Next tile is the only child tap target**; taps elsewhere on the child screen do nothing. Its hit area extends about 16 dp beyond the visible tile, so a small hand does not have to be exact.

Default demo policies: Playtime first; Tidy up = child taps; Bath = unlock; Teeth = child taps; Story = grown-up only.

## 9. Grown-up sheet and gate
- **Gate**: press and hold the top-left dot for about 1.2 s to open a bottom sheet **(tunable, may change to a two-finger or pattern gesture)**. A progress ring fills while holding. Keyboard/accessibility activation opens it immediately.
- **Sheet contents**:
  - "Say together": a sentence to read aloud and a question to ask, using the colour words when traffic colour is on, e.g. "We are playing. The big circle is getting smaller and the screen is green. When it is red, we are out of time."
  - Minutes left (caregiver only; never shown to the child).
  - Pause / Resume, "Show what's next" (Under 3 only), "Let child start next" (when policy 2 applies), "Start <next>", Restart, Back to setup.
  - Quick timer (§10).

## 10. Quick timer (ad-hoc time)
For moments with no routine or a change of plan (e.g. the playground).
- **More time on this activity** (caregiver only, decision 26): "5 / 10 / 15 more min". Extends the current activity by that long; nothing is inserted and nothing is paused. Available before the activity ends and after it has ended (from `transition`, it returns the activity to `active`). Progress is measured against the new, longer total, so it drops back (8 min + 5 min is at about 62%, amber). This replaces the separate grace period (decision 24).
- **Something else now**: preset activity + minutes (5, 10, 15, 20, 30) + Start. Presets: Playground, Outside time, Free play, Snack time. Inserted after the current activity; the routine continues after it.
- **Quick timer only** (from setup, no routine): choose activity + minutes; runs that one timer, then an "All done!" screen with a check mark that the child can tap.
- Quick-timer entries are temporary; going back to setup or restarting removes them.
- **Interruption pauses, not consumes (decision 20):** "Something else now" pauses the interrupted activity with its remaining time, and when the quick timer ends the chime plays and the screen goes red (`transition`), then the interrupted activity auto-resumes after 5 seconds with no interaction (decision 27); the routine then continues as before. No nested interruptions: "Something else now" is not offered while a quick timer is running or on its red screen (decision 28), including one started from `transition`. An activity already in `transition` is not paused. An interrupted activity that was paused before the interruption comes back running (decision 29). 
- Quick timers are presets only (no custom name or photo, decision 21) and do not add dots to the day strip (decision 22).

## 11. Photo and clip (3 and over)
- Tapping the activity bubble opens the photo enlarged (the circle grows to a rounded card over the screen). If a clip exists it plays there. It closes by itself: photo after about 6 s, clip when it ends. Tapping closes it early.
- The timer keeps running while media is open.
- Caregiver sets a photo and a clip per activity in setup (camera or gallery). Ideas to support: the child's own toothbrush, the place where it happens (the sink), or a recording of the child doing it.
- Media is local only. See privacy rule in `CLAUDE.md`.

## 12. Default routine (example content for first-run)
Evening: Playtime 8 min, Tidy up 3, Bath time 10, Brush teeth 3, Story time 8, Sleep time (final, "Goodnight"). Activity colours: amber `#D98214`, green `#3E9A62`, blue `#2F86CC`, teal `#1E9C96`, purple `#8462C2`, indigo `#33448A`. Pictograms are simple white shapes on the coloured bubble (blocks, box, tub, toothbrush, book, moon); presets add swing, apple, sun and check mark.

## 13. Not in scope yet
Clock face or numbers for the child, ages above about 4½, custom activity names/photos in a quick timer, multi-day scheduling, cloud sync/accounts, iOS.
