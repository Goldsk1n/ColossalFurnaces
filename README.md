# Colossal Furnaces

Colossal Furnaces adds scalable, hollow multiblock versions of the vanilla furnace, smoker, and blast furnace. A completed structure renders as one enlarged vanilla-style machine and provides a compact inventory with configurable automation interfaces.

## Requirements

- Minecraft 26.1.2
- NeoForge 26.1.2.104 or newer in the 26.1.2 line
- No required library mods

## Machine Types

| Machine | Accepted recipes | Processing rate |
| --- | --- | --- |
| Colossal Furnace | Vanilla smelting recipes | `size^2` work per tick |
| Colossal Smoker | Vanilla smoking recipes | `size^2` work per tick |
| Colossal Blast Furnace | Vanilla blasting recipes | `size^2` work per tick |

The smoker only processes items with smoking recipes. The blast furnace only processes items with blasting recipes. They do not fall back to ordinary smelting recipes.

Working particles reuse vanilla sprites: front smoke and flames for furnaces, top-center smoke for smokers, and front smoke without flames for blast furnaces. Particle size matches structure size: 2x, 3x, 4x, and 5x for sizes 2 through 5. Front particles spawn 0.20, 0.30, 0.40, and 0.50 blocks outward from the front surface respectively, reducing sprite intersection at oblique viewing angles. Smoker top emissions and front emission width/height ranges are unchanged. Smoke velocity/spread on all three axes and its upward acceleration use the same multiplier. Flames receive zero supplied velocity and retain vanilla's tiny unscaled drift rather than rising like smoke. Frequency, lifetime, fading, and collision behavior are unchanged; ordinary vanilla blocks keep their original particles.

## Construction

Build a complete hollow cube from the shared Wall and Interface blocks, then choose its machine type with the Core:

- Use exactly one **Core** anywhere in the shell.
- Fill every other boundary position with shared **Colossal Furnace Wall** or **Colossal Furnace Interface** blocks.
- Keep every position inside the cube empty.
- Use a **Colossal Furnace Core**, **Colossal Smoker Core**, or **Colossal Blast Furnace Core** to select the machine behavior.
- Supported outer sizes are `2x2x2` through `5x5x5` by default.

| Outer size | Total shell blocks | Core blocks | Available wall/interface positions |
| --- | ---: | ---: | ---: |
| `2x2x2` | 8 | 1 | 7 |
| `3x3x3` | 26 | 1 | 25 |
| `4x4x4` | 56 | 1 | 55 |
| `5x5x5` | 98 | 1 | 97 |

The structure forms automatically when the shell becomes valid. The Core's horizontal facing determines the front of the assembled machine. If automatic structure revalidation is disabled, right-click the Core to validate the structure manually.

Breaking or replacing a required shell block disassembles the machine when automatic revalidation is enabled and closes any open machine GUI. Stored inventory remains associated with the Core but cannot be accessed until the structure is rebuilt. Breaking the Core drops the machine contents, including while unformed.

## Inventory and Processing

Every size and machine type uses the same compact inventory:

- 9 input slots
- 3 fuel slots
- 9 output slots
- No pages

The machine applies its entire work budget to the next valid item. It does not become slower when only one input slot is occupied. If enough work remains after an item finishes, processing continues into the next item during the same tick. This provides batch throughput without requiring every input slot to be populated.

Fuel efficiency follows vanilla behavior. Increasing the structure size increases the rate at which work and fuel burn time are consumed, but does not reduce the total fuel cost of a recipe. Already-burning fuel also continues to drain when processing is idle or output is blocked.

Working visuals respond to actual heat consumption, including short fuels fully spent within one tick. Lit textures, light, and particles have a four-tick shutdown grace; this does not grant extra cooking heat, and the GUI flame reflects only remaining fuel/heat.

Completed recipes accumulate experience like a vanilla furnace. Manually taking an output grants the stored recipe experience, unlocks the completed recipes, and fires the standard NeoForge smelting event. Automated extraction does not grant experience to a player; the experience remains stored until a player takes an output. Breaking the Core releases any remaining stored experience into the world.

## Speed Tables

One work unit is equivalent to one tick of recipe progress in the corresponding vanilla machine.

### Colossal Furnace

The completion values below use a standard 200-tick smelting recipe.

| Size | Work/tick | Speed vs. vanilla furnace | Completion time | Seconds |
| --- | ---: | ---: | ---: | ---: |
| `2x2x2` | 4 | 4x | 50 ticks | 2.50 s |
| `3x3x3` | 9 | 9x | 23 ticks | 1.15 s |
| `4x4x4` | 16 | 16x | 13 ticks | 0.65 s |
| `5x5x5` | 25 | 25x | 8 ticks | 0.40 s |

### Colossal Smoker and Blast Furnace

The completion values below use a standard 100-tick smoking or blasting recipe.

| Size | Work/tick | Speed vs. vanilla specialist | Completion time | Seconds |
| --- | ---: | ---: | ---: | ---: |
| `2x2x2` | 4 | 4x | 25 ticks | 1.25 s |
| `3x3x3` | 9 | 9x | 12 ticks | 0.60 s |
| `4x4x4` | 16 | 16x | 7 ticks | 0.35 s |
| `5x5x5` | 25 | 25x | 4 ticks | 0.20 s |

All three machine types apply the same size-squared work budget. Standard smoking and blasting recipes take half as much work as standard smelting recipes, naturally preserving vanilla's 2x specialist throughput advantage at the same structure size without an additional speed multiplier.

Actual completion time is `ceil(recipe cooking time / work per tick)`. Sustained batches can use leftover work in a completion tick, so long-run throughput is more precise than repeatedly rounding each item up to a whole tick.

## Controls

- **Right-click any block in a formed structure:** Open the machine GUI.
- **Move away from the structure:** The GUI closes beyond eight blocks from the nearest point on the machine's outer bounds, regardless of where the Core is placed.
- **Right-click an unformed Core:** Validate the structure. If it is invalid, show the configured structure error without opening a GUI; if it becomes valid, open the GUI.
- **Shift + right-click a formed Interface with an empty hand:** Cycle its mode.
- **Look at a formed Interface:** Show its current mode and the configuration hint on the HUD.

Interface modes cycle in this order:

`Universal -> Input -> Fuel -> Output -> Universal`

The assembled structure displays a colored marker over each Interface:

| Mode | Marker color | Item capability behavior |
| --- | --- | --- |
| Universal | Neutral gray | Insert valid inputs or fuel and extract outputs through the combined inventory |
| Input | Yellow | Exposes 9 input slots; accepts and automatically pulls items cookable by the linked machine |
| Fuel | Red | Exposes 3 fuel slots; accepts and automatically pulls fuel valid for the linked machine |
| Output | Green | Exposes 9 output slots for extraction and enables automatic output pushing |

## Automation

Attach hoppers, pipes, or other NeoForge item handlers to formed Interface blocks. Interfaces expose item capabilities according to their selected mode.

An **Input** Interface automatically pulls cookable items from adjacent inventories outside the multiblock. A **Fuel** Interface automatically pulls valid fuel instead. Each Interface attempts a transfer every 8 ticks, moving up to 64 items of one item type into its corresponding machine inventory. Transfers respect the source inventory's sided extraction rules and stop when the machine has no room. Invalid items remain in the source inventory.

An **Output** Interface automatically pushes items into adjacent inventories outside the multiblock every 8 ticks. It checks each outward-facing side of that Interface and transfers from the first output slot that the destination can accept.

Automatic transfers run only on the server, activate through the selected Interface mode, and only operate while the structure is formed. Full destinations leave output stacks unchanged. **Universal** remains passive: it never automatically pulls or pushes, avoiding ambiguous routing for items that can be both recipe inputs and fuel. Hoppers and pipes can still interact with its combined inventory.

For predictable automation, dedicate separate Interfaces to Input, Fuel, and Output. Universal mode is useful for general-purpose access but exposes all 21 machine slots as one combined handler.

## Configuration

The common NeoForge configuration is generated at:

```text
config/colossalfurnaces-common.toml
```

| Option | Default | Range | Description |
| --- | --- | --- | --- |
| `maxColossalFurnaceSize` | `5` | `2` to `5` | Maximum outer cube size accepted for all machine types |
| `autoRevalidateStructure` | `true` | Boolean | Revalidate nearby formed structures whenever shell blocks change |
| `showStructureErrorMessages` | `true` | Boolean | Show structure validation errors to players |

Reducing the configured maximum prevents larger structures from forming. The hard implementation limit is currently `5x5x5`.

## Crafting

The shared Wall recipe combines four cobblestone with one smooth stone and produces one Wall. A Wall combines shapelessly with a furnace, smoker, or blast furnace to produce the corresponding Core, or with a hopper to produce an Interface. Because every Core and Interface consumes one Wall, a machine requires exactly one initially crafted Wall per shell position. Use the vanilla recipe book or an installed recipe viewer such as JEI for the layouts.

## Building from Source

```text
./gradlew build
```

On Windows:

```text
gradlew.bat build
```

The release JAR is written to `build/libs/colossalfurnaces-neoforge-26.1.2-1.0.0.jar`. Its filename identifies the loader, Minecraft version, and mod version; do not install a different port's JAR.

For this port's atlas API source-contract check, run:

```text
powershell.exe -NoProfile -ExecutionPolicy Bypass -File ./scripts/verify-renderer-atlas-contract.ps1
```

Run a build first to provide the local Minecraft sources. This checks atlas lookup IDs, texture binding, and normalized UV calls; it does not replace in-game rendering and resource-reload tests.

## License

The source code and original project assets are available under the MIT License. Assets derived from or incorporating Minecraft artwork, including the screenshot-based mod logo, are not MIT-licensed; they remain subject to Mojang and Microsoft's rights and the Minecraft EULA and Usage Guidelines. See [LICENSE](LICENSE) for the MIT terms and Minecraft artwork notice.

Minecraft is a trademark of Microsoft Corporation. This project is not affiliated with or endorsed by Microsoft or Mojang.
