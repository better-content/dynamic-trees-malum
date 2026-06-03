package com.dtmalum.dtmalum.gametest;

import com.dtmalum.dtmalum.Dtmalum;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(Dtmalum.MODID)
@PrefixGameTestTemplate(false)
@Mod.EventBusSubscriber(modid = Dtmalum.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DtmalumGameTests {
    private DtmalumGameTests() {
    }

    @SubscribeEvent
    public static void register(final RegisterGameTestsEvent event) {
        event.register(DtmalumGameTests.class);
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "dtmalum_resources", timeoutTicks = 80)
    public static void dynamicRunewoodResourcesLoad(final GameTestHelper helper) {
        assertDynamicTree(helper, "runewood", "malum:runewood_sapling");
        assertDynamicTree(helper, "azure_runewood", "malum:azure_runewood_sapling");
        helper.succeed();
    }

    private static void assertDynamicTree(final GameTestHelper helper, final String tree, final String primitiveSapling) {
        Species species = Species.REGISTRY.get(Dtmalum.location(tree));
        helper.assertTrue(species.isValid(), "species should load: " + tree);
        helper.assertTrue(species.getFamily().isValid(), "family should load for " + tree);
        helper.assertTrue(species.getLeavesProperties().isValid(), "leaves properties should load for " + tree);
        helper.assertTrue(species.getPrimitiveLeaves().isPresent(), "primitive leaves should resolve for " + tree);
        helper.assertTrue(species.hasSeed(), "generated seed should be registered for " + tree);
        helper.assertTrue(species.getSapling().isPresent(), "generated dynamic sapling should be registered for " + tree);
        helper.assertTrue(
                ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(primitiveSapling)),
                "primitive sapling should exist: " + primitiveSapling
        );
    }
}
