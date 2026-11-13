# GSA FieldForce v1.6.0 — Pad FX Routing & MOVE Controls

- 4×4 unlabelled pads, selected pad is the active editor target; removed Spinner selection.
- Under each pad: IN (import), OUT (export WAV), FX (green LED when enabled).
- GLOBAL FX switch at top of sampler: red when off/no active FX pad, green when active.
- Sequencer: per-row FX and MUTE (yellow LED), embedded at left of 16×16 grid; no scroll container.
- Shared per-pad FX flags control BOTH FX Rack and MOVE on live pad hits and sequencer voices. Bypassed voices remain dry. Global off hard-bypasses the effects bus.
- FX Rack power control uses MOVE-style illuminated button instead of Android Switch.
- Removed small footer copy and bottom recorder instructions.
- Project format V2 saves global FX, 16 FX flags and 16 mute flags; V1 files remain loadable.
- Original nine MOVE skins, native DSP integration and OpenGL ES view retained from v1.5.0.

## QA scope
Portable Java tests and static syntax checks can run locally; Android Gradle APK build and phone UI testing still required. AudioTrack live DSP and UI dimensions cannot be verified without Android runtime.
