package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.Codec;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.structure.StructurePiecesGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import net.thegrimsey.stoneholm.Stoneholm;
import net.thegrimsey.stoneholm.mixin.StructurePoolFeatureConfigAccessor;

import java.util.Optional;

public class UnderGroundVillageStructure extends StructureFeature<StructurePoolFeatureConfig> {
    public static Identifier START_POOL = new Identifier(Stoneholm.MODID, "start_pool");

    public UnderGroundVillageStructure(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, context -> {
            if(!canGenerate(context))
                return Optional.empty();

            return createPiecesGenerator(context);
        });
    }

    private static boolean canGenerate(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context) {
        return true;
        // We don't want to spawn too far above the sea level because then we may end up spawning pieces above ground.
        // Bit-shift chunkX & Y for theoretical performance improvements. It is unclear if this really matters, I believe the compiler should be intelligent enough to do this on it's own.
        //int terrainHeight = context.chunkGenerator().getHeightOnGround(context.chunkPos().x << 4, context.chunkPos().z  << 4, Heightmap.Type.WORLD_SURFACE_WG, context.world());
        //int maxHeight = context.chunkGenerator().getSeaLevel() + Stoneholm.CONFIG.VILLAGE_MAX_DISTANCE_ABOVE_SEALEVEL;

        //return terrainHeight <= maxHeight;
    }

    public static Optional<StructurePiecesGenerator<StructurePoolFeatureConfig>> createPiecesGenerator(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context) {
        // Turns the chunk coordinates into actual coordinates.
        int x = context.chunkPos().x << 4;
        int z = context.chunkPos().z << 4;

        // Position, we don't care about Y as we will just be placed on top on the terrain.
        BlockPos blockPos = new BlockPos(x, 0, z);

        ((StructurePoolFeatureConfigAccessor)context.config()).setStructures(() -> context.registryManager().get(Registry.STRUCTURE_POOL_KEY).get(START_POOL));
        ((StructurePoolFeatureConfigAccessor)context.config()).setSize(Stoneholm.CONFIG.VILLAGE_SIZE);

        Optional<StructurePiecesGenerator<StructurePoolFeatureConfig>> piecesGenerator = StoneholmGenerator.generate(context, PoolStructurePiece::new, blockPos, false, true);

        return piecesGenerator;
    }
}
