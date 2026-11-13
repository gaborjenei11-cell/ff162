#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
TMP=$(mktemp -d); trap 'rm -rf "$TMP"' EXIT
javac -d "$TMP" app/src/main/java/audio/gsa/fieldforce/MoveNative.java app/src/main/java/audio/gsa/fieldforce/MovePresets.java app/src/main/java/audio/gsa/fieldforce/MoveFx.java app/src/main/java/audio/gsa/fieldforce/MoveMotion.java tests/move/MovePortTest.java
java -cp "$TMP" MovePortTest
