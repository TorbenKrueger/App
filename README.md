# Android 19 App

This repository contains a minimal Android application that targets API 19.

## Modules
- `app`: main application module

## Build
Use the Android Gradle Plugin 8.4.0 with Kotlin 1.9.21.

Run `gradle wrapper` to generate a local wrapper (requires network access) and then `./gradlew assemble`.

### Troubleshooting missing imports
The sample relies on the AndroidX *activity-ktx* artifact for the
`viewModels` extension. Ensure your `build.gradle.kts` contains:

```kotlin
implementation("androidx.activity:activity-ktx:1.9.0")
```

The `R` class is generated from resources when the module builds
successfully. Verify that the package namespace in `android` block is set
to `"com.example.app"` so that the `import com.example.app.R` statement
resolves correctly.
