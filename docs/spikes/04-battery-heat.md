# Spike (d): Keep-awake battery and heat

Device: Moto G14, Android 14, on the charger throughout (owner's phone was plugged in when this
was run). `MainActivity` (which sets `FLAG_KEEP_SCREEN_ON`) was kept in the foreground for 30
minutes; `dumpsys battery`'s level and temperature were sampled every 5 minutes.

## Results

| t (min) | Battery level | Temperature |
|---|---|---|
| 0 | 90% | 31.6°C |
| 5 | 92% | 31.4°C |
| 10 | 94% | 31.2°C |
| 15 | 96% | 30.5°C |
| 20 | 98% | 29.9°C |
| 25 | 99% | 28.8°C |
| 30 | 99% | 28.1°C |

Charging the whole time (`AC powered: true`, `status: charging`), so this is a direct answer to the
"overnight charging behaviour" half of `OPEN_QUESTIONS.md` #14: the phone charged normally with the
screen continuously on and awake, and actually got **cooler** over the 30 minutes (31.6°C → 28.1°C)
rather than warmer — no sign of the screen-on + charging combination causing heat buildup on this
device.

## Still open

This run doesn't answer the *unplugged* screen-on-battery-drain question — the device happened to
be on the charger for the whole session, so level only ever went up. Getting real unplugged-drain
numbers needs a separate ~30 minute run with the phone off the charger, which wasn't done here to
avoid tying up the owner's device further. Worth a quick follow-up run whenever convenient; nothing
in this result suggests a problem, just that it's unmeasured.

## Recommendation

No action needed from this run — charging-while-running looks fine on this device. Recommend a
short unplugged follow-up before treating `OPEN_QUESTIONS.md` #14 as fully closed.
