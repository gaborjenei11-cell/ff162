# QA report — GSA FieldForce v1.0.0

## Local environment

- Java/Javac available; Android SDK / Gradle not installed in the execution environment.
- `bash tests/run_java_tests.sh`: PASS.
- Tested: synthesized starter kits, sample normalize / reverse / trim / fade, PCM16 WAV encode/decode round-trip, 16×16 pattern storage, tempo clock, 12 FX recipes finite non-silent response, project save/load with embedded PCM, static audit for four UI pages, ten skins, Android media bridges, APK workflow.
- XML parse and ZIP integrity checks done before packaging.

## Not yet verified

- End-to-end Android Gradle compilation; subject to first GitHub Actions run.
- Microphone permissions, AudioTrack output latency and MP3 decoder on a physical device.
- Visual layout and touch ergonomics across Android phone/tablet densities.
- Release signing and Google Play policies.

A successful core test does not imply a successful Android APK build. Resolve actual build errors from the GitHub Actions log if any are surfaced.
