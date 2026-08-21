#!/usr/bin/env python3
"""Switches the Compose / Kotlin / AGP / Gradle versions of the build.

Used to run the test suite against every supported Jetpack Compose version. See tools/compose_matrix.py.
"""

import argparse
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DEPENDENCIES = ROOT / "buildSrc/src/main/kotlin/Dependencies.kt"
CONFIGURATIONS = ROOT / "buildSrc/src/main/kotlin/Configurations.kt"
BUILD_SRC = ROOT / "buildSrc/build.gradle.kts"
WRAPPER = ROOT / "gradle/wrapper/gradle-wrapper.properties"

def replace_once(text, pattern, value, description):
    new_text, count = re.subn(pattern, lambda match: match.group(1) + value + match.group(2), text, count=1)

    if count != 1:
        sys.exit(f"Failed to patch {description}, {count} matches for {pattern!r}.")

    return new_text

def patch_compose(ui, foundation, material_icons, activity, compiler):
    text = DEPENDENCIES.read_text()
    block = re.search(r"object Compose \{.*?\n        \}", text, re.S)

    if block is None:
        sys.exit("The 'object Compose' block was not found in Dependencies.kt.")

    patched = block.group(0)
    patched = replace_once(patched, r'(const val version = ")[^"]+(")', ui, "compose ui version")
    patched = replace_once(patched, r'(private const val foundationVersion = ")[^"]+(")', foundation, "foundation version")
    patched = replace_once(patched, r'(private const val materialIconsVersion = ")[^"]+(")', material_icons, "material icons version")
    patched = replace_once(patched, r'(private const val activityVersion = ")[^"]+(")', activity, "activity version")
    patched = replace_once(patched, r'(const val compilerVersion = ")[^"]+(")', compiler, "compose compiler version")

    DEPENDENCIES.write_text(text[:block.start()] + patched + text[block.end():])

def patch_sdk_levels(compile_sdk, app_min_sdk):
    text = CONFIGURATIONS.read_text()
    text = replace_once(text, r"(const val appCompileVersion = )\d+(\n)", compile_sdk, "app compile sdk")
    text = replace_once(text, r"(const val appMinVersion = )\d+(\n)", app_min_sdk, "app min sdk")
    CONFIGURATIONS.write_text(text)

def patch_toolchain(kotlin, agp, gradle):
    text = BUILD_SRC.read_text()
    text = replace_once(text, r'(kotlin-gradle-plugin:)[^"]+(")', kotlin, "kotlin gradle plugin")
    text = replace_once(text, r'(com\.android\.tools\.build:gradle:)[^"]+(")', agp, "android gradle plugin")
    BUILD_SRC.write_text(text)

    text = WRAPPER.read_text()
    text = replace_once(text, r"(distributions/gradle-)[0-9.]+(-all\.zip)", gradle, "gradle distribution")
    WRAPPER.write_text(text)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--compose-ui", required=True)
    parser.add_argument("--foundation", required=True)
    parser.add_argument("--material-icons", required=True)
    parser.add_argument("--activity", required=True)
    parser.add_argument("--compiler", required=True)
    parser.add_argument("--kotlin", required=True)
    parser.add_argument("--agp", required=True)
    parser.add_argument("--gradle", required=True)
    parser.add_argument("--compile-sdk", required=True)
    parser.add_argument("--app-min-sdk", required=True)
    args = parser.parse_args()

    patch_compose(args.compose_ui, args.foundation, args.material_icons, args.activity, args.compiler)
    patch_sdk_levels(args.compile_sdk, args.app_min_sdk)
    patch_toolchain(args.kotlin, args.agp, args.gradle)

    print(
        f"compose-ui {args.compose_ui}, foundation {args.foundation}, material-icons {args.material_icons}, "
        f"activity {args.activity}, compiler {args.compiler}, kotlin {args.kotlin}, agp {args.agp}, "
        f"gradle {args.gradle}, compileSdk {args.compile_sdk}, appMinSdk {args.app_min_sdk}"
    )

if __name__ == "__main__":
    main()
