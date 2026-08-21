#!/usr/bin/env python3
"""Runs the instrumented tests of test-app on every available emulator.

Boots one AVD at a time, runs the tests and shuts the emulator down.

Usage:
    python3 tools/emulator_matrix.py                    # every AVD
    python3 tools/emulator_matrix.py Android_14         # selected AVDs only
"""

import os
import pathlib
import subprocess
import sys
import time

ROOT = pathlib.Path(__file__).resolve().parent.parent
LOGS = ROOT / "build/emulator-matrix"
SDK = pathlib.Path(os.environ.get("ANDROID_HOME", pathlib.Path.home() / "Library/Android/sdk"))
EMULATOR = SDK / "emulator/emulator"
ADB = SDK / "platform-tools/adb"

BOOT_TIMEOUT_S = 420
SHUTDOWN_TIMEOUT_S = 60

def adb(*arguments, timeout=60):
    return subprocess.run([str(ADB), *arguments], capture_output=True, text=True, timeout=timeout)

def list_avds():
    result = subprocess.run([str(EMULATOR), "-list-avds"], capture_output=True, text=True)
    return [line.strip() for line in result.stdout.splitlines() if line.strip()]

def boot(avd, log_file):
    with log_file.open("w") as log:
        process = subprocess.Popen(
            [str(EMULATOR), "-avd", avd, "-no-snapshot", "-no-boot-anim", "-no-audio", "-gpu", "swiftshader_indirect"],
            stdout=log,
            stderr=subprocess.STDOUT,
        )

    deadline = time.time() + BOOT_TIMEOUT_S

    while time.time() < deadline:
        if process.poll() is not None:
            return None, "the emulator exited during boot"

        if adb("shell", "getprop", "sys.boot_completed").stdout.strip() == "1":
            adb("shell", "input", "keyevent", "82")
            for scale in ("window_animation_scale", "transition_animation_scale", "animator_duration_scale"):
                adb("shell", "settings", "put", "global", scale, "0")
            return process, None

        time.sleep(5)

    return process, f"the emulator did not boot within {BOOT_TIMEOUT_S}s"

def shutdown(process):
    adb("emu", "kill")

    try:
        process.wait(timeout=SHUTDOWN_TIMEOUT_S)
    except subprocess.TimeoutExpired:
        process.kill()
        process.wait(timeout=SHUTDOWN_TIMEOUT_S)

def test(avd):
    LOGS.mkdir(parents=True, exist_ok=True)

    process, error = boot(avd, LOGS / f"{avd}-emulator.log")

    if error is not None:
        if process is not None:
            shutdown(process)
        return "BOOT FAIL", error

    api_level = adb("shell", "getprop", "ro.build.version.sdk").stdout.strip()

    try:
        log_file = LOGS / f"{avd}-test.log"

        with log_file.open("w") as log:
            result = subprocess.run(
                ["./gradlew", ":test-app:connectedDebugAndroidTest"],
                cwd=ROOT,
                stdout=log,
                stderr=subprocess.STDOUT,
                text=True,
            )

        if result.returncode == 0:
            return f"PASS (API {api_level})", ""

        failures = [line.strip() for line in log_file.read_text().splitlines() if "AssertionError" in line]

        return f"FAIL (API {api_level})", "; ".join(failures[:2])
    finally:
        shutdown(process)

def main():
    requested = sys.argv[1:]
    avds = [it for it in list_avds() if not requested or it in requested]

    if not avds:
        sys.exit(f"No AVD matches {requested}. Available: {list_avds()}")

    results = []

    for avd in avds:
        print(f"==> {avd}", flush=True)
        started = time.time()
        status, detail = test(avd)
        results.append((avd, status, detail, time.time() - started))
        print(f"    {status} in {time.time() - started:.0f}s {detail}", flush=True)

    print("\n=== SUMMARY ===")
    for avd, status, detail, duration in results:
        print(f"{status:16} {avd:12} {duration:4.0f}s {detail}")

    sys.exit(1 if any(not status.startswith("PASS") for _, status, _, _ in results) else 0)


if __name__ == "__main__":
    main()
