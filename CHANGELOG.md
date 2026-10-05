# Changelog

All notable changes to Colossal Furnaces are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project uses [Semantic Versioning](https://semver.org/).

## [1.0.0] - 2026-10-04

### Added

- Colossal Furnace, Colossal Smoker, and Colossal Blast Furnace multiblocks.
- Native NeoForge 26.1.2 support using Java 25.
- Hollow cubic structures from `2x2x2` through `5x5x5`, using one shared Wall, one shared configurable Interface, and a machine-specific Core.
- Automatic structure formation, validation, disassembly, and inventory preservation through the Core.
- Enlarged vanilla-style formed models, specialized textures, localized lighting, and working particles for all three machine families.
- Mod-specific working particles scaled to 2x, 3x, 4x, and 5x for sizes 2 through 5, with equal X/Y/Z smoke velocity scaling and scaled smoke acceleration, preserving vanilla sprites, lifetime, emission width/height ranges, and frequency. Flames scale visually only and retain vanilla furnace motion with zero supplied velocity.
- Compact vanilla-style GUI with 9 input slots, 3 fuel slots, and 9 output slots.
- Universal, Input, Fuel, and Output Interface modes with colored formed-structure markers.
- Empty-hand Shift + right-click Interface configuration and contextual HUD guidance.
- NeoForge item-handler automation on formed Interfaces, filtered automatic pulling from Input and Fuel Interfaces, and automatic pushing from Output Interfaces. Universal Interfaces remain passive.
- Size-scaled batch processing with work carryover: `size^2` work per tick for all three machine types, preserving vanilla's 2x specialist throughput advantage through shorter smoking and blasting recipes.
- Vanilla-style fuel consumption, idle fuel drain, modded fuel support, recipe experience, recipe unlocking, and NeoForge smelting events.
- Colossal Chests-inspired crafting: individual stone Walls convert shapelessly into machine-specific Cores or hopper-based Interfaces.
- Localization, configuration options, and NeoForge game-test coverage for every machine family.

### Fixed

- Revalidating an active machine, including opening its GUI, preserves lower-front light sources and Interface marker lighting without resetting fuel or cooking progress. Shell light caching is invalidated whenever formation is reapplied.

- Machine GUI titles and Inventory labels use vanilla's opaque text rendering instead of transparent RGB-only colors, restoring missing text in all three machine screens without changing their layout.

- Formed smokers and blast furnaces look up the blocks atlas by its definition ID instead of its texture path, fixing client crashes when a Core completes the structure, including corner placements.

- All five block items include client item definitions linking to their existing inventory models, fixing missing-texture icons in inventories, recipe viewers, and the creative tab.

- GameTests use registered functions and serializable vanilla function-based instances, fixing registry synchronization failures that left development clients stuck on Loading Terrain. Regression coverage packs and decodes every test through the login synchronization path.

- Formed smoker and blast furnace faces interpolate normalized atlas sprite coordinates instead of the old 0-16 range, preventing unrelated atlas textures from appearing on the body while preserving animated fronts.

- Furnace and blast furnace front particles spawn 0.10 blocks outward per structure-size unit (0.20-0.50 blocks for sizes 2-5), reducing scaled sprite intersection with the front at oblique angles without changing particle motion or smoker top emissions.

- Short fuels such as bamboo activate lit textures, light, and working particles even when their heat is completely spent within one tick, including size-5 smokers and blast furnaces. Real heat consumption refreshes visual grace and prevents erroneous extra idle drain when refueling leaves the same end-of-tick heat balance.

- Interface automation runs only on the server, preventing client-side ghost exports, flickering or invisible output beside a full barrel, and phantom input/fuel imports.

- Furnace flames no longer receive an upward/outward push or amplified drift; smoke continues rising independently.

- Breaking a lit Core removes it on the first attempt, drops the Core and stored contents once, and restores visible unformed shell blocks instead of resurrecting the Core from its cached lighting state. Replacing an active Core no longer overwrites the replacement block.

- Machine GUI reach is measured from the formed structure's outer bounds instead of the Core, preventing immediate closure when opening from a distant face.

- Unformed Cores cannot open a GUI; disassembly invalidates open menus, while retained contents remain available after rebuilding or drop when the Core is broken.

- Oversized structures are rejected reliably instead of potentially being interpreted as smaller valid structures.
- The shared Interface evaluates NeoForge-defined fuel using its linked Core's recipe type.
- Formed faces use local world lighting and update correctly when adjacent blocks change.
- Formed machine bodies and Interface markers apply vanilla block face shading once, without additional entity-shader darkening.
- Formed textures have the same horizontal orientation as their vanilla counterparts.
- Colossal Smokers emit smoke from the top center and Colossal Blast Furnaces emit front smoke without flame particles, matching their vanilla counterparts.

[1.0.0]: https://github.com/Goldsk1n/ColossalFurnaces/releases/tag/v1.0.0
