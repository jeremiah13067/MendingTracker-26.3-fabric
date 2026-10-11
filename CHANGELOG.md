# Changelog

## 1.0.1+mc26.3

- Port to Minecraft Java Edition 26.3 and the current stable Fabric toolchain.
- Update Fabric API, MiniHUD, MaLiLib, and Mod Menu compatibility.
- Track explicit equipment slots instead of numeric inventory indices.
- Preserve the original aggregate XP estimate and standalone HUD behavior.
- Clear totals when leaving a world.
- Load optional MiniHUD mixins only when MiniHUD and MaLiLib are present.
- Keep the custom MiniHUD toggle synchronized with its GUI and persisted configuration.
- Preserve existing configuration fields; recover invalid files and back them up before replacement.
- Save settings atomically where supported, retain unsaved edits through resizing, and keep failed saves open.
- Translate configuration and HUD text.
- Add unit tests, real client integration profiles, packaged-JAR testing, and Windows/Linux CI.
- Restrict metadata to the supported Minecraft 26.3 release line.

## 1.0.0_mc26.2

Original upstream Minecraft 26.2 release.