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

    public static void registerStructureFeatures()
    {
        /*
         *   Because of L23 in StructureConfig I don't believe spacing & separation to be correctly labeled. I am not sure what they actually mean though, it would require some testing.
         *   I retrieved that information from this guide though:
         *   https://github.com/TelepathicGrunt/StructureTutorialMod/blob/1.16.3-Fabric-Jigsaw/src/main/java/com/telepathicgrunt/structure_tutorial/STStructures.java#L39
         */
        StructureConfig structureConfig = new StructureConfig(Stoneholm.CONFIG.VILLAGE_SPACING, Stoneholm.CONFIG.VILLAGE_SEPARATION, Stoneholm.CONFIG.VILLAGE_SALT);

        FabricStructureBuilder.create(Stoneholm.UNDERGROUNDVILLAGE_IDENTIFIER, UNDERGROUND_VILLAGE)
                .step(GenerationStep.Feature.SURFACE_STRUCTURES)
                .defaultConfig(structureConfig)
                .superflatFeature(UNDERGROUND_VILLAGE.configure(FeatureConfig.DEFAULT))
                .register();
    }
}
