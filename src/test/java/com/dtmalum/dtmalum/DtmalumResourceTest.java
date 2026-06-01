package com.dtmalum.dtmalum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                assertTrue(species.has("texture_overrides"), "species must point generated seed models at Malum textures");
            }
        }
    }

    @Test
    void worldGenTargetsPackagedSpeciesAndRunewoodCanceller() throws IOException {
        Set<String> species = resourceIds(TREE_ROOT.resolve("species"));
        Path defaultWorldGen = TREE_ROOT.resolve("world_gen/default.json");

        JsonParser.parseReader(Files.newBufferedReader(defaultWorldGen)).getAsJsonArray().forEach(element -> {
            JsonObject apply = element.getAsJsonObject().getAsJsonObject("apply");
            assertTrue(species.contains(apply.get("species").getAsString()), "unknown species in " + defaultWorldGen);
            assertTrue(apply.get("density").getAsDouble() > 0.0, "density must be positive");
            assertTrue(apply.get("chance").getAsDouble() > 0.0, "chance must be positive");
        });

        Path cancellers = TREE_ROOT.resolve("world_gen/feature_cancellers.json");
        JsonObject canceller = JsonParser.parseReader(Files.newBufferedReader(cancellers))
                .getAsJsonArray()
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("cancellers");

        assertEquals("dtmalum:runewood_tree", canceller.get("type").getAsString());
        assertEquals("malum", canceller.get("namespace").getAsString());
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
