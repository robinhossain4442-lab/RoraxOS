# RoraxOS

An Android dashboard app for running a local, non-root Linux environment (via PRoot) with a configurable set of your own command-line tools — e.g. a Python/Node script that runs as a GitHub bot — built with Kotlin and Jetpack Compose.

## What works end-to-end right now

- **Onboarding**: disclaimer → engine choice → your rootfs URL → live install progress → dashboard.
- **Settings**: change the rootfs URL later and trigger a reinstall.
- **Dashboard**: start/stop the environment (foreground service keeps it alive in the background), browse tools in a grid, see recent activity.
- **Drawer**: tools grouped by category, with a ROOT badge for any tool marked `requiresRoot`.
- **Tool execution**: tapping a tool runs its `command` via `sh -c` inside the PRoot sandbox and streams stdout/stderr live into the terminal screen.
- **Tool definitions**: edit `app/src/main/assets/tools.json`. Each tool is just `{ name, command, category, requiredPackages, requiresRoot }` — point `command` at your own script, e.g. `"python3 /root/scripts/my_github_bot.py"`.

## What you still need to provide

- **PRoot binaries**: place `arm64-v8a` PRoot binaries at `app/src/main/jniLibs/arm64-v8a/libproot.so` (not bundled here).
- **A rootfs tarball URL**: this app does NOT ship with one. On first launch it asks you for a `.tar.gz` URL (e.g. a Debian/Ubuntu arm64 rootfs you host as a GitHub Release asset on your own repo). You can change it later in Settings.
- **Your own scripts**: bundle them into the rootfs itself (e.g. under `/root/scripts/`) or have your tool's command `git clone`/`curl` them down on first run, then reference them from `tools.json`.

## Known limitation: no interactive stdin yet

`LinuxEnvironment.execute()` streams a command's stdout/stderr, but nothing currently writes to the child process's stdin. That's fine for scripts that run to completion on their own (a bot script, a build command, `apt-get update`, etc.) — the `shell_check` example tool is one of these. It is **not** fine for a tool whose command is a bare interactive shell like `/bin/bash` with no arguments — that will block waiting on input that never arrives. If you want a true interactive terminal (type commands, see output live), you'll need to extend `TerminalScreen` with an input field that writes to `process.outputStream` and wire that through `LinuxEnvironment`. That's a reasonable next step but isn't implemented yet.

## Building

A GitHub Actions workflow (`.github/workflows/build.yml`) builds a debug APK on every push to `main` and uploads it as a build artifact — check the **Actions** tab, then download the APK from the workflow run.

To build locally:

```bash
./gradlew assembleDebug
```

(Open in Android Studio and it will offer to generate the Gradle wrapper jar if it's missing.)

## Project layout

```
app/src/main/java/com/roraxos/linuxenv/
├── core/              # LinuxEnvironment (PRoot runner), RootfsInstaller, AppPreferences
├── data/               # Tool model + JSON-backed repository
├── service/            # Foreground service keeping the environment alive
├── ui/
│   ├── onboarding/     # Welcome, engine choice, rootfs URL, install progress, complete
│   ├── dashboard/      # Dashboard screen, VM status card, tools grid, drawer
│   ├── terminal/       # Live command output screen + ViewModel
│   └── settings/       # Rootfs URL + reinstall
└── MainActivity.kt      # Navigation graph wiring it all together
```

## License

See `LICENSE`.
