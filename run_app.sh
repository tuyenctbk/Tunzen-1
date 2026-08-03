#!/usr/bin/env bash

# TuneZen Android Emulator / Device Launcher Script
set -e

export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export PATH="$JAVA_HOME/bin:$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:/opt/homebrew/bin"

ADB_BIN=$(which adb 2>/dev/null || echo "$ANDROID_HOME/platform-tools/adb")
EMULATOR_BIN=$(which emulator 2>/dev/null || echo "$ANDROID_HOME/emulator/emulator")

echo "==============================================="
echo "🎵 TuneZen - Android Launcher Script"
echo "==============================================="

# Ensure gradle-wrapper.jar exists
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "📦 Copying Gradle wrapper binaries..."
    mkdir -p gradle/wrapper
    if [ -f "/Users/user/antigravity/Neotune/gradle/wrapper/gradle-wrapper.jar" ]; then
        cp /Users/user/antigravity/Neotune/gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
    fi
fi

# Check connected devices
RUNNING_DEVICES=$("$ADB_BIN" devices 2>/dev/null | grep -v "List" | grep "device" | wc -l | tr -d ' ' || echo "0")

if [ "$RUNNING_DEVICES" -eq "0" ]; then
    echo "⚠️  No active Android emulator or device found."
    echo "🔍 Checking available AVDs..."
    
    AVD_NAME=$("$EMULATOR_BIN" -list-avds 2>/dev/null | head -n 1 || echo "")
    
    if [ -z "$AVD_NAME" ]; then
        echo "❌ No Android Virtual Device (AVD) found on your machine."
        echo "👉 Please start an emulator or connect a device."
        exit 1
    fi

    echo "🚀 Starting emulator '$AVD_NAME'..."
    "$EMULATOR_BIN" -avd "$AVD_NAME" >/dev/null 2>&1 &
    
    echo "⏳ Waiting for emulator to boot completely..."
    "$ADB_BIN" wait-for-device
    until [ "$("$ADB_BIN" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
        sleep 2
        echo "   Waiting for Android boot..."
    done
    echo "✅ Emulator is online and ready!"
else
    echo "✅ Active Android device/emulator connected."
fi

echo "🔨 Building and installing debug APK..."
./gradlew installDebug

echo "⚡ Starting TuneZen MainActivity..."
"$ADB_BIN" shell am start -n com.soloprono.tunezen/com.example.MainActivity

echo "==============================================="
echo "🎉 TuneZen is now running on your emulator!"
echo "==============================================="
