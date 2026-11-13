#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="${TMPDIR:-/tmp}/gsa-fieldforce-core-tests"
rm -rf "$OUT"; mkdir -p "$OUT"
javac -encoding UTF-8 -d "$OUT" "$ROOT"/app/src/main/java/audio/gsa/fieldforce/core/*.java "$ROOT"/tests/CoreTest.java
java -cp "$OUT" CoreTest
javac -encoding UTF-8 -d "$OUT" "$ROOT"/tests/ParseSources.java
mapfile -t SOURCES < <(find "$ROOT"/app/src/main/java -name "*.java" | sort)
java -cp "$OUT" ParseSources "${SOURCES[@]}"
python3 "$ROOT"/tests/static_audit.py
