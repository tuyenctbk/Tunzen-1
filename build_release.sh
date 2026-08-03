#!/usr/bin/env bash

# TuneZen - Build Release AAB (.aab) Script
set -e

export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export PATH="$JAVA_HOME/bin:$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:/opt/homebrew/bin"

echo "==============================================="
echo "📦 Building TuneZen Signed Release AAB (.aab)"
echo "==============================================="

./gradlew bundleRelease

echo ""
echo "==============================================="
echo "🎉 RELEASE AAB BUILD SUCCESSFUL!"
echo "📍 Location: app/build/outputs/bundle/release/app-release.aab"
echo "==============================================="
