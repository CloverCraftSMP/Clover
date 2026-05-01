<div align="center">
<h1>Clover</h1>

**Core mod for CloverCraft SMP**

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62b47a?style=for-the-badge&logo=minecraft&logoColor=white)
![Fabric](https://img.shields.io/badge/Fabric-required-dbb98a?style=for-the-badge)
![Modrinth](https://img.shields.io/modrinth/dt/SnT761HH?style=for-the-badge&logo=modrinth&color=62b47a&label=Downloads)
![License](https://img.shields.io/github/license/CloverCraftSMP/Clover?style=for-the-badge&color=4a9eff)

</div>

---

## About

Clover adds a collection of hand-crafted structures to the overworld. Every structure was built by the CloverCraft community.

Clover also serves as the coremod for the [CloverCraft SMP](https://modrinth.com/modpack/clovercraftsmp-season-3) modpack, bundling compatibility fixes and minor patches needed to keep everything running smoothly together.

---

## Compatibility

Clover is built to play nice with other mods. If you run into an issue, please [open an issue](https://github.com/CloverCraftSMP/Clover/issues) with your mod list and a crash report or description of the problem.

Known compatibility notes will be listed here as they come up.

---

## Development

```bash
# Clone the repo
git clone https://github.com/CloverCraftSMP/Clover.git
cd Clover
 
# Run the dev client
./gradlew :1.21.1:runClient
 
# Run datagen (regenerates structure JSON files)
./gradlew :1.21.1:runDatagen
 
# Build
./gradlew build
```

Dev builds are published automatically on every push to `main` and are available on the [Releases](https://github.com/CloverCraftSMP/Clover/releases) page as pre-releases.

---

<div align="center">
*Part of [CloverCraft SMP](https://modrinth.com/modpack/clovercraftsmp-season-3) - Season 3 <3*
</div>