package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import net.thegrimsey.stoneholm.SHStructures;
import net.thegrimsey.stoneholm.Stoneholm;

import java.util.Optional;

public class UnderGroundVillageStructure extends Structure {
    public static final Codec<Structure> CODEC = createCodec(UnderGroundVillageStructure::new);
    public static final Identifier START_POOL = new Identifier(Stoneholm.MODID, "main");

    public UnderGroundVillageStructure(Config config) {
        super(config);
    }

    public Optional<StructurePosition> getStructurePosition(Context context) {
        // Turns the chunk coordinates into actual coordinates.
        int x = context.chunkPos().x << 4;
        int z = context.chunkPos().z << 4;

        // Position, set Y to 1 to offset height up.
        BlockPos blockPos = new BlockPos(x, 1, z);

        return StoneholmGenerator.generate(context, blockPos);
    }

    public StructureType<?> getType() {
        return SHStructures.UNDERGROUND_VILLAGE;
    }
}
