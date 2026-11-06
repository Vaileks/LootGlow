# LootGlow

Client-side Fabric mod for Minecraft 1.21.11 by Vaileks.

Highlights supported legendary and epic items on Bloodstone and Runes SMP, enchanted golden apples, totems and maces when dropped on the ground.

## Installation

Requires Java 21+ and Fabric Loader 0.18.4+. Put `LootGlow.jar` in the client `mods` folder. Fabric API and server-side installation are not required.

Settings are created in `config/lootglow.json` after first launch. Categories, colors, distance and additional matching rules can be configured there. Restart Minecraft to apply changes. Bundled item matching rules are in `src/main/resources/lootglow-rules.json`.

## Build

With JDK 21+ installed:

```sh
./gradlew build
```

Output: `build/libs/LootGlow.jar`. The build runs the item matching and configuration checks.

The compiled outline-rendering path can also be checked with:

```sh
python3 tests/verify_render_path.py /path/to/named-minecraft-1.21.11.jar
```

## Links

- [Modrinth](https://modrinth.com/mod/lootglow-vaileksdev)
- [Source](https://github.com/Vaileks/LootGlow)
