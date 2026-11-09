# RoraxOS

An Android dashboard app for running a local, non-root Linux environment (via PRoot) with a configurable set of command-line tools, built with Kotlin and Jetpack Compose.

## Status

Early scaffold. This commit includes:

- `LinuxEnvironment` — runs commands inside a PRoot-wrapped rootfs, streaming output.
- `RootfsInstaller` — downloads and extracts a rootfs tarball with stage/progress reporting.
- `LinuxService` — foreground service keeping the environment alive in the background.
- Tool model (`Tool`, `ToolRepository`) driven by `assets/tools.json` — add your own tools here.
- Onboarding wizard (welcome/disclaimer, engine choice, install progress, done).
- Dashboard screen (VM status card, tools grid, recent activity).
- Terminal screen for live command output.

## What you still need to provide

- **PRoot binaries**: place `arm64-v8a` PRoot binaries at `app/src/main/jniLibs/arm64-v8a/libproot.so` (PRoot binaries aren't included in this repo).
- **A rootfs URL**: point `RootfsInstaller` at a Debian/Ubuntu arm64 rootfs tarball you're entitled to use (e.g. your own GitHub Release asset).
- **Your own tools**: edit `app/src/main/assets/tools.json`. Only generic placeholder tools are included — add commands relevant to your own authorized use case.

## Building

A GitHub Actions workflow (`.github/workflows/build.yml`) builds a debug APK on every push to `main` and uploads it as a build artifact — check the **Actions** tab after pushing, then download the APK from the workflow run.

To build locally:

```bash
./gradlew assembleDebug
```

(Run `gradle wrapper` once locally if the Gradle wrapper jar isn't present, or open the project in Android Studio, which will offer to set it up.)

## License

See `LICENSE`.
