# Jetpack Compose compatibility

The SDK reads Compose internals through reflection, so every supported Compose version has to be verified. The version below is the one the SDK is compiled and tested against.
`test-app` uses the same version as a regular `implementation` dependency.

## Version matrix

| Compose UI | Compose Foundation | Compose compiler | Kotlin | AGP    | Gradle | compileSdk | app minSdk |
|------------|--------------------|------------------|--------|--------|--------|------------|------------|
| 1.2.1      | 1.2.1              | 1.3.2            | 1.7.20 | 7.3.1  | 7.5.1  | 34         | 21         |
| 1.3.3      | 1.3.1              | 1.3.2            | 1.7.20 | 7.3.1  | 7.5.1  | 34         | 21         |
| 1.4.3      | 1.4.3              | 1.5.0            | 1.9.0  | 7.3.1  | 7.5.1  | 34         | 21         |
| 1.5.4      | 1.5.4              | 1.5.0            | 1.9.0  | 7.3.1  | 7.5.1  | 34         | 21         |
| 1.6.8      | 1.6.8              | 1.5.0            | 1.9.0  | 7.3.1  | 7.5.1  | 34         | 21         |
| 1.7.8      | 1.7.8              | 1.5.0            | 1.9.0  | 8.13.2 | 8.13   | 34         | 21         |
| 1.8.3      | 1.8.3              | 1.5.0            | 1.9.0  | 8.13.2 | 8.13   | 35         | 21         |
| 1.9.5      | 1.9.5              | 1.5.0            | 1.9.0  | 8.13.2 | 8.13   | 36         | 21         |
| 1.10.6     | 1.10.6             | 1.5.0            | 1.9.0  | 8.13.2 | 8.13   | 36         | 23         |

## Running the matrix

Both matrices have a shared run configuration in Android Studio, **Test all Compose versions** and **Test all Android versions**.

`tools/compose_matrix.py` patches the versions, runs the instrumented tests of `test-app` on the connected device and restores the build
files afterwards.

```bash
python3 tools/compose_matrix.py            # every version
python3 tools/compose_matrix.py 1.7.8      # a single version
```

Logs of the individual runs are written to `build/compose-matrix/`.

`tools/emulator_matrix.py` does the same across the Android versions, it boots every AVD in turn and keeps the Compose version unchanged.

```bash
python3 tools/emulator_matrix.py                # every AVD
python3 tools/emulator_matrix.py Android_16     # a single AVD
```

The tests are verified on API 22 to 37. API 21 is skipped, `Initializer.setup` returns early for it and the SDK records nothing there at all.

## What is verified

`TextFieldComposeSensitivityTest` records `TextFieldComposeActivity` and asserts the contents of the extracted wireframe. A text of a
sensitive element never becomes a `Skeleton.Text`, it is replaced by a plain rectangle, so the absence of a text in the wireframe proves
that the text is not recorded.

The two test methods assert the opposite for the same string, which makes them a negative control for each other. If the reflection
silently stops working, `textFieldsAreSensitiveByDefault` fails because the placeholder leaks into the wireframe.
