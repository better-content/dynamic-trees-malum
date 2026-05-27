package com.dtmalum.dtmalum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

final class DtmalumResourceTest {
    private static final Path TREE_ROOT = Path.of("src/main/resources/trees/dtmalum");

    @Test
    void speciesReferenceExistingFamiliesAndLeavesProperties() throws IOException {
        Set<String> families = resourceIds(TREE_ROOT.resolve("families"));
        Set<String> leavesProperties = resourceIds(TREE_ROOT.resolve("leaves_properties"));

        try (var paths = Files.list(TREE_ROOT.resolve("species"))) {
            for (Path path : paths.filter(DtmalumResourceTest::isJson).toList()) {
                JsonObject species = readObject(path);
                assertTrue(families.contains(species.get("family").getAsString()), "unknown family in " + path);
                assertTrue(leavesProperties.contains(species.get("leaves_properties").getAsString()),
                        "unknown leaves_properties in " + path);
                assertTrue(species.get("signal_energy").getAsDouble() > 0.0, "signal_energy must be positive in " + path);
                assertTrue(species.get("growth_rate").getAsDouble() > 0.0, "growth_rate must be positive in " + path);
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
}
