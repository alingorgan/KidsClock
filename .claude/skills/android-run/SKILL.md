---
name: android-run
description: Build KidsClock, install it on the Android emulator (AVD Pixel_3a_API_32_arm64-v8a) or a connected device, launch it and take a screenshot. Use when asked to run, try or screenshot the app.
---

`adb` and `emulator` are not on PATH. Use:
```
SDK=~/Library/Android/sdk; ADB=$SDK/platform-tools/adb
```

1. Device: `$ADB devices`. If none, boot the AVD in the background and wait:
   `nohup $SDK/emulator/emulator -avd Pixel_3a_API_32_arm64-v8a -no-snapshot -no-audio > /tmp/emu.log 2>&1 &`
   then `$ADB wait-for-device` and poll `$ADB shell getprop sys.boot_completed` until `1`.
2. Install: `./gradlew :app:installDebug`
3. Launch: `$ADB shell am start -n com.kidsclock/.MainActivity`
4. Wait ~3 s (the emulator is slow to draw the first frame), then screenshot:
   `mkdir -p build/screenshots && $ADB exec-out screencap -p > build/screenshots/run.png` and read the PNG.
5. Elements on screen (by resource-id, thanks to `testTagsAsResourceId`): `$ADB shell uiautomator dump /sdcard/u.xml && $ADB shell cat /sdcard/u.xml`; look for `resource-id="run.title"` etc. See `docs/UI_AUTOMATION.md` for the id convention.
6. Privacy check: `$ADB shell dumpsys package com.kidsclock | grep -i INTERNET` must print nothing.
7. Crashes: `$ADB logcat -d -s AndroidRuntime:E`

8. Snapshots: `./gradlew recordRoborazziDebug` re-records baselines after an intended visual change; `./gradlew check` verifies them.
9. On-device UI tests (Compose/Espresso + UiAutomator): `./gradlew :app:connectedDebugAndroidTest` (needs a device from step 1).
10. Maestro (if installed): `maestro test .maestro/smoke.yaml`.
