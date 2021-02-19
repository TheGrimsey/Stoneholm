package net.thegrimsey.stoneholm;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.thegrimsey.stoneholm.structures.UnderGroundVillageStructure;

public class SHStructures
{
    public static StructureFeature<DefaultFeatureConfig> UNDERGROUND_VILLAGE = new UnderGroundVillageStructure(DefaultFeatureConfig.CODEC);

    public static void setupAndRegisterStructureFeatures()
    {
        FabricStructureBuilder.create(new Identifier(Stoneholm.MODID, "underground_village"), UNDERGROUND_VILLAGE)
                .step(GenerationStep.Feature.UNDERGROUND_DECORATION)
                .defaultConfig(new StructureConfig(10, 5, 20210218))
                .superflatFeature(UNDERGROUND_VILLAGE.configure(FeatureConfig.DEFAULT))
                .register();
    }
}
