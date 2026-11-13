# GSA FieldForce v1.5.0 – Native GLES MOVE Pad

## Implemented in source
- MOVE 1.0.3d native C++ DSP compiled as Android NDK JNI library (existing integration)
- New Android OpenGL ES 2.0 pad renderer (MoveGLView) replacing Canvas pad on MOVE page
- Reads the 85 original visual profile parameter sets; skin-specific LED tint; XY touch; BPM-related spin
- Shared nine MOVE skin assets, metal knobs and sequencer LED styling from prior FieldForce releases
- FX Rack opens landscape MOVE viewport and returns with Back
- GitHub workflow installs matching Android NDK and CMake versions

## Important limitations / remaining work
- This is an Android OpenGL ES reimplementation, **not** the identical JUCE desktop OpenGL renderer. Exact animation parity and pixel-identical GUI are **not** verified.
- Existing Java-to-JNI per-sample control calls need a block-oriented real-time audio path and concurrency audit.
- Full APK build and on-device visual/audio tests are NOT run in this environment.
- Some presets may not produce identical audible results until DAW/Android A-B audio comparisons are performed.

## Local verification
Portable Java tests, MOVE tests and Java syntax parsing passed. ZIP integrity verified separately.
