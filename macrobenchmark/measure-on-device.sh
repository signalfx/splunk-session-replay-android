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
# Usage: ROUNDS=3 ITERS=7 ATTEMPTS=3 macrobenchmark/measure-on-device.sh
set -uo pipefail

PKG=com.splunk.android.sr.testapp
ACT="$PKG/.ui.MainActivity"
ROUNDS=${ROUNDS:-3}
ITERS=${ITERS:-7}
ATTEMPTS=${ATTEMPTS:-3}
MODES=(verify speed-profile speed)
OUT=$(mktemp -d)

fail() {
	echo "$*" >&2
	echo "aborted; partial samples in $OUT" >&2
	exit 1
}

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

# A mode measured from fewer samples than the others is not comparable with them, and a launch drops
# out precisely when the device is struggling, so retry rather than let the sample silently vanish.
timed_launch() {
	local attempt t
	for attempt in $(seq 1 "$ATTEMPTS"); do
		t=$(launch)
		if [ -n "$t" ]; then
			printf '%s' "$t"
			return 0
		fi
		echo "  no TotalTime from am start (attempt $attempt/$ATTEMPTS), retrying" >&2
	done
	return 1
}

median() {
	sort -n | awk '{v[NR]=$1} END {if (NR==0) {print "n/a"; exit} print (NR%2) ? v[(NR+1)/2] : (v[NR/2]+v[NR/2+1])/2}'
}

# Devices reject compiler filters for all sorts of reasons: shell restrictions, vendor policy, an
# unsupported filter. Launches after a rejected call keep the previous compilation state while being
# recorded under the requested mode, so the run has to stop instead of reporting a mislabelled
# comparison. Some builds report the refusal on stdout with a zero exit status, hence both checks.
compile_as() {
	local mode=$1 output
	output=$(adb shell "cmd package compile -m $mode -f $PKG" 2>&1 | tr -d '\r') ||
		fail "adb could not run 'cmd package compile -m $mode' on the device"
	case "$output" in
	*Success*) ;;
	*) fail "device refused compilation mode '$mode': ${output:-no output}" ;;
	esac
}

# Modes rotate by one position each round. Under a fixed order a device that heats up or throttles
# within a round penalises whichever mode always runs last, which is indistinguishable from a
# compilation effect; rotating spreads that cost across all of them instead.
round_modes() {
	local count=${#MODES[@]}
	local offset=$((($1 - 1) % count))
	local i
	for ((i = 0; i < count; i++)); do
		printf '%s\n' "${MODES[(offset + i) % count]}"
	done
}

for round in $(seq 1 "$ROUNDS"); do
	for mode in $(round_modes "$round"); do
		compile_as "$mode"
		# The first launch after recompiling repopulates caches; do not measure it.
		launch >/dev/null

		line=""
		for iter in $(seq 1 "$ITERS"); do
			t=$(timed_launch) ||
				fail "no valid timing for '$mode' in round $round, iteration $iter after $ATTEMPTS attempts"
			echo "$t" >> "$OUT/$mode"
			line="$line $t"
		done
		echo "round $round  $(printf '%-13s' "$mode")$line"
	done
done

echo
printf '%-14s %8s %8s %8s %4s\n' "mode" "median" "min" "max" "n"
for mode in "${MODES[@]}"; do
	printf '%-14s %8s %8s %8s %4s\n' "$mode" \
		"$(median < "$OUT/$mode")" \
		"$(sort -n "$OUT/$mode" | head -1)" \
		"$(sort -n "$OUT/$mode" | tail -1)" \
		"$(grep -c . "$OUT/$mode")"
done
echo
echo "raw samples in $OUT"
