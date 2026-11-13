# GSA FieldForce 1.1.0 — Analog UI / MOVE integration

- Preserved original Recorder, 16-pad Sampler, 16×16 Sequencer, FX Rack, 10 skins, WAV/MP3 import, WAV export and project management.
- Waveform now appears on Recorder page only.
- Sequencer is drawn as a compact touch-responsive 16×16 LED grid; no horizontal or vertical scrolling. Tempo, metronome, swing and transport stay visible.
- Effects use brushed-metal custom rotary controls instead of sliders.
- Sampler pads use available space rather than fixed-size rows.
- FieldForce wordmark moved to a dedicated row below the top GSA logo/header to avoid camera cutout overlap.
- FX Rack has a MOVE 1.0.3d button, opening a dedicated 16:9 landscape viewport; back returns to FX Rack.
- Imported the 85 original MOVE 1.0.3d preset names, numeric DSP recipes and visual profile parameters from the provided VST source.
- Android-native MOVE audio processor supports XY, wet-only/additive mix, FREEZE, tempo-aware delay, and is inserted after the FieldForce FX rack for live pads and sequencer output.
- MOVE view includes a touch-controlled spiral visualization, REC/PLAY gesture automation, SAVE/LOAD motion, RANDOM, FREEZE, preset browse and back navigation. Ten skin accent palettes apply.

## Scope / limitations

This is **not a byte-identical Android build of the JUCE/VST3 MOVE DSP or OpenGL renderer**. Android's Java AudioTrack/Canvas implementation is an adaptation using the exact 85 preset datasets. The VST-specific DSP graph, original GPU shaders, host tempo/automation API, and some VST-only features have not been ported one-for-one. Android MOVE uses the FieldForce sequencer BPM, not a DAW host clock. MOVE motion recording is stored in local app preferences, not embedded in `.fieldforce` project files.

Full Android Gradle compilation and on-device UI/audio validation require the GitHub Actions build and a phone; local environment lacks Android SDK.
