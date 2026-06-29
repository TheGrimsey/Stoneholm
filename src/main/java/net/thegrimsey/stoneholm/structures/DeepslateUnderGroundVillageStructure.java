package net.thegrimsey.stoneholm.structures;

//? if >=1.21 {
/*import com.mojang.serialization.MapCodec;*/
//?} else {
import com.mojang.serialization.Codec;
//?}
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import net.thegrimsey.stoneholm.SHStructures;

import java.util.Optional;

public class DeepslateUnderGroundVillageStructure extends UnderGroundVillageStructure {
    //? if >=1.21 {
    /*public static final MapCodec<DeepslateUnderGroundVillageStructure> CODEC = createCodec(DeepslateUnderGroundVillageStructure::new);*/
    //?} else {
    public static final Codec<Structure> CODEC = createCodec(DeepslateUnderGroundVillageStructure::new);
    //?}

    public DeepslateUnderGroundVillageStructure(Config config) {
        super(config);
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        int x = context.chunkPos().x << 4;
        int z = context.chunkPos().z << 4;
        return StoneholmGenerator.generate(context, new BlockPos(x, 0, z), BlockSet.DEEPSLATE);
    }

    @Override
    public StructureType<?> getType() {
        return SHStructures.DEEPSLATE_UNDERGROUND_VILLAGE;
    }
}
