# GSA FieldForce v1.4.0 — unified hardware GUI pass

Implemented in this source package:
- Nine shared original MOVE skins, logos, backgrounds and bitmap knob assets (from v1.3.0).
- Common 3D LED polycarbonate button drawable throughout FieldForce, including sequencer and sampler.
- Sequencer 16x16 grid uses the available height without scroll views; LED glow and glass specular layer.
- Recorder-only waveform, compact header, FieldForce title beside GSA logo, camera cutout-safe mode.
- FX rack uses eight physical-style rotary controls, no effect sliders.
- MOVE opens as a 16:9 landscape view with return-to-FX button, original MOVE presets, motion features and shared skin.
- Original MOVE 1.0.3d C++ DSP source and JNI bridge retained from v1.3.0.

NOT YET VERIFIED OR COMPLETE:
- Pixel-identical JUCE GUI / native OpenGL renderer; the MOVE pad is still Android Canvas.
- Fully production-optimized real-time JNI block audio path; existing per-frame JNI is retained.
- Android APK build and device rendering (Android SDK/NDK not available locally).
- Guaranteed layout fit on every display size; verify on target phone, especially short landscape screens.

Do not describe this as a fully completed pixel-identical port. It is a functional source iteration requiring Android device validation.
