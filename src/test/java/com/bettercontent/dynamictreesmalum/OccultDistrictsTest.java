package com.bettercontent.dynamictreesmalum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OccultDistrictsTest {
    @Test void fixedSeedIsStableAcrossSerialParallelAndReorderedQueries() {
        long seed = 84291L;
        var points = new ArrayList<long[]>();
        for (int x = -4096; x <= 4096; x += 97) {
            for (int z = -4096; z <= 4096; z += 113) {
                points.add(new long[] {x, z});
            }
        }
        List<Double> expected = points.stream()
            .map(p -> OccultDistricts.occultStrength(seed, (int) p[0], (int) p[1])).toList();
        assertEquals(expected, points.parallelStream()
            .map(p -> OccultDistricts.occultStrength(seed, (int) p[0], (int) p[1])).toList());
        Collections.reverse(points);
        for (int i = 0; i < points.size(); i++) {
            assertEquals(expected.get(expected.size() - 1 - i),
                OccultDistricts.occultStrength(seed, (int) points.get(i)[0], (int) points.get(i)[1]));
        }
    }

    @Test void sampledFieldHasOrdinaryGapsAndAllThreeDistrictBands() {
        int ordinary = 0, fringe = 0, interior = 0, core = 0;
        for (int x = -8192; x <= 8192; x += 64) {
            for (int z = -8192; z <= 8192; z += 64) {
                switch (OccultDistricts.band(991L, x, z)) {
                    case ORDINARY -> ordinary++;
                    case FRINGE -> fringe++;
                    case INTERIOR -> interior++;
                    case CORE -> core++;
                }
            }
        }
        int total = ordinary + fringe + interior + core;
        assertTrue(ordinary > total * .10 && ordinary < total * .75, "ordinary gaps should remain common");
        assertTrue(fringe > total * .05, "fringes should be visible");
        assertTrue(interior > total * .05, "interiors should be substantial");
        assertTrue(core > total * .01, "cores should be rare but represented");
    }
}
