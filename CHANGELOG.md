# Changelog

## [Unreleased]
### Added
- Version compatibility checks to ensure mod compatibility at runtime
### Fixed
- ~~Tide satchel deletion / desync due to fish stack sizes~~
- ~~Tide crates occasionally create / delete fluid blocks~~
- ~~Vanilla Backport prevents mobs from being leashable~~
  - Above have been subsequently mostly fixed by offending mods, Tide has required a mixin to convert old satchels to new system (otherwise they're wiped...).
- Supplementaries safe block password set to player name (*still* broken even with an [attempted fix](https://github.com/MehVahdJukaar/Supplementaries/commit/5e27621641111107e6ca68792e1081ac7ca0e7c4))
- Nether build limit is too low
- Modpack checker overriding reload command
### Changed
- Edited enderscapes end vault loot table to have mending
- Updated to support new mod versions

## [0.3.3] - 2026-05-20
### Added
### Fixed
- Tide satchel item deletion glitch through replacement mixin
### Changed

## [0.3.2] - 2026-05-18
### Added
### Fixed
- Larion lava in end bug through mixin (project seems unmaintained)
### Changed
- Using mixin constraints for more clear code

## [0.3.1] - 2026-05-16
### Added
- Anti crop trampling
  - Prevent mobs from trampling crops
  - Player requires feather falling to not trample
- No mending villagers
- Better phantoms
  - Phantoms only attack players who haven't slept
  - Gamerule for attacking players while burning
### Fixed
- Saddle dupe in clutter bestiary
### Changed
- Structure spacing and separation

## [0.2.2] - 2026-05-01
### Added
- Changelog template
### Fixed
- Changelog generation
- Version bumping
### Changed

## 0.2.0 - 2026-05-01
### Added
- Initial release with changelog