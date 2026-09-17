#!/bin/bash
# Splits the single profile produced by BaselineProfileGenerator across the modules that own the
# rules: everything under an SDK module's namespace moves to that module, the rest stays with the
# test app.
#
# Usage: split-baseline-profile.sh [generated-profile]
# The generated profile defaults to test-app/src/main/baseline-prof.txt, which is where the
# regeneration step in README.md pulls it to.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_PROFILE="$ROOT/test-app/src/main/baseline-prof.txt"
GENERATED="${1:-$APP_PROFILE}"

if [[ ! -f "$GENERATED" ]]; then
	echo "No generated profile at $GENERATED. Pull it from the device first." >&2
	exit 1
fi

# module:class-prefix
MODULES=(
	"bridge:com/splunk/android/bridge"
	"core:com/splunk/android/instrumentation/recording/core"
	"debugger:com/splunk/android/debugger"
	"frame-capturer:com/splunk/android/instrumentation/recording/capturer"
	"interactions:com/splunk/android/instrumentation/recording/interactions"
	"screenshot:com/splunk/android/instrumentation/recording/screenshot"
	"wireframe:com/splunk/android/instrumentation/recording/wireframe"
)

owned_prefixes=""
for entry in "${MODULES[@]}"; do
	owned_prefixes="${owned_prefixes:+$owned_prefixes|}${entry#*:}"
done

# Splitting an already split file would overwrite every module profile with an empty one.
if ! grep -qE "^[HSP]*L($owned_prefixes)/" "$GENERATED"; then
	echo "$GENERATED contains no SDK rules; it looks already split. Nothing to do." >&2
	exit 1
fi

input=$(mktemp)
trap 'rm -f "$input"' EXIT
cp "$GENERATED" "$input"

for entry in "${MODULES[@]}"; do
	module="${entry%%:*}"
	prefix="${entry#*:}"

	grep -E "^[HSP]*L$prefix/" "$input" > "$ROOT/$module/src/main/baseline-prof.txt" || true
	printf '%-16s %5s rules\n' "$module" "$(wc -l < "$ROOT/$module/src/main/baseline-prof.txt" | tr -d ' ')"
done

# Keep only the rules no library module owns; those ship with the app itself.
grep -v -E "^[HSP]*L($owned_prefixes)/" "$input" > "$APP_PROFILE"
printf '%-16s %5s rules\n' "test-app" "$(wc -l < "$APP_PROFILE" | tr -d ' ')"
