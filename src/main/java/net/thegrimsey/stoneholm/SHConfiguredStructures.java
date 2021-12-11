package net.thegrimsey.stoneholm;

import net.minecraft.structure.PlainsVillageData;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class SHConfiguredStructures {
    public static ConfiguredStructureFeature<?, ?> CONFIGURED_UNDERGROUND_VILLAGE = SHStructures.UNDERGROUND_VILLAGE
            .configure(new StructurePoolFeatureConfig(() -> PlainsVillageData.STRUCTURE_POOLS, 0));

    public static void registerConfiguredStructures() {
        Registry<ConfiguredStructureFeature<?, ?>> registry = BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE;
        Registry.register(registry, Stoneholm.CONFIGURED_UNDERGROUNDVILLAGE_IDENTIFIER, CONFIGURED_UNDERGROUND_VILLAGE);
    }
}
