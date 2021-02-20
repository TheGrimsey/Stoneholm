package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.Codec;
import net.minecraft.structure.MarginedStructureStart;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import net.thegrimsey.stoneholm.Stoneholm;

public class UnderGroundVillageStructure extends StructureFeature<DefaultFeatureConfig>
{
    public UnderGroundVillageStructure(Codec<DefaultFeatureConfig> codec)
    {
        super(codec);
    }

    @Override
    public StructureStartFactory<DefaultFeatureConfig> getStructureStartFactory() {
        return UnderGroundVillageStructure.Start::new;
    }

    public static class Start extends MarginedStructureStart<DefaultFeatureConfig>
    {
        public Start(StructureFeature<DefaultFeatureConfig> structureIn, int chunkX, int chunkZ, BlockBox blockBox, int referenceIn, long seedIn)
        {
            super(structureIn, chunkX, chunkZ, blockBox, referenceIn, seedIn);
        }

        @Override
        public void init(DynamicRegistryManager registryManager, ChunkGenerator chunkGenerator, StructureManager manager, int chunkX, int chunkZ, Biome biome, DefaultFeatureConfig config)
        {
            // Turns the chunk coordinates into actual coordinates we can use. (Gets center of that chunk)
            int x = chunkX * 16;
            int z = chunkZ * 16;

            BlockPos blockpos = new BlockPos(x, 0, z);

            StructurePoolBasedGenerator.method_30419(registryManager,
                    new StructurePoolFeatureConfig(
                            () -> registryManager.get(Registry.TEMPLATE_POOL_WORLDGEN).get(new Identifier(Stoneholm.MODID, "start_pool")), 10),
                    PoolStructurePiece::new, chunkGenerator, manager, blockpos, this.children, this.random, false, true);

            this.setBoundingBoxFromChildren();
        }
    }
}
