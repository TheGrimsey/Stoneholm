package net.thegrimsey.stoneholm;

import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import net.thegrimsey.stoneholm.mixin.StructureFeatureAccessor;
import net.thegrimsey.stoneholm.structures.UnderGroundVillageStructure;

public class SHStructures {
    public static final StructureFeature<StructurePoolFeatureConfig> UNDERGROUND_VILLAGE = new UnderGroundVillageStructure(StructurePoolFeatureConfig.CODEC);

    public static void registerStructureFeatures() {
        // Create structure config using config values.
        StructureFeatureAccessor.callRegister(Stoneholm.UNDERGROUNDVILLAGE_IDENTIFIER.toString(), UNDERGROUND_VILLAGE, GenerationStep.Feature.SURFACE_STRUCTURES);
    }
}
