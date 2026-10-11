# Mending Tracker 1.0.1+mc26.3

Minecraft Java Edition **26.3** · Fabric · Client only

Requires Java 25, Fabric Loader 0.19.5+, and Fabric API 0.162.0+26.3. Optional integrations: Mod Menu 21.0.0; MiniHUD 0.41.2 with MaLiLib 0.30.2.

This release preserves equipped-item counting, the aggregate XP estimate, standalone HUD positioning/alignment, the client configuration command, and MiniHUD integration. Existing `mending-tracker.json` settings remain compatible.

Compatibility and robustness fixes include explicit equipment-slot access, optional-mixin gating, clearing stale totals, configuration recovery/backups, reliable save/discard behavior, and translated UI text.

Install the binary JAR and Fabric API into the client's `mods` directory. Do not install the source or test JAR. Verify downloads against `SHA256SUMS`.

See `VALIDATION.md` for the actual verification record. Release readiness requires all documented release gates to pass. No release or tag is published by the migration.