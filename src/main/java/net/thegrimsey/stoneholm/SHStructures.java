package net.thegrimsey.stoneholm;

//? if >=1.21 {
/*import com.mojang.serialization.MapCodec;*/
//?} else {
import com.mojang.serialization.Codec;
//?}
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import net.thegrimsey.stoneholm.structures.DeepslateUnderGroundVillageStructure;
import net.thegrimsey.stoneholm.structures.UnderGroundVillageStructure;

public class SHStructures {
    public static StructureType<?> UNDERGROUND_VILLAGE = null;
    public static StructureType<?> DEEPSLATE_UNDERGROUND_VILLAGE = null;

    public static void registerStructureFeatures() {
        UNDERGROUND_VILLAGE = register(Stoneholm.UNDERGROUNDVILLAGE_IDENTIFIER.toString(), UnderGroundVillageStructure.CODEC);
        DEEPSLATE_UNDERGROUND_VILLAGE = register("stoneholm:deepslate_underground_village", DeepslateUnderGroundVillageStructure.CODEC);
    }

    //? if >=1.21 {
    /*private static <S extends Structure> StructureType<S> register(String id, MapCodec<S> codec) {
        return Registry.register(Registries.STRUCTURE_TYPE, id, () -> codec);
    }*/
    //?} else {
    private static <S extends Structure> StructureType register(String id, Codec<Structure> codec) {
        return Registry.register(Registries.STRUCTURE_TYPE, id, () -> codec);
    }
    //?}
}
