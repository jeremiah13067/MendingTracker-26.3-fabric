# Mending Tracker for Minecraft 26.3

A client-only Fabric mod that displays the number of equipped items needing Mending and the estimated XP needed to repair them. This fork updates [RedstoneDisorder's Mending Tracker](https://github.com/RedstoneDisorder/MendingTracker) for Minecraft Java Edition 26.3.

## Installation

Use Minecraft **26.3**, Java **25**, Fabric Loader **0.19.5 or newer**, and Fabric API **0.162.0+26.3 or newer for 26.3**. Put `mending-tracker-1.0.1+mc26.3.jar` and Fabric API in your client's `mods` directory. A server installation is unnecessary.

Optional integrations tested by this project:

| Mod | Version |
|---|---|
| Mod Menu | 21.0.0 |
| MiniHUD | 0.41.2 |
| MaLiLib (required by MiniHUD) | 0.30.2 |

MiniHUD and MaLiLib are optional. With MiniHUD installed, enable **Mending** in its info-line settings. Its toggle, line order, and hotkey use MiniHUD's normal configuration. **Override MiniHUD** enables the standalone display alongside MiniHUD.

## Usage and configuration

Run `/mendingTracker`, or open Mending Tracker's settings in Mod Menu. Configure X/Y position as percentages of the GUI, left/center/right alignment, and the MiniHUD override. Changes preview live; save persists them, while discard or Escape restores saved settings.

Tracking covers four armor slots, the selected main hand, and offhand. Unequipped inventory items, undamaged items, and equipment without Mending are excluded. The XP estimate preserves the original calculation: the combined missing durability divided by two, rounded up. Custom enchantment effects or XP-orb allocation can affect actual repair costs.

The HUD hides with F1 and when the debug overlay or always-on screen debug text is active. Tracking clears when leaving a world.

Settings remain in `config/mending-tracker.json`; existing settings are compatible. Invalid settings fall back to defaults, coordinates are clamped to 0–100, and malformed files are backed up before being replaced on save. Failed saves keep the configuration screen open.

## Development

Requires a full JDK 25. Set `JAVA_HOME` to that JDK, then use the checked-in wrapper:

```sh
./gradlew clean build prepareRelease
./gradlew runClient -PoptionalMods=all
./gradlew runClientGameTest -PoptionalMods=none
./gradlew productionClientTest -PoptionalMods=all
```

On Windows use `gradlew.bat`. Runtime profiles are `none` (default), `modmenu`, `malilib`, `minihud` (includes MaLiLib), and `all`. Optional integrations are compile-only unless selected.

`productionClientTest` loads the packaged mod and a separate test JAR with Fabric development mode disabled. It creates disposable test worlds and captures screenshots. Linux CI supplies a virtual display and software graphics. The test JAR is excluded from release artifacts.

Release files and SHA-256 checksums are generated in `build/release/`. See [CHANGELOG.md](CHANGELOG.md), [RELEASE_NOTES.md](RELEASE_NOTES.md), and [VALIDATION.md](VALIDATION.md).

## Attribution and feedback

Original author: **RedstoneDisorder**. MiniHUD mixin integration derives from the upstream mod and Mikarific's EID Tracker approach. MIT license and original copyright are retained.

Fork source and migration pull requests: [jeremiah13067/MendingTracker-26.3-fabric](https://github.com/jeremiah13067/MendingTracker-26.3-fabric). Original project and feature feedback: [upstream issues](https://github.com/RedstoneDisorder/MendingTracker/issues).