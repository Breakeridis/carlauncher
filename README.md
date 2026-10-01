# Minimal Android Car Launcher

A clean, high-contrast home screen replacement for Android automotive head units
(Allwinner T507 / A133 - NWD K2401P, 1080p landscape), implementing
`UIDescription.md`: 3-column cockpit layout, live radio widget, circular map
portal with compass bezel, GPS speedometer HUD, phone projection tile,
nav/music quick-launch tiles, app dock + drawer, day/night theming and OTA updates.

## Requirements

- JDK 17
- Android SDK (compileSdk 34, minSdk 29 = Android 10)
- Android Studio (recommended) or plain Gradle 8.9

## Build

Open the project folder in Android Studio and let it sync, or from the command line:

```
gradlew assembleDebug          # -> app/build/outputs/apk/debug/app-debug.apk
```

Install on the head unit:

```
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Then make it the default home app (head unit Settings > Default apps > Launcher,
or `adb shell cmd package set-home-activity com.carlauncher/.MainActivity`).

On first run, grant the location permission (GPS speed + heading).

## Project structure

```
app/src/main/java/com/carlauncher/
├── LauncherApplication.kt      # applies saved day/night mode at boot
├── MainActivity.kt             # home activity, wires all controllers + flows
├── data/
│   ├── AppPrefs.kt             # persistent prefs (presets, pins, units, theme)
│   └── AppsRepository.kt       # installed app enumeration + launching
├── location/SpeedProvider.kt   # GPS speed/heading via LocationManager
├── radio/
│   ├── RadioTuner.kt           # interface + state + factory
│   ├── StubRadioTuner.kt       # simulated tuner (development/demo)
│   ├── VendorRadioTuner.kt     # TEMPLATE: plug in your unit's vendor API
│   └── RadioAppLauncher.kt     # opens the stock radio app
├── ui/
│   ├── ClockController.kt      # [1] clock + calendar
│   ├── RadioController.kt      # [2] FM/AM widget, presets
│   ├── MapPortalView.kt        # [3] circular map + compass (custom view)
│   ├── SpeedometerView.kt      # [4] digital speed HUD (custom view)
│   ├── ProjectionController.kt # [5] ZLink / AA / CarPlay detection
│   ├── QuickLaunchController.kt# [6] nav + music tiles
│   ├── DockController.kt       # [7] pinned apps dock
│   ├── AppDrawerFragment.kt    # full-screen app drawer
│   └── SystemController.kt     # [8] theme / settings / OTA
└── update/UpdateManager.kt     # in-app OTA check + download + install
```

## Radio integration (per-unit firmware)

The launcher ships with a working **stub** tuner (4 simulated FM stations) so the
UI is fully functional out of the box. NWD/Allwinner units expose the real tuner
through vendor-specific broadcast intents, hidden services, or serial/CAN
commands that differ per firmware build.

1. Run logcat while the stock radio app changes stations to discover the mechanism.
2. Fill in the TODOs in `radio/VendorRadioTuner.kt`.
3. Flip `useVendorRadio` to `true` in `AppPrefs` (or add a debug toggle).
4. Extend `radio/RadioAppLauncher.kt` with your unit's stock radio package.

## OTA updates

The in-app updater uses this repository's GitHub **latest release** as the
update source (`https://api.github.com/repos/Breakeridis/carlauncher/releases/latest`):
it compares the release tag (e.g. `v0.2`) with the installed `versionName`,
downloads the attached APK via the system DownloadManager and starts the
package installer. No extra hosting is required.

Publishing an update:

```
git tag v0.2
git push origin v0.2
```

CI then builds a **signed release APK** with the permanent release keystore
(stored as repository secrets) and attaches it to the new GitHub release.
All release APKs are signed with the same key, so in-app updates never hit
the signature-mismatch installer error (-7 `INSTALL_FAILED_UPDATE_INCOMPATIBLE`).

**Important:** install only release APKs on the head unit. Debug APKs (CI
artifacts) are signed with an ephemeral debug key and cannot be updated in
place later — if one is installed, uninstall it before installing a release.

## Signing

Release signing uses one permanent keystore:

- **CI**: repository secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
  `KEY_ALIAS`, `KEY_PASSWORD`.
- **Local**: `keystore.properties` next to `settings.gradle.kts` (gitignored):

```properties
storeFile=release.keystore
storePassword=...
keyAlias=carlauncher
keyPassword=...
```

Keep the keystore and passwords safe — if they are lost, the next release
cannot update the installed app and a full reinstall is required.

## Map engine

`MapPortalView` currently draws a procedural placeholder road network with a
rotating compass bezel, pan/pinch zoom and GPS heading tracking. To use a real
map, replace the canvas drawing with Google Maps / Mapbox (needs an API key and
the corresponding SDK dependency) while reusing the existing touch + GPS plumbing.

## Day / night

The theme toggle button switches `AppCompatDelegate` between night and day
modes; palettes live in `res/values/colors.xml` and `res/values-night/colors.xml`
(UI and map portal follow the same mode). To switch to automatic day/night based
on the unit's light sensor, set the default mode to `MODE_NIGHT_AUTO`.
