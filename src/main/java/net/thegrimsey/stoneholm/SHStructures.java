package net.thegrimsey.stoneholm;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.thegrimsey.stoneholm.structures.UnderGroundVillageStructure;

public class SHStructures
{
    public static StructureFeature<DefaultFeatureConfig> UNDERGROUND_VILLAGE = new UnderGroundVillageStructure(DefaultFeatureConfig.CODEC);

    public static void registerStructureFeatures()
    {
        // Create structure config using config values.
        StructureConfig structureConfig = new StructureConfig(Stoneholm.CONFIG.VILLAGE_MAX_DISTANCE, Stoneholm.CONFIG.VILLAGE_MIN_DISTANCE, 8699777);

        FabricStructureBuilder.create(Stoneholm.UNDERGROUNDVILLAGE_IDENTIFIER, UNDERGROUND_VILLAGE)
                .step(GenerationStep.Feature.TOP_LAYER_MODIFICATION)
                .defaultConfig(structureConfig)
                .superflatFeature(UNDERGROUND_VILLAGE.configure(FeatureConfig.DEFAULT))
                .register();
    }
}
