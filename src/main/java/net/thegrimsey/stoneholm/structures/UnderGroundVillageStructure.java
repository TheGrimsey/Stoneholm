package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.Codec;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.structure.StructurePiecesGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import net.thegrimsey.stoneholm.Stoneholm;
import net.thegrimsey.stoneholm.mixin.StructurePoolFeatureConfigAccessor;

import java.util.Optional;

public class UnderGroundVillageStructure extends StructureFeature<StructurePoolFeatureConfig> {
    public static final Identifier START_POOL = new Identifier(Stoneholm.MODID, "start_pool");

    public UnderGroundVillageStructure(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, UnderGroundVillageStructure::createPiecesGenerator);
    }

    public static Optional<StructurePiecesGenerator<StructurePoolFeatureConfig>> createPiecesGenerator(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context) {
        // Turns the chunk coordinates into actual coordinates.
        int x = context.chunkPos().x << 4;
        int z = context.chunkPos().z << 4;

        // Position, set Y to 1 to offset height up.
        BlockPos blockPos = new BlockPos(x, 1, z);

        // TODO: We don't really need to do this since we have our own generator class now but I don't want to mess with it right now.
        ((StructurePoolFeatureConfigAccessor)context.config()).setStructures(() -> context.registryManager().get(Registry.STRUCTURE_POOL_KEY).get(START_POOL));
        ((StructurePoolFeatureConfigAccessor)context.config()).setSize(Stoneholm.CONFIG.VILLAGE_SIZE);

        return StoneholmGenerator.generate(context, PoolStructurePiece::new, blockPos);
    }
}
