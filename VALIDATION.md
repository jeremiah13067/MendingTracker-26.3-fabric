# Validation record

Status: verification in progress. This document will be updated with actual results before release readiness is declared.

Target: Minecraft 26.3, Java 25, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3.

Required gates:
- Clean Java 25 builds and unit tests on Windows and Linux.
- Packaged-client integration tests for none, Mod Menu, MaLiLib, MiniHUD + MaLiLib, and all optional mods.
- Equipment tracking, world lifecycle, configuration, debug/F1 suppression, rendering screenshots, and optional integration persistence.
- Binary/source JAR inspection and SHA-256 checksums.
- Passing CI on the final pull request commit.

The GitHub Actions artifacts contain build reports, packaged release files, client logs, and screenshots. Unexecuted or failed runtime checks remain release blockers.