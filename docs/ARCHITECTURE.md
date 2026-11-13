# GSA FieldForce Architecture

- `MainActivity`: immersive screen, four page builders, themed buttons + material textures, Storage Access Framework document selection and project management.
- `AudioEngine`: independent `AudioTrack` thread at 44.1 kHz stereo/PCM16; short voice list, mono pad rendering with shared stereo FX; sample-accurate 16th-note scheduling. Not a system-wide Android sound processor.
- `MediaSupport.Recorder`: Android `AudioRecord` capturing at 44.1 kHz mono with a 60-second cap; microphone permission requested on demand.
- `MediaSupport.decode`: PCM16 WAV fallback + Android `MediaExtractor` and `MediaCodec` decoding for MP3 and device-supported formats.
- `PcmSample`, `KitFactory`: immutable mono sample objects and synthesized, copyright-free starter kits.
- `Pattern`, `SequencerClock`: 16×16 event grid, BPM, swing, metronome and timing.
- `AudioFx`: streaming stereo combination of saturation, color filter, bitcrush, modulated chorus, cross-channel feedback delay, room ambience, width; 12 presets.
- `WavIO`: portable PCM16 RIFF WAV import/export.
- `ProjectCodec`: versioned, GZIP-compressed binary `.fieldforce` session format; embedded PCM, pattern and settings (note: only FX preset number, not custom eight-knob positions).
- `.github/workflows/build-apk.yml`: portable core tests, Gradle assembleDebug, APK artifact upload.

## Audio behaviour

AudioTrack block rendering runs in a high-priority thread, with preallocated DSP buffers. Pad hits arrive from the UI via a concurrent queue. Recorded samples are not streamed from disk and have a 60-second cap. FX applied destructively to a sample use a separate instance so live FX tails are unaffected. Offline sequencer WAV rendering uses a separate FX instance and copies current knob values.

## Permissions / privacy

Only `RECORD_AUDIO` is requested, on demand for microphone recording. Files are selected/exported using Android's Storage Access Framework; no broad storage or internet permission is required. No account, telemetry or network service is included.
