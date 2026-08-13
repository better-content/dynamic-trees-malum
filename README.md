# Dynamic Trees for Malum (`dynamic_trees_malum`)

Forge 1.20.1 addon that provides Dynamic Trees integration for Malum Runewood trees.

## References used (structure + API)

- `DynamicTreesTeam/DynamicTrees-BWG` (branch `1.20.1`) as primary structural reference.
- `Groupix05/DynamicTrees-Template` (branch `1.20.1`) for minimal addon bootstrap.
- `DynamicTreesTeam/DynamicTreesPlus` for worldgen canceller JSON shape.

This project follows the same addon layout pattern:

- `src/main/resources/trees/<modid>/families/*.json`
- `src/main/resources/trees/<modid>/leaves_properties/*.json`
- `src/main/resources/trees/<modid>/species/*.json`
- `src/main/resources/trees/<modid>/world_gen/default.json`
- `src/main/resources/trees/<modid>/world_gen/feature_cancellers.json`

Main mod bootstrap follows current 1.20.1 pattern with:

- `RegistryHandler.setup(MODID)`
- `GatherDataHelper.gatherAllData(...)`

## Versions tested against

- Minecraft: `1.20.1`
- Forge: `47.4.13`
- Dynamic Trees dependency in build: `1.4.9`
- Malum jar inspected for IDs: `malum-1.20.1-1.6.7.jar`

## Trees included

- Runewood
- Azure Runewood

Soulwood is intentionally not implemented in v1.

## Dependency stance

- Required:
  - `dynamictrees`
  - `malum`
- Optional:
  - `dynamictreesplus`
  - `dynamic_trees_addon_lib`

Neither optional dependency is required by the current JSON set.

## Verified Malum IDs used

- `malum:runewood_log`
- `malum:stripped_runewood_log`
- `malum:runewood_leaves`
- `malum:runewood_sapling`
- `malum:azure_runewood_leaves`
- `malum:azure_runewood_sapling`

Note: Malum `1.6.7` does not provide `azure_runewood_log`; Azure Runewood worldgen in Malum itself uses `runewood_log` with azure leaves.

## Worldgen replacement strategy

`dynamic_trees_malum` uses DT worldgen splices plus scoped feature cancellers:

- Splices dynamic species into existing Dynamic Trees species pools in biome tags, without overriding biome tree density/chance:
  - `#malum:has_runewood`
  - `#malum:has_rare_runewood`
  - `#malum:has_azure_runewood`
  - `#malum:has_rare_azure_runewood`
- Cancels Malum's custom `malum:runewood_tree` configured feature type only in those same biome tags and only for the `malum` namespace.

## Tuning worldgen

Edit `src/main/resources/trees/dynamic_trees_malum/world_gen/default.json` and tune the random species weights. Avoid setting per-biome `density` or `chance` unless the pack explicitly wants this addon to change total tree density.

## Known limitations

- Azure Runewood currently shares the Runewood primitive log because that is how Malum `1.6.7` is configured.
- Soulwood integration is deferred.

## Verification

- `./gradlew verifyFast`
- `./gradlew verifyFull`

`verifyFast` runs the unit-test lane. `verifyFull` adds the headless Forge GameTest pass.

## Community and support

For modpack and mod discussion, playtest feedback, and bug reports, join the [Better Content Discord](https://discord.gg/EkRnZbzqS9).

## Identity

The clean-break canonical identity is repository/artifact `dynamic-trees-malum`, mod ID and resource namespace `dynamic_trees_malum`, and Maven group `com.bettercontent`. Legacy `dtmalum` worlds and configs are not migrated.
