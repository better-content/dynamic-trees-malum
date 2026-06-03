package com.dtmalum.dtmalum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

final class DtmalumResourceTest {
    private static final Path TREE_ROOT = Path.of("src/main/resources/trees/dtmalum");
    private static final Path ASSET_ROOT = Path.of("src/main/resources/assets/dtmalum");
    private static final Path GENERATED_ROOT = Path.of("src/generated/resources");
    private static final Set<String> EXPECTED_SPECIES = Set.of(
            "dtmalum:runewood",
            "dtmalum:azure_runewood"
    );
    private static final Map<String, String> PRIMITIVE_SAPLINGS = Map.of(
            "dtmalum:runewood", "malum:runewood_sapling",
            "dtmalum:azure_runewood", "malum:azure_runewood_sapling"
    );

    @Test
    void speciesReferenceExistingFamiliesAndLeavesProperties() throws IOException {
        Set<String> families = resourceIds(TREE_ROOT.resolve("families"));
        Set<String> leavesProperties = resourceIds(TREE_ROOT.resolve("leaves_properties"));
        Set<String> packagedSpecies = resourceIds(TREE_ROOT.resolve("species"));

        assertEquals(EXPECTED_SPECIES, packagedSpecies);

        try (var paths = Files.list(TREE_ROOT.resolve("species"))) {
            for (Path path : paths.filter(DtmalumResourceTest::isJson).toList()) {
                JsonObject species = readObject(path);
                String speciesId = "dtmalum:" + path.getFileName().toString().replaceFirst("\\.json$", "");
                assertTrue(families.contains(species.get("family").getAsString()), "unknown family in " + path);
                assertTrue(leavesProperties.contains(species.get("leaves_properties").getAsString()),
                        "unknown leaves_properties in " + path);
                assertEquals(PRIMITIVE_SAPLINGS.get(speciesId), species.get("primitive_sapling").getAsString());
                assertTrue(species.get("signal_energy").getAsDouble() > 0.0, "signal_energy must be positive in " + path);
                assertTrue(species.get("growth_rate").getAsDouble() > 0.0, "growth_rate must be positive in " + path);
                assertTrue(species.get("up_probability").getAsInt() > 0, "up_probability must be positive in " + path);
                if (species.has("texture_overrides")) {
                    assertFalse(species.getAsJsonObject("texture_overrides").has("seed"),
                            "seed models should use addon-owned item textures in " + path);
                }
                String tree = path.getFileName().toString().replaceFirst("\\.json$", "");
                assertExists(ASSET_ROOT.resolve("textures/item/" + tree + "_seed.png"));
            }
        }
    }

    @Test
    void worldGenTargetsPackagedSpeciesAndRunewoodCanceller() throws IOException {
        Set<String> species = resourceIds(TREE_ROOT.resolve("species"));
        Path defaultWorldGen = TREE_ROOT.resolve("world_gen/default.json");

        JsonArray defaultEntries = JsonParser.parseReader(Files.newBufferedReader(defaultWorldGen)).getAsJsonArray();
        defaultEntries.forEach(element -> {
            JsonObject apply = element.getAsJsonObject().get("apply").isJsonArray()
                    ? element.getAsJsonObject().getAsJsonArray("apply").get(0).getAsJsonObject()
                    : element.getAsJsonObject().getAsJsonObject("apply");
            JsonObject speciesSelection = apply.getAsJsonObject("species");
            assertEquals("splice_before", speciesSelection.get("method").getAsString());
            JsonObject random = speciesSelection.getAsJsonObject("random");
            assertTrue(random.keySet().stream().anyMatch(species::contains), "unknown species in " + defaultWorldGen);
            assertTrue(random.has("..."), "worldgen splice must preserve existing Dynamic Trees species choices");
            assertFalse(apply.has("density"), "worldgen should not override biome tree density");
            assertFalse(apply.has("chance"), "worldgen should not override biome tree chance");
        });
        JsonObject rareRunewood = defaultEntries.get(1).getAsJsonObject();
        assertEquals("#malum:has_rare_runewood", rareRunewood.getAsJsonObject("select").get("tag").getAsString());
        assertTrue(rareRunewood.get("apply").isJsonArray(), "rare runewood should add to existing forest species pools");

        Path cancellers = TREE_ROOT.resolve("world_gen/feature_cancellers.json");
        JsonArray cancellerEntries = JsonParser.parseReader(Files.newBufferedReader(cancellers)).getAsJsonArray();
        assertEquals(4, cancellerEntries.size(), "each Malum runewood biome tag should have a scoped canceller");
        Set<String> cancelledTags = cancellerEntries.asList()
                .stream()
                .map(JsonElement::getAsJsonObject)
                .map(entry -> entry.getAsJsonObject("select").get("tag").getAsString())
                .collect(Collectors.toUnmodifiableSet());
        assertEquals(Set.of(
                "#malum:has_runewood",
                "#malum:has_rare_runewood",
                "#malum:has_azure_runewood",
                "#malum:has_rare_azure_runewood"
        ), cancelledTags);

        cancellerEntries.forEach(element -> {
            JsonObject canceller = element.getAsJsonObject().getAsJsonObject("cancellers");
            assertEquals("dtmalum:runewood_tree", canceller.get("type").getAsString());
            assertTrue(canceller.getAsJsonArray("namespaces")
                    .asList()
                    .stream()
                    .anyMatch(namespace -> "malum".equals(namespace.getAsString())),
                    "Malum cancellers must be scoped to the Malum namespace");
        });
    }

    @Test
    void familiesAndLeavesPointAtMalumPrimitiveBlocks() throws IOException {
        for (String tree : Set.of("runewood", "azure_runewood")) {
            JsonObject family = readObject(TREE_ROOT.resolve("families/" + tree + ".json"));
            assertEquals("malum:runewood_log", family.get("primitive_log").getAsString());
            assertEquals("malum:stripped_runewood_log", family.get("primitive_stripped_log").getAsString());
            assertTrue(family.getAsJsonObject("texture_overrides").has("branch"));
            assertTrue(family.getAsJsonObject("texture_overrides").has("stripped_branch_top"));

            JsonObject leaves = readObject(TREE_ROOT.resolve("leaves_properties/" + tree + ".json"));
            assertEquals("malum:" + tree + "_leaves", leaves.get("primitive_leaves").getAsString());
            assertEquals("dynamictrees:deciduous", leaves.get("cell_kit").getAsString());
        }
    }

    @Test
    void generatedClientDataAndLootResourcesCoverEverySpecies() {
        for (String tree : Set.of("runewood", "azure_runewood")) {
            assertExists(GENERATED_ROOT.resolve("assets/dtmalum/blockstates/" + tree + "_branch.json"));
            assertExists(GENERATED_ROOT.resolve("assets/dtmalum/blockstates/" + tree + "_leaves.json"));
            assertExists(GENERATED_ROOT.resolve("assets/dtmalum/blockstates/" + tree + "_sapling.json"));
            assertExists(GENERATED_ROOT.resolve("assets/dtmalum/blockstates/stripped_" + tree + "_branch.json"));
            assertExists(GENERATED_ROOT.resolve("assets/dtmalum/models/block/" + tree + "_branch.json"));
            assertExists(GENERATED_ROOT.resolve("assets/dtmalum/models/block/stripped_" + tree + "_branch.json"));
            assertExists(GENERATED_ROOT.resolve("assets/dtmalum/models/block/saplings/" + tree + ".json"));
            assertExists(GENERATED_ROOT.resolve("assets/dtmalum/models/item/" + tree + "_branch.json"));
            assertExists(GENERATED_ROOT.resolve("assets/dtmalum/models/item/" + tree + "_seed.json"));
            assertExists(GENERATED_ROOT.resolve("data/dtmalum/loot_tables/blocks/" + tree + "_leaves.json"));
            assertExists(GENERATED_ROOT.resolve("data/dtmalum/loot_tables/trees/branches/" + tree + ".json"));
            assertExists(GENERATED_ROOT.resolve("data/dtmalum/loot_tables/trees/branches/stripped_" + tree + ".json"));
            assertExists(GENERATED_ROOT.resolve("data/dtmalum/loot_tables/trees/leaves/" + tree + ".json"));
            assertExists(GENERATED_ROOT.resolve("data/dtmalum/loot_tables/trees/voluntary/" + tree + ".json"));
        }

        assertExists(GENERATED_ROOT.resolve("assets/dtmalum/lang/en_us.json"));
        assertExists(GENERATED_ROOT.resolve("data/dynamictrees/tags/blocks/saplings.json"));
        assertExists(GENERATED_ROOT.resolve("data/dynamictrees/tags/items/seeds.json"));
    }

    private static Set<String> resourceIds(Path directory) throws IOException {
        try (var paths = Files.list(directory)) {
            Set<String> ids = paths.filter(DtmalumResourceTest::isJson)
                    .map(path -> "dtmalum:" + path.getFileName().toString().replaceFirst("\\.json$", ""))
                    .collect(Collectors.toUnmodifiableSet());
            assertFalse(ids.isEmpty(), "expected resources in " + directory);
            return ids;
        }
    }

    private static boolean isJson(Path path) {
        return path.getFileName().toString().endsWith(".json");
    }

    private static JsonObject readObject(Path path) throws IOException {
        return JsonParser.parseReader(Files.newBufferedReader(path)).getAsJsonObject();
    }

    private static void assertExists(Path path) {
        assertTrue(Files.isRegularFile(path), "expected generated resource " + path);
    }
}
