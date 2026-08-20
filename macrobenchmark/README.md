# Macrobenchmark & baseline profiles

`BaselineProfileGenerator` records the classes and methods the app touches while starting up.
`StartupBenchmark` measures cold start with and without the resulting profile.

The rules are checked in per module. AGP packages `<sdk-module>/src/main/baseline-prof.txt` into that
module's AAR, so **customer apps get the SDK's profile automatically**;
`test-app/src/main/baseline-prof.txt` holds everything no SDK module owns. At build time AGP merges
them all into `assets/dexopt/baseline.prof` inside the APK.

## Measuring

Macrobenchmark needs an API 28–34 device, rooted or a userdebug build. Nothing above API 34 works
under AGP 7.3.1: `profileinstaller` 1.3.1 refuses to install the profile, 1.4.x cannot be dexed, and
on API 36 `pgrep` truncates the process name so Macrobenchmark never finds the app.

```bash
./gradlew :test-app:assembleBenchmark :macrobenchmark:assembleBenchmark
adb install -r test-app/build/outputs/apk/benchmark/test-app-benchmark.apk
adb install -r -t macrobenchmark/build/outputs/apk/benchmark/macrobenchmark-benchmark.apk

adb shell am instrument -w -r --no-window-animation \
  -e androidx.benchmark.suppressErrors EMULATOR \
  -e class com.splunk.android.sr.macrobenchmark.StartupBenchmark \
  com.splunk.android.sr.macrobenchmark/androidx.test.runner.AndroidJUnitRunner
```

Drop `suppressErrors` on a physical device. Gradle's `connectedBenchmarkAndroidTest` is unreliable
here because the project sets `useUnifiedTestPlatform=false`.

On a newer or unrooted device use the fallback instead. It switches compilation state with
`cmd package compile` and times launches with `am start -W`, so it needs no root, no instrumentation
APK and no `ProfileInstaller`:

```bash
ROUNDS=3 ITERS=7 macrobenchmark/measure-on-device.sh
```

Three things distort the numbers:

- Comparing modes from different invocations. The same mode has drifted 15 % between runs on an
  emulator, several times the effect being measured.
- Launching the app by hand right before measuring. A hot page cache once turned a 1356 ms median
  into 452 ms.
- The splash screen. `MainActivity` holds it for a fixed second, which would hide everything the
  profile improves, so the benchmark passes a `skipSplashScreenHold` extra that the activity honours.
  Nothing else about the launch changes.

## Regenerating the profiles

```bash
adb shell am instrument -w -r --no-window-animation \
  -e androidx.benchmark.suppressErrors EMULATOR \
  -e class com.splunk.android.sr.macrobenchmark.BaselineProfileGenerator \
  com.splunk.android.sr.macrobenchmark/androidx.test.runner.AndroidJUnitRunner

adb pull "/storage/emulated/0/Android/media/com.splunk.android.sr.macrobenchmark/BaselineProfileGenerator_startup-baseline-prof.txt" \
  test-app/src/main/baseline-prof.txt

macrobenchmark/split-baseline-profile.sh
```

The script moves each rule to the module whose namespace owns the class and leaves the rest with the
app.

## Results

Xiaomi 25010PN30G, Android 16, via `measure-on-device.sh`, three interleaved rounds of seven
iterations:

| Compilation | Median | Min | Max | vs. no AOT |
| --- | --- | --- | --- | --- |
| no AOT | 139 ms | 135 ms | 188 ms | — |
| profile-guided AOT | 121 ms | 113 ms | 143 ms | **−13 %** |
| full AOT | 130 ms | 126 ms | 141 ms | −6 % |

Every round gave the same ordering and the spread within a mode is smaller than the gap between
modes. Full AOT landing *between* the other two is the interesting part: compiling everything is
worse than compiling just the startup path. The page cache stays warm across iterations, so the
absolute numbers are optimistic — trust the comparison, not the milliseconds.

The script measures ART's own profile, collected by replaying the generator's journey, because
installing the checked-in profile is exactly what API 36 blocks.

An API 34 emulator agrees but far more noisily: −11.0 % and −2.5 % in two runs, with full compilation
again slower than no compilation at all.
