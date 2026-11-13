# GSA FieldForce for Android

An original GSA-branded **field recorder + 16-pad sampler + 16-track step sequencer + FX rack** Android application. Built as a native Java Android Studio project, with no runtime internet dependency, and an automated GitHub Actions debug APK workflow.

## Features

- Four quick-access pages: **RECORDER**, **SAMPLER**, **SEQUENCER**, **FX RACK**.
- Immersive full-screen studio UI, waveform display, original GSA artwork and **10 themed material skins**.
- On-device microphone capture (mono 44.1 kHz, up to 60 s) with normalize, reverse, fade, trim and destructive FX print.
- 16 pads, original synthesized starter kits, import WAV/MP3 (Android decoder support required) and export WAV.
- 16×16 grid sequencer with PLAY, STOP, live step REC, BPM 40–240, swing and metronome; render 1/2/4/8/16 bars as stereo WAV.
- 12 browsable FX presets plus individual Mix / Drive / Echo / Reverb / Filter / Crush / Chorus / Stereo controls. These affect pad playback and the sequencer render.
- Project documents (`.fieldforce`) contain the actual pad audio, pattern, skin and selected FX preset.
- MENU > NEW / SAVE / LOAD / SKIN / EXPORT / EXIT.

The app processes **its own sampler audio**, not other applications' output. It does not include or reproduce Koala Sampler's code or artwork.

## Build an installable APK using GitHub

1. Upload the **contents** of this repository ZIP, including `.github`, to a GitHub repository.
2. Go to **Actions → Build GSA FieldForce APK → Run workflow**.
3. Once green, download **GSA-FieldForce-Android-debug-APK** in the workflow Artifacts area.
4. Extract the downloaded artifact and install `app-debug.apk` on an Android device for testing (Android 10+).

The workflow uses **JDK 17**, **Android Gradle Plugin 8.7.3**, **Gradle 8.10.2**, **compileSdk/targetSdk 35** and minSdk 29. It runs the portable core tests before `:app:assembleDebug`. A release-signed APK / Play Store build requires additional signing setup.

Locally, open the project root in Android Studio (with Android SDK 35 and JDK 17). For standalone audio-core tests use:

```bash
bash tests/run_java_tests.sh
```

## Limitations & verification

The pure Java sampler DSP, WAV and project code passed local JVM tests. The Android UI, microphone and Android media decoder need to be compiled with an actual Android SDK and tested on hardware; **this ZIP is source code, not a prebuilt APK**. Sample import is limited to 60 seconds. WAV fallback supports PCM16 mono/stereo; codec support varies by device. A project contains one 16-step pattern and 16 audio pads.

See [Hungarian documentation](README_HU.md) and [Architecture](docs/ARCHITECTURE.md).
