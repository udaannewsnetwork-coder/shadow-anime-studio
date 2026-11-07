# Praveencoder development notes

The first UI scaffold is now wired with a real Android voice-recognition flow and an explicit Google Search action. Website Builder creates a local starter HTML preview payload; AI provider and persistent project storage are intentionally separate next steps.

## Build

Open the project in Android Studio, sync Gradle, and run on an Android 8+ device or emulator. The first voice action asks for microphone permission. Google search opens through the device's browser intent.

## Next implementation milestones

1. Add a backend proxy for an AI provider; never ship API keys in the APK.
2. Store generated projects with Room and render sanitized HTML in a WebView.
3. Add MediaRecorder for user-started audio files and Media3 for user-selected video editing.
4. Add explicit Play Store search intents; installation remains a user-confirmed Play Store action.
5. Add tests and a CI build workflow before distributing an APK.
