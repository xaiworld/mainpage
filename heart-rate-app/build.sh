#!/usr/bin/env bash
# Builds heartbeat-debug.apk from scratch using only javac/D8/aapt/zipalign/apksigner
# (no Gradle, no Android Studio, no network access to Google's SDK servers required
# beyond the one-time R8 download below).
#
# Prerequisites (Ubuntu/Debian):
#   sudo apt-get install -y android-sdk-build-tools android-sdk-platform-23 aapt apksigner
#   (provides aapt, aidl, zipalign, apksigner, and /usr/lib/android-sdk/platforms/android-23/android.jar)
#
# Debian/Ubuntu's android-sdk-build-tools package does not ship a dex compiler
# (dx/d8), so this script fetches Google's standalone R8 jar (which contains D8)
# directly from the R8 project's public release bucket on Google Cloud Storage.
set -euo pipefail

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_JAR="/usr/lib/android-sdk/platforms/android-23/android.jar"
BUILD="$APP_DIR/build"
R8_JAR="$BUILD/r8.jar"
R8_URL="https://storage.googleapis.com/r8-releases/raw/main/00054e52ddc3a2260dbc05a6e0d820112e1cea7b/r8.jar"
KEYSTORE="$BUILD/debug.keystore"

rm -rf "$BUILD/classes" "$BUILD/dex" "$BUILD/apk"
mkdir -p "$BUILD/classes" "$BUILD/dex" "$BUILD/apk"

if [ ! -f "$R8_JAR" ]; then
  echo "== fetching R8/D8 =="
  curl -sS -o "$R8_JAR" "$R8_URL"
fi

echo "== javac =="
javac -source 8 -target 8 -bootclasspath "$ANDROID_JAR" -d "$BUILD/classes" \
  "$APP_DIR/src/com/example/heartbeat/MainActivity.java"

echo "== D8 (java bytecode -> dex) =="
( cd "$BUILD/classes" && java -cp "$R8_JAR" com.android.tools.r8.D8 \
    --release --min-api 19 --lib "$ANDROID_JAR" --output "$BUILD/dex" \
    $(find . -name "*.class") )

echo "== aapt (manifest + resources.arsc) =="
aapt package -f -M "$APP_DIR/AndroidManifest.xml" -I "$ANDROID_JAR" \
  -F "$BUILD/apk/unsigned.apk"

echo "== add classes.dex =="
( cd "$BUILD/dex" && zip -j "$BUILD/apk/unsigned.apk" classes.dex )

echo "== zipalign =="
zipalign -f -p 4 "$BUILD/apk/unsigned.apk" "$BUILD/apk/aligned.apk"

if [ ! -f "$KEYSTORE" ]; then
  echo "== generating debug keystore =="
  keytool -genkeypair -v -keystore "$KEYSTORE" \
    -storepass androiddebugkey -keypass androiddebugkey -alias androiddebugkey \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Heartbeat Debug, OU=Debug, O=Debug, L=Debug, S=Debug, C=US"
fi

echo "== sign =="
apksigner sign --ks "$KEYSTORE" --ks-pass pass:androiddebugkey --key-pass pass:androiddebugkey \
  --out "$BUILD/apk/heartbeat-debug.apk" "$BUILD/apk/aligned.apk"

apksigner verify "$BUILD/apk/heartbeat-debug.apk"
echo "== done: $BUILD/apk/heartbeat-debug.apk =="
