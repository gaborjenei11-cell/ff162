# GSA FieldForce — GitHub build fix (v1.0.1)

**Observed:** `bash: tests/run_java_tests.sh: No such file or directory` (exit 127).

**Cause:** `tests/run_java_tests.sh` is present in the original distributed ZIP, but absent in the GitHub checkout used by the failing workflow (likely incomplete repository upload or different repository root).

**Fix:** `.github/workflows/build-apk.yml` now runs the complete suite when available; otherwise it compiles the portable Java core directly and continues to the Android APK build. It prints the tracked test files to make incomplete uploads visible.

**Recommended:** replace the entire repository contents with this archive (including `tests/`, `.github/`, `app/`, and root Gradle files). Do not nest everything in an extra directory. The workflow then runs full tests.

**Build validation:** local `bash tests/run_java_tests.sh` passes. Actual Android SDK/Gradle APK build must be verified by GitHub Actions.
