# FieldForce v1.6.1 – GitHub checkout javítás

A teljes ZIP **tartalmát** töltsd fel a GitHub repository gyökerébe, ne csak az app/ mappát.

Kötelező: `.github/workflows/build-apk.yml`, `tests/run_java_tests.sh`, `tests/run_move_tests.sh`, `tests/CoreTest.java`, `app/src/main/java/audio/gsa/fieldforce/core/*.java`, `app/build.gradle`.

A GitHub webes fájlfeltöltője rejtett `.github` mappáknál problémás lehet; Git CLI ajánlott:

```bash
git add -A
git commit -m "Fix FieldForce CI checkout and tests"
git push
```

A workflow most hiányzó fájlok esetén pontos hibát ad, és nem próbál értelmetlen wildcard fallbackot fordítani. A javítás nem garantálja a teljes Android APK fordítását: további hibákat a következő futás logja mutatja.
