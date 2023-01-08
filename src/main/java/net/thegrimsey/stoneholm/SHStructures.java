package net.thegrimsey.stoneholm;

import com.mojang.serialization.Codec;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import net.thegrimsey.stoneholm.structures.UnderGroundVillageStructure;

public class SHStructures {
    public static StructureType<?> UNDERGROUND_VILLAGE = null;

    public static void registerStructureFeatures() {
        // Create structure config using config values.
        UNDERGROUND_VILLAGE = register(Stoneholm.UNDERGROUNDVILLAGE_IDENTIFIER.toString(), UnderGroundVillageStructure.CODEC);
    }

    private static <S extends Structure> StructureType register(String id, Codec<Structure> codec) {
        return Registry.register(Registry.STRUCTURE_TYPE, id, () -> codec);
    }
}
