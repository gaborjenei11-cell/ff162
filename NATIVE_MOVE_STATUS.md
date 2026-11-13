# v1.3.0 original MOVE DSP native integration

- `app/src/main/cpp/move/MoveDSP.h` and `PresetLibrary.h` copied unchanged from the MOVE 1.0.3d ZIP.
- NDK CMake builds the actual original C++ MoveDSP into `libgsa_move.so`.
- `MoveNative.java` provides JNI lifecycle, controls, stereo processing.
- `MoveFx.java` uses the native DSP when the library loads; its older Java DSP remains fallback for JVM-only tests.
- Current bridge calls JNI twice per stereo frame and recalculates routing every sample: functionally integrated but not optimized for real-time performance. A block-based JNI interface is recommended before production.
- Original OpenGL visual renderer / pixel-identical JUCE GUI are NOT ported yet. Existing MOVE visual assets and unified skins remain.
- Android Gradle/NDK build and device testing have NOT been completed locally.
