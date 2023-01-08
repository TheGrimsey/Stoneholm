package net.thegrimsey.stoneholm.structures;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.thegrimsey.stoneholm.Stoneholm;

import java.util.Arrays;
import java.util.HashSet;

public class GenerationHelpers {
    public static final HashSet<Identifier> terrainCheckIgnoredPools = new HashSet<>(Arrays.asList(
            new Identifier(Stoneholm.MODID, "bee"),
            new Identifier(Stoneholm.MODID, "deco_blocks"),
            new Identifier(Stoneholm.MODID, "deco_coverings"),
            new Identifier(Stoneholm.MODID, "deco_wallpapers"),
            new Identifier(Stoneholm.MODID, "iron_golem"),
            new Identifier(Stoneholm.MODID, "villagers"),
            new Identifier(Stoneholm.MODID, "armor_stands")
    ));

    public static final Identifier FALLBACK_POOL_DOWN_ID = new Identifier(Stoneholm.MODID, "fallback_down_pool");
    public static final Identifier FALLBACK_POOL_SIDE_ID = new Identifier(Stoneholm.MODID, "fallback_side_pool");
    public static final Identifier END_CAP_POOL_ID = new Identifier(Stoneholm.MODID, "end_cap");

    public static int pieceOverFlowingCorners(BlockBox boundingBox, ChunkGenerator chunkGenerator, HeightLimitView world, NoiseConfig noiseConfig) {
        int maxYBuffer = 3;
        int maxY = boundingBox.getMaxY() + maxYBuffer;

        boolean minCorner = maxY > chunkGenerator.getHeightOnGround(boundingBox.getMinX(), boundingBox.getMinZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
        boolean maxCorner = maxY > chunkGenerator.getHeightOnGround(boundingBox.getMaxX(), boundingBox.getMaxZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
        boolean minXmaxZ = maxY > chunkGenerator.getHeightOnGround(boundingBox.getMinX(), boundingBox.getMaxZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
        boolean maxXminZ = maxY > chunkGenerator.getHeightOnGround(boundingBox.getMaxX(), boundingBox.getMinZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);

        return (minCorner ? 1 : 0) + (minXmaxZ ? 1 : 0) + (maxCorner ? 1 : 0) + (maxXminZ ? 1 : 0);
    }
}
