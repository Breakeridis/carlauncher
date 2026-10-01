# Minimal Android Car Launcher

## Target Head Unit Information
- **Platform / Processor**: Allwinner T507 / A133 SoC (Nowada / NWD K2401P Platform)
- **Operating Environment**: Android Automotive In-Dash System
- **Display Resolution & Orientation**: 1080p Landscape Widescreen (16:9 / 16:10 Automotive In-Dash Display)
- **Primary Use Case**: Distraction-free driver cockpit interface optimized for glanceability, physical touch target ergonomics, and direct vehicle integration.

---

## Overview
Minimal Android Car Launcher is a clean, modern, and high-contrast home screen replacement built specifically for automotive head units. It prioritizes driver focus and safety by providing zero-latency touch controls, extra-large glanceable metrics, an interactive live navigation map portal, an integrated live radio tuner, and instant access to phone projection (Android Auto / Apple CarPlay).

---

## Screen Layout & Component Positions

The interface is structured in a balanced, landscape 3-column cockpit layout:

```
┌─────────────────────────┬─────────────────────────┬─────────────────────────┐
│       LEFT COLUMN       │      CENTER COLUMN      │      RIGHT COLUMN       │
├─────────────────────────┼─────────────────────────┼─────────────────────────┤
│ [1] Digital Clock &     │                         │ [5] Phone Projection    │
│     Calendar Widget     │   [3] Circular Moving   │     (ZLink / Auto)      │
├─────────────────────────┤       Map Portal        ├─────────────────────────┤
│ [2] Live FM/AM Radio    │                         │ [6] Navigation & Music  │
│     Tuner & Presets     │   [4] Speedometer HUD   │     Quick-Launch Tiles  │
├─────────────────────────┤       (Double Size)     ├─────────────────────────┤
│ [7] Left App Dock &     │                         │ [8] System Controls &   │
│     All Apps Button     │                         │     Cloud Updater Dock  │
└─────────────────────────┴─────────────────────────┴─────────────────────────┘
```

---

### [1] Top-Left: Digital Clock & Calendar Widget
- **Position**: Upper section of the left column.
- **Contents**:
  - Extra-large digital time display (Hours and Minutes).
  - Real-time running seconds counter.
  - Localized day of the week and full date calendar text.
- **Functionality**:
  - Constantly synchronizes with head unit system time.
  - Single-tap opens the system Clock, World Clock, and Alarm application.

---

### [2] Middle-Left: Live FM/AM Radio Tuner Widget
- **Position**: Middle section of the left column, directly below the clock.
- **Contents**:
  - **Live Frequency & Band**: Displays current broadcast frequency in bold, high-contrast numbers (e.g. `88.6 FM` or `AM`).
  - **Station Name / RDS Information**: Displays Program Service (PS) and radio text broadcasted by the active station.
  - **Physical-Friendly Seek Controls**:
    - **`[ − ]` Button**: Large seek-down / step-down button.
    - **`[ + ]` Button**: Large seek-up / step-up button.
  - **Horizontal Favorite Presets Strip**:
    - 4 distinct quick-access station chips (e.g., `88.6`, `95.2`, `98.5`, `103.7`).
- **Functionality**:
  - **Real-Time Frequency Sync**: Automatically detects and displays the active frequency playing on the vehicle's native radio tuner without requiring manual input.
  - **Seek & Step Tuning**: Tap `[ − ]` or `[ + ]` to step frequencies or seek the next available broadcast station.
  - **Instant 1-Tap Preset Recall**: Tap any of the 4 favorite station chips to immediately tune the car's radio directly to that frequency.
  - **Long-Press Preset Saving**: Long-press any preset chip to save the currently playing frequency into that preset slot. Saved stations persist permanently across car restarts.
  - **Launch Native Radio**: Tap the central frequency display to launch the head unit's full-screen native radio application.

---

### [3] Center: Circular Moving Map Portal
- **Position**: Center column of the screen, occupying the full vertical height of the display.
- **Contents**:
  - Full-color, interactive moving map view centered on your current vehicle position.
  - Rotating exterior compass bezel marked with cardinal directions (N, E, S, W).
  - Centered vehicle locator icon that dynamically turns with the car's direction of travel.
  - Live GPS tracking indicator showing lock status.
  - Integrated Search and Recenter controls.
- **Functionality**:
  - **Live Vehicle Tracking**: Smoothly tracks the car along roads in real-time as you drive.
  - **Directional Compass Orientation**: The map automatically aligns to the vehicle's forward travel heading or magnetic north.
  - **Touch Navigation & Exploration**: Supports full touch panning and pinch-to-zoom gestures to explore surrounding roads, intersections, and landmarks.
  - **One-Touch Recenter**: Tapping the recenter button instantly snaps the map camera back to the car's live GPS coordinates.
  - **In-Portal Address Search**: Tap the search icon to find specific addresses, cities, or points of interest.
  - **Auto Day/Night Theming**: Automatically adjusts map road contrast between day illumination and dark cockpit night vision.

---

### [4] Center-HUD: Integrated Digital Speedometer
- **Position**: Located directly inside the center map portal for direct line-of-sight driving.
- **Contents**:
  - Double-sized, ultra-bold digital speed readout.
  - Speed unit label (`KM/H` or `MPH`).
- **Functionality**:
  - **High-Accuracy GPS Speed**: Calculates and displays true ground vehicle speed in real-time.
  - **Instant Unit Toggle**: Tap directly on the speed numbers to instantly switch between **Kilometers per Hour (KM/H)** and **Miles per Hour (MPH)**. Preferences are saved automatically.

---

### [5] Top-Right: Phone Projection Quick-Launch Tile (ZLink / Android Auto / Apple CarPlay)
- **Position**: Upper section of the right column.
- **Contents**:
  - Prominent vehicle phone projection tile displaying the connection status.
- **Functionality**:
  - **One-Touch Auto Connect**: Automatically detects the head unit's pre-installed phone projection app (ZLink, SpeedPlay, AutoKit, Android Auto, or CarPlay).
  - Tapping the tile immediately launches wireless or wired phone mirroring without needing to navigate through menus.

---

### [6] Middle-Right: Navigation & Media Quick-Launch Tiles
- **Position**: Middle section of the right column.
- **Contents**:
  - **Navigation Tile**: Dedicated shortcut for turn-by-turn road navigation.
  - **Music Player Tile**: Dedicated shortcut for external audio and media streaming apps (e.g. Spotify, YouTube Music, local car audio).
- **Functionality**:
  - **Single-Tap Launch**: Opens your preferred navigation or media application immediately.
  - **Long-Press App Assignment**: Long-press either tile to select and bind any other installed navigation or audio app as the default handler.

---

### [7] Bottom-Left: Customizable Application Dock & All Apps Button
- **Position**: Bottom edge of the left column.
- **Contents**:
  - Up to 4 customizable pinned favorite app shortcut icons.
  - The master **All Apps** drawer launcher button (grid icon).
- **Functionality**:
  - **Launch Favorites**: Single-tap any pinned app icon to open it.
  - **Quick Dock Customization (No Complex Settings Needed)**:
    - **Long-Press Any Pinned App**: Opens a quick pop-up dialog with options to:
      - **Remove**: Clears the shortcut from the bottom bar.
      - **Replace**: Opens an app selector to replace the slot with any installed app.
    - **Add New Apps**: If fewer than 4 apps are pinned, an `[ + ]` icon appears. Tapping it allows picking an app to pin to the bar.
  - **Open Full Application Drawer**: Tap the All Apps icon to open the complete app drawer.

---

### [8] Bottom-Right: System Controls & Cloud Updater Dock
- **Position**: Bottom edge of the right column.
- **Contents**:
  - **Theme Toggle Button**: Switches the entire launcher UI between clean daylight mode and deep automotive cockpit night mode.
  - **Vehicle Settings Button**: Directly opens the head unit's factory car and system settings menu.
  - **In-App Cloud Updater Button**: Direct over-the-air update manager.
- **Functionality**:
  - **Cockpit Day/Night Mode**: Inverts interface contrast to minimize glare during night driving while maximizing sunlight readability during the day.
  - **Direct System Settings Access**: Gives quick access to car sound equalizer, steering wheel controls, Wi-Fi, Bluetooth, and vehicle configurations.
  - **In-App Over-The-Air (OTA) Updates**:
    - Tap the update button at any time to check for the latest releases online.
    - Shows download progress directly on screen and triggers the automatic update installation on the head unit without requiring a computer, USB drive, or manual file copying.

---

## Overlay Features & Menus

### Full-Screen Application Drawer
- **Activation**: Tap the **All Apps** grid icon on the bottom-left dock.
- **Contents & Features**:
  - **Instant Search Bar**: Type to filter applications in real-time.
  - **Alphabetical Grid**: Displays all software installed on the head unit with high-resolution icons.
  - **1-Touch Pinning**: Long-press any application in the drawer to directly add it to the bottom favorites dock.
  - **Smooth Dismissal**: Tap outside the drawer or swipe down to return to the home screen.

### Automatic Power & Boot Optimization
- Optimized for automotive standby: The launcher immediately restores all widgets, radio stations, map state, and dock shortcuts instantly upon vehicle ignition without delay.
