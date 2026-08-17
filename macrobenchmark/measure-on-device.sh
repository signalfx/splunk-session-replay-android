#!/bin/bash
# Cold start A/B on a device that Macrobenchmark cannot drive. See README.md for when that applies;
# in short, on API 35+ `profileinstaller` 1.3.1 refuses to install the profile and on API 36
# `pgrep -l -f` truncates the process name so Macrobenchmark cannot find the app.
#
# Instead of Macrobenchmark this drives the compilation state with `cmd package compile` and times
# the launch with `am start -W`, neither of which needs root or an instrumentation APK.
#
# The `speed-profile` state compiles against the profile ART collected itself, not against the
# checked-in baseline profile, because installing that one is exactly what is blocked here. Warm the
# profile first with the same journey BaselineProfileGenerator records, so that the two are
# comparable:
#
#   for i in $(seq 8); do
#     adb shell am force-stop com.splunk.android.sr.testapp
#     adb shell am start -n com.splunk.android.sr.testapp/.ui.MainActivity; sleep 3
#     adb shell input swipe 540 1800 540 700 120; sleep 2
#   done
#   sleep 25   # let the profile saver flush
#
# Usage: ROUNDS=3 ITERS=7 macrobenchmark/measure-on-device.sh
set -uo pipefail

PKG=com.splunk.android.sr.testapp
ACT="$PKG/.ui.MainActivity"
ROUNDS=${ROUNDS:-3}
ITERS=${ITERS:-7}
MODES="verify speed-profile speed"
OUT=$(mktemp -d)

adb shell input keyevent KEYCODE_WAKEUP >/dev/null 2>&1
adb shell wm dismiss-keyguard >/dev/null 2>&1

# One cold launch, printed as milliseconds to first frame. The extra makes MainActivity skip its
# fixed one second splash screen hold, which would otherwise swamp the difference being measured.
launch() {
	adb shell am force-stop $PKG >/dev/null 2>&1
	sleep 2
	adb shell "am start -W -n $ACT --ez skipSplashScreenHold true" 2>/dev/null |
		grep -E "^TotalTime:" | awk '{print $2}' | tr -d '\r'
}

median() {
	sort -n | awk '{v[NR]=$1} END {if (NR==0) {print "n/a"; exit} print (NR%2) ? v[(NR+1)/2] : (v[NR/2]+v[NR/2+1])/2}'
}

# Rounds interleave the modes so that thermal drift hits all of them equally.
for round in $(seq 1 "$ROUNDS"); do
	for mode in $MODES; do
		adb shell cmd package compile -m "$mode" -f $PKG >/dev/null 2>&1
		# The first launch after recompiling repopulates caches; do not measure it.
		launch >/dev/null

		line=""
		for _ in $(seq 1 "$ITERS"); do
			t=$(launch)
			if [ -n "$t" ]; then
				echo "$t" >> "$OUT/$mode"
				line="$line $t"
			fi
		done
		echo "round $round  $(printf '%-13s' "$mode")$line"
	done
done

echo
printf '%-14s %8s %8s %8s %4s\n' "mode" "median" "min" "max" "n"
for mode in $MODES; do
	printf '%-14s %8s %8s %8s %4s\n' "$mode" \
		"$(median < "$OUT/$mode")" \
		"$(sort -n "$OUT/$mode" | head -1)" \
		"$(sort -n "$OUT/$mode" | tail -1)" \
		"$(grep -c . "$OUT/$mode")"
done
echo
echo "raw samples in $OUT"
