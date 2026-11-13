# GSA FieldForce — Android Field Recorder / Sampler / Sequencer / FX

**GitHubra feltölthető natív Android Studio-projekt (v1.0.0).** Ez a projekt egy új, eredeti GSA-alkalmazás: az egyszerű pad-sampler munkafolyamatból indul ki, de saját felülettel és hangmotorral dolgozik. Nem a Koala Sampler forráskódja vagy képi másolata.

## Mit tud a jelenlegi verzió?

- **Teljes képernyős GSA felület:** Recorder / Sampler / Sequencer / FX RACK oldalak, közös hullámkijelző és egygombos MENU.
- **Field Recorder:** valódi mikrofonfelvétel (`RECORD_AUDIO` engedéllyel), 44,1 kHz-es mono PCM, max. 60 másodperces take; a kiválasztott padra kerül. NORMALIZE / REVERSE / FADE / TRIM 10%, FX PRINT.
- **Sampler:** 16 nyomható pad, öt szintetikus kezdőkit, WAV/MP3 betöltés az Android fájlválasztóval, padonként külön minta, WAV export.
- **Sequencer:** 16 sáv × 16 lépés, PLAY / STOP / REC (élő padütések beírása a futó lépésre), 40–240 BPM, swing és metronóm, projektszintű pattern, 1–16 ütem WAV export.
- **Effektek:** 12 beépített FX preset, 8 állítható paraméter (wet mix / drive / echo / reverb / filter / bitcrush / chorus / stereo), közös effektútvonal a samplernek és a sequencernek. A rögzített mintára az FX PRINT felírható.
- **10 tematikus skin:** Black Ember, Blue Alloy, Cream Metal, Walnut Glass, Violet Titanium, Solar Gold, Gunmetal Amber, Bronze Cyan, Pearl Magenta, Neon Circuit; egyedi anyag-textúrákkal és élénk LED-színnel.
- **MENU:** NEW PROJECT, SAVE PROJECT, LOAD PROJECT, SKINS (10), EXPORT SEQUENCE WAV, EXIT. A `.fieldforce` projektek a pattern mellett a betöltött minták hangját is tartalmazzák.

**Fontos:** ez az alkalmazás a saját samplerének a hangját dolgozza fel. Nem manipulálja a telefonon más alkalmazások (Spotify, YouTube stb.) kimenetét. A WAV-fájlok a felhasználó által kiválasztott telefonos vagy felhős mappába kerülnek az Android SAF rendszerén keresztül.

## Hogyan lesz APK a GitHubon?

1. Hozz létre egy üres GitHub repositoryt (például `GSA-FieldForce-Android`).
2. A ZIP **tartalmát** töltsd fel a repository gyökerébe, a `.github` mappával együtt.
3. Kattints a GitHubon az **Actions > Build GSA FieldForce APK > Run workflow** lehetőségre.
4. Zöld build után a workflow **Artifacts** részéből töltsd le a `GSA-FieldForce-Android-debug-APK` csomagot; az `app-debug.apk` telepíthető tesztelésre.

A workflow Java 17, Android SDK 35, Gradle 8.10.2 és AGP 8.7.3 konfigurációval dolgozik, ellenőrzi a hordozható hangmagot, majd `:app:assembleDebug`-et futtat. Nem kell saját aláírókulcs a debug APK-hoz. Play Store publikáláshoz később saját release-keystore és további tesztelés szükséges.

## Android Studio build

JDK 17 és Android SDK 35 telepítése után nyisd meg a mappát Android Studio-ban, és futtasd az `app` konfigurációt, vagy `gradle :app:assembleDebug` paranccsal építsd a projektet megfelelő Gradle-installációval.

A hangmag platformfüggetlen tesztelése Linux/macOS / Git Bash alatt:

```bash
bash tests/run_java_tests.sh
```

## Jelenlegi korlátok

- Egy pattern 16 lépéses (4/4-ben egy ütem), egy projekt egy aktív patternnel. Pattern láncolás, MIDI-ki-/bemenet és time-stretch későbbi fejlesztési kör.
- A minták maximum 60 másodpercesek; a WAV fallback PCM16 mono/sztereó, az MP3 és más kodekek az Android készülékén elérhető MediaCodec dekódert használják.
- A sequencer WAV-export a hangmotorral azonos effekt-algoritmust futtat, de az élő output pufferelése és a késleltetés készüléktől függ.
- A **Java hangmag tesztelt**; Android SDK/Gradle nélkül ebben a környezetben teljes APK-fordítást vagy készülékes tesztet nem lehetett végezni. A GitHub Actions a végső fordítási ellenőrzés.

## Mappák

`app/src/main/java/audio/gsa/fieldforce/` — Android UI, AudioTrack, AudioRecord és médiaimport.

`app/src/main/java/audio/gsa/fieldforce/core/` — hangszerkesztés, 16×16 pattern, FX, WAV, projektszerializálás.

`app/src/main/res/drawable-nodpi/` — 10 skin textúra.

`tests/` — hordozható Java-tesztek és forrásaudit.

`.github/workflows/build-apk.yml` — automatikus APK-build.


## v1.0.1 — GitHub Actions javítás

A `Validate portable audio core` lépés már akkor is lefut egy önálló Java core-fordítási ellenőrzéssel, ha a GitHubra feltöltéskor kimaradt a `tests/` mappa. A teljes tesztcsomag továbbra is a `tests/run_java_tests.sh` segítségével fut, ha az elérhető. **Javasolt a ZIP minden fájlját és rejtett `.github` mappáját a repository gyökerébe feltölteni.** A helyi környezetben Android SDK hiányában az APK fordítás nem ellenőrizhető; a GitHub Actions build eredménye szükséges.
