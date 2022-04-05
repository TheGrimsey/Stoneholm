package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.Codec;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.PostPlacementProcessor;
import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.structure.StructurePiecesGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import net.thegrimsey.stoneholm.Stoneholm;

import java.util.Optional;

public class UnderGroundVillageStructure extends StructureFeature<StructurePoolFeatureConfig> {
    public static final Identifier START_POOL = new Identifier(Stoneholm.MODID, "start_pool");

    public UnderGroundVillageStructure(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, UnderGroundVillageStructure::createPiecesGenerator, PostPlacementProcessor.EMPTY);
    }

    public static Optional<StructurePiecesGenerator<StructurePoolFeatureConfig>> createPiecesGenerator(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context) {
        // Turns the chunk coordinates into actual coordinates.
        int x = context.chunkPos().x << 4;
        int z = context.chunkPos().z << 4;

        // Position, set Y to 1 to offset height up.
        BlockPos blockPos = new BlockPos(x, 1, z);

        return StoneholmGenerator.generate(context, PoolStructurePiece::new, blockPos);
    }
}
