# Heartbeat (chest accelerometer BPM)

A minimal Android app: lay the phone flat on your bare chest, screen up, and
hold still. It reads the accelerometer, picks out the tiny periodic vibration
each heartbeat makes in the chest wall (seismocardiography), and turns the
last few beat-to-beat gaps into a live BPM estimate.

This is **not a medical device** and is nowhere near as accurate as a
finger-over-camera+flash pulse app or a real pulse oximeter/ECG — chest
accelerometer signals are weak and very sensitive to motion, breathing, and
how firmly/where the phone is pressed. Treat it as a fun demo, not a health
reading.

## How it works

`src/com/example/heartbeat/MainActivity.java`:

1. Registers a `TYPE_LINEAR_ACCELERATION` sensor listener (falls back to
   `TYPE_ACCELEROMETER` if unavailable) at the fastest sampling rate.
2. Takes the magnitude of the 3-axis vector each sample.
3. Runs two exponential moving averages over it — a fast one that tracks the
   raw signal and a slow one that tracks the drifting baseline (breathing,
   posture). Their difference is a crude band-pass filter that isolates the
   quick heartbeat blip from slower motion.
4. Tracks a noise-adaptive threshold (based on the running variance of that
   filtered signal) and calls anything that crosses it — outside a 300ms
   refractory window, to avoid double-counting one beat — a detected beat.
5. Keeps the last 8 beat-to-beat intervals, discards outliers (>40% off the
   median), averages the rest, and converts to BPM. Once at least 4 beats
   have been seen, it shows a live, exponentially-smoothed BPM; before that
   it shows a "Calibrating… N/4 beats" message.
6. If a sample spikes far past the noise floor (phone being picked up/moved),
   it's treated as motion, not a heartbeat, and the UI says so.

No network access, no permissions beyond the accelerometer (which needs none
at the OS level), no background service — it only runs while the app is in
the foreground.

## Building

Requires Java (`javac`, `keytool`) and, on Debian/Ubuntu:

```
sudo apt-get install -y android-sdk-build-tools android-sdk-platform-23 aapt apksigner
```

That package set ships `aapt`, `aidl`, `zipalign`, `apksigner`, and
`android.jar` for API 23 — but Debian/Ubuntu's build-tools package does not
include a dex compiler (`dx`/`d8`). `build.sh` fetches Google's standalone R8
jar (which contains D8) directly from R8's public release bucket to fill that
one gap. No Gradle or Android Studio involved.

```
./build.sh
```

Output: `build/apk/heartbeat-debug.apk`, signed with a throwaway debug key
generated on first run (`build/debug.keystore`) so it can be installed
directly.

## Installing

Copy the APK to a phone and open it (or `adb install build/apk/heartbeat-debug.apk`
over USB with Developer Options / USB debugging enabled). You'll need to allow
"install unknown apps" for whichever app you use to open the file, since it's
not from an app store.

Targets `minSdkVersion 19`, `targetSdkVersion 34` — recent Android releases
refuse to sideload apps that declare an old target SDK (Android 14 started
blocking targets below API 23 and the floor keeps rising), so the manifest
declares a modern target even though the code only uses ancient APIs. The
`HIGH_SAMPLING_RATE_SENSORS` permission is declared because targetSdk 31+
otherwise caps accelerometer sampling at 200 Hz.
