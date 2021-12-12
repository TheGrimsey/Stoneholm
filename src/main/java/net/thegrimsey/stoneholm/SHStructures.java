package net.thegrimsey.stoneholm;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import net.thegrimsey.stoneholm.structures.UnderGroundVillageStructure;

public class SHStructures {
    public static final StructureFeature<StructurePoolFeatureConfig> UNDERGROUND_VILLAGE = new UnderGroundVillageStructure(StructurePoolFeatureConfig.CODEC);

    public static void registerStructureFeatures() {
        // Create structure config using config values.
        StructureConfig structureConfig = new StructureConfig(Stoneholm.CONFIG.VILLAGE_SPACING, Stoneholm.CONFIG.VILLAGE_SEPARATION, 8699777);

        FabricStructureBuilder.create(Stoneholm.UNDERGROUNDVILLAGE_IDENTIFIER, UNDERGROUND_VILLAGE)
                .step(GenerationStep.Feature.SURFACE_STRUCTURES)
                .defaultConfig(structureConfig)
                .register();
    }
}
