# GSA FieldForce v1.2.0 — Unified MOVE Visual Assets

## Implemented
- The original MOVE 1.0.3d nine background PNGs, nine metal knob PNGs, nine LED PNGs and GSA logo are included without re-rendering.
- One shared nine-skin selection persists in preferences and applies to both FieldForce and MOVE.
- A SKIN button is available directly on the landscape MOVE page.
- The existing Android 85-preset MOVE adaptation, sampler/sequencer output processing, and full-screen 16:9 MOVE route remain available.
- Recorder-only waveform and non-scrolling sequencer remain in place.

## Not yet achieved
- Pixel-identical VST3 layout is not guaranteed: Android layouts and text differ from JUCE PluginEditor.
- The original C++ MoveDSP.h has NOT been compiled into Android; current MoveFx.java remains an adaptation.
- Original OpenGL animation has NOT been ported: Android MovePadView is used.
- Android Gradle build and on-device testing are still required.

This is an honest incremental source release, NOT a certified full DSP/GUI port.
