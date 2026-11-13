# QA — GSA FieldForce 1.1.0

- PASS: `bash tests/run_java_tests.sh` (portable core, Java syntax parser, static UI audit).
- PASS: `bash tests/run_move_tests.sh` (85 original MOVE preset records, Android DSP finite outputs, bypass, FREEZE, XY motion capture).
- PASS: original 10 skins and brand assets retained.
- PASS: GitHub Actions workflow invokes both portable and MOVE tests before Gradle.
- NOT RUN: complete Android Gradle `assembleDebug` (Android SDK/Gradle not available locally).
- NOT RUN: Android emulator/device tests, notch/safe area validation, touch latency and actual audio performance.
- WARNING: Android MOVE is a feature adaptation, not full JUCE DSP/GL binary parity.
