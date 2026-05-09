# Dynamic Trees for Malum (`dtmalum`)

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
- Forge: `47.3.0`
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

`dtmalum` uses DT worldgen + feature cancellers:

- Adds dynamic species generation in biome tags:
  - `#malum:has_runewood`
  - `#malum:has_rare_runewood`
  - `#malum:has_azure_runewood`
  - `#malum:has_rare_azure_runewood`
- Cancels static Malum tree features only in those same biome tag selections via:
  - `type: tree`
  - `namespace: malum`

This is intentionally scoped to tree generation and avoids broad namespace-wide non-tree removals.

## Tuning worldgen

Edit:

- `src/main/resources/trees/dtmalum/world_gen/default.json`

Tune per-entry:

- `density`
- `chance`

## Known limitations

- Azure Runewood currently shares the Runewood primitive log because that is how Malum `1.6.7` is configured.
- Canceller currently targets Malum tree-type features by namespace in selected Malum biome tags; if Malum adds other `tree`-type features in those tags in future versions, narrow selectors further.
- Soulwood integration is deferred.
