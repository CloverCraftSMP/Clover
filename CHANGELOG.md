# Changelog

## [Unreleased]
### Added
- Ported all Mastercutter recipes to Clover
  - Can I please have a nap now? -Colonel
- Logs can now be crafted directly into sticks
- Small vanilla flowers can now be duplicated using bonemeal, similar to tall flowers
  - Also includes lilypads and eyeblossoms (26.1+).
  - Excludes torchflower, wither rose, and golden dandelion.
- Brown dye can now be crafted from orange dye and black dye
- Green dye can now be crafted from yellow dye and blue dye
- Black dye can now be crafted from coal / charcoal
- Rose bush can now be crafted from 4 poppy
- Peony can now be crafted from 4 pink tulip
- Lilac can now be crafted from 4 allium
- Sunflower can now be crafted from 4 dandelion
- Concrete can now be crafted from 8 powdered concrete and a water bucket
- Mud can now be crafted from 8 dirt and a water bucket
- Coarse dirt can now be turned into normal dirt in a stonecutter
- Wool can now be turned into string in a stonecutter
### Fixed
- Saving a schematic containing a copper pipe no longer crashes the game
### Changed
- Refactored generated recipes into new subfolders for neatnexss

## [4.1.1] - 2026-11-17
### Added
- Dye duplication recipes
- Player head drops
- Cobblestone -> Gravel -> Sand stonecutter recipes
### Fixed
### Changed
- Bump Stonecutter to 0.10.0, Gradle to 9.2.2 and other versions

## [4.1.0] - 2026-11-13
### Added
### Fixed
- Structure generation works properly
- Loot tables work properly
### Changed

## [4.0.0] - 2026-10-18
### Added
- Huge extension for Simple Copper Pipes!
- New build process, using Stonecutter! Heaps of nice automations for us and we can multi-version target now as well.
- Clover Companion now built into Clover itself.
- LuckPerms syncing for Discord roles -> Minecraft LP group! (with a maintenance mode to require additional permission to join)
### Fixed
### Changed


## [0.4.0] - 2026-07-22
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