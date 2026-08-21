#!/usr/bin/env python3
"""Runs the instrumented tests of test-app against every supported Jetpack Compose version.

The Compose version dictates the toolchain, see the table in docs/compose-compatibility.md. The build files are patched in place and
restored from git when the run finishes.

Usage:
    python3 tools/compose_matrix.py                     # every version
    python3 tools/compose_matrix.py 1.7.8 1.10.6        # selected versions only
"""

import collections
import pathlib
import re
import subprocess
import sys
import time

ROOT = pathlib.Path(__file__).resolve().parent.parent
LOGS = ROOT / "build/compose-matrix"
PATCHED_FILES = [
    "buildSrc/src/main/kotlin/Dependencies.kt",
    "buildSrc/src/main/kotlin/Configurations.kt",
    "buildSrc/build.gradle.kts",
    "gradle/wrapper/gradle-wrapper.properties",
]

Configuration = collections.namedtuple(
    "Configuration",
    "compose_ui foundation material_icons activity compiler kotlin agp gradle compile_sdk app_min_sdk",
)

KOTLIN_1_7 = dict(compiler="1.3.2", kotlin="1.7.20")
KOTLIN_1_9 = dict(compiler="1.5.0", kotlin="1.9.0")

AGP_7 = dict(agp="7.3.1", gradle="7.5.1")
AGP_8 = dict(agp="8.13.2", gradle="8.13")

OLD_SATELLITES = dict(material_icons="1.2.1", activity="1.3.1")
NEW_SATELLITES = dict(material_icons="1.7.8", activity="1.9.3")

MATRIX = [
    Configuration(compose_ui="1.2.1", foundation="1.2.1", compile_sdk="34", app_min_sdk="21", **OLD_SATELLITES, **KOTLIN_1_7, **AGP_7),
    Configuration(compose_ui="1.3.3", foundation="1.3.1", compile_sdk="34", app_min_sdk="21", **OLD_SATELLITES, **KOTLIN_1_7, **AGP_7),
    Configuration(compose_ui="1.4.3", foundation="1.4.3", compile_sdk="34", app_min_sdk="21", **OLD_SATELLITES, **KOTLIN_1_9, **AGP_7),
    Configuration(compose_ui="1.5.4", foundation="1.5.4", compile_sdk="34", app_min_sdk="21", **OLD_SATELLITES, **KOTLIN_1_9, **AGP_7),
    Configuration(compose_ui="1.6.8", foundation="1.6.8", compile_sdk="34", app_min_sdk="21", **OLD_SATELLITES, **KOTLIN_1_9, **AGP_7),
    Configuration(compose_ui="1.7.8", foundation="1.7.8", compile_sdk="34", app_min_sdk="21", **NEW_SATELLITES, **KOTLIN_1_9, **AGP_8),
    Configuration(compose_ui="1.8.3", foundation="1.8.3", compile_sdk="35", app_min_sdk="21", **NEW_SATELLITES, **KOTLIN_1_9, **AGP_8),
    Configuration(compose_ui="1.9.5", foundation="1.9.5", compile_sdk="36", app_min_sdk="21", **NEW_SATELLITES, **KOTLIN_1_9, **AGP_8),
    Configuration(compose_ui="1.10.6", foundation="1.10.6", compile_sdk="36", app_min_sdk="23", **NEW_SATELLITES, **KOTLIN_1_9, **AGP_8),
]

def run(command, log_file=None):
    if log_file is None:
        return subprocess.run(command, cwd=ROOT, capture_output=True, text=True)

    with log_file.open("w") as log:
        return subprocess.run(command, cwd=ROOT, stdout=log, stderr=subprocess.STDOUT, text=True)

def backup():
    """Keeps the contents of the patched files. Restoring them from git would throw away uncommitted work."""
    return {it: (ROOT / it).read_text() for it in PATCHED_FILES}

def restore(saved):
    for path, contents in saved.items():
        (ROOT / path).write_text(contents)

def resolved_compose_version():
    """Returns the Compose UI version that Gradle really puts on the runtime classpath of test-app."""
    result = run(
        [
            "./gradlew",
            "--quiet",
            ":test-app:dependencyInsight",
            "--configuration", "debugRuntimeClasspath",
            "--dependency", "androidx.compose.ui:ui",
        ]
    )

    matches = re.findall(r"androidx\.compose\.ui:ui(?:-android)?:([0-9][0-9.]*)", result.stdout)

    return matches[0] if matches else None

def test(configuration):
    result = run(
        [
            "python3",
            "tools/set_versions.py",
            "--compose-ui", configuration.compose_ui,
            "--foundation", configuration.foundation,
            "--material-icons", configuration.material_icons,
            "--activity", configuration.activity,
            "--compiler", configuration.compiler,
            "--kotlin", configuration.kotlin,
            "--agp", configuration.agp,
            "--gradle", configuration.gradle,
            "--compile-sdk", configuration.compile_sdk,
            "--app-min-sdk", configuration.app_min_sdk,
        ]
    )

    if result.returncode != 0:
        return "PATCH FAILED", result.stdout + result.stderr

    resolved = resolved_compose_version()

    if resolved != configuration.compose_ui:
        return "WRONG VERSION", f"expected {configuration.compose_ui} on the classpath, Gradle resolved {resolved}"

    log_file = LOGS / f"compose-{configuration.compose_ui}.log"
    result = run(["./gradlew", "--no-daemon", ":test-app:connectedDebugAndroidTest"], log_file)

    if result.returncode == 0:
        return "PASS", ""

    failures = [line.strip() for line in log_file.read_text().splitlines() if "AssertionError" in line or "FAILED" in line]

    return "FAIL", "; ".join(failures[:3])

def main():
    requested = sys.argv[1:]
    configurations = [it for it in MATRIX if not requested or it.compose_ui in requested]

    if not configurations:
        sys.exit(f"No configuration matches {requested}. Known versions: {[it.compose_ui for it in MATRIX]}")

    LOGS.mkdir(parents=True, exist_ok=True)
    saved = backup()
    results = []

    try:
        for configuration in configurations:
            print(f"==> compose {configuration.compose_ui} (kotlin {configuration.kotlin}, agp {configuration.agp})", flush=True)
            started = time.time()
            status, detail = test(configuration)
            results.append((configuration, status, detail, time.time() - started))
            print(f"    {status} in {time.time() - started:.0f}s {detail}", flush=True)
    finally:
        restore(saved)

    print("\n=== SUMMARY ===")
    for configuration, status, detail, duration in results:
        print(
            f"{status:13} compose-ui {configuration.compose_ui:7} kotlin {configuration.kotlin:7} "
            f"agp {configuration.agp:8} compileSdk {configuration.compile_sdk} {duration:4.0f}s {detail}"
        )

    sys.exit(1 if any(status != "PASS" for _, status, _, _ in results) else 0)

if __name__ == "__main__":
    main()
