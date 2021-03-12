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
        // TODO Grab all values from config.
        /*
         *   Because of L23 in StructureConfig I don't believe spacing & separation to be correctly labeled. I am not sure what they actually mean though, it would require some testing.
         *   I retrieved that information from this guide though:
         *   https://github.com/TelepathicGrunt/StructureTutorialMod/blob/1.16.3-Fabric-Jigsaw/src/main/java/com/telepathicgrunt/structure_tutorial/STStructures.java#L39
         */
        //Maximum space between villages.
        int spacing = 15;
        // Minimum space between villages.
        int separation = 5;

        // Generation seed. Random prime number
        int salt = 8698777;

        FabricStructureBuilder.create(Stoneholm.UNDERGROUNDVILLAGE_IDENTIFIER, UNDERGROUND_VILLAGE)
                .step(GenerationStep.Feature.SURFACE_STRUCTURES)
                .defaultConfig(new StructureConfig(spacing, separation, salt))
                .superflatFeature(UNDERGROUND_VILLAGE.configure(FeatureConfig.DEFAULT))
                .register();
    }
}
