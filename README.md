# Aethornex Location — Android prototype

This is an Android Studio Kotlin starter project for a draggable floating radar overlay.

## What works
- Opens as an Android app.
- Requests Android's "Display over other apps" permission.
- Starts a draggable floating radar.
- Draws a player marker and **simulated** target dots/lines.
- The overlay can be closed using the × in its top-right corner; the app's Stop Radar button also stops it.

## Important limitations
This is a UI prototype only. It does **not** detect Free Fire MAX players, read game memory, identify hitboxes, capture the game screen, or reveal enemies. The target dots are simulated placeholders. A normal third-party overlay cannot obtain authoritative enemy coordinates from the official game. Do not use modified clients, memory injection, or anti-cheat bypasses.

## Build
1. Open this folder in Android Studio (a recent version with Android SDK 36 installed).
2. Allow Gradle sync to finish.
3. Select Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Android Studio will show the generated APK location.

The project uses application ID `com.aethornex.location`, min SDK 26 and target SDK 35. Test the overlay permission flow on your device before relying on it.
