/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.thegrimsey.stoneholm.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import net.minecraft.block.JigsawBlock;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.*;
import net.minecraft.structure.pool.*;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import net.thegrimsey.stoneholm.Stoneholm;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;

public class StoneholmGenerator {
    static final Logger LOGGER = LogManager.getLogger();

    public static Optional<Structure.StructurePosition> generate(Structure.Context inContext, BlockPos pos) {
        int size = Stoneholm.CONFIG.VILLAGE_SIZE;
        if (size <= 0)
            return Optional.empty();

        DynamicRegistryManager registryManager = inContext.dynamicRegistryManager();
        Registry<StructurePool> registry = registryManager.get(RegistryKeys.TEMPLATE_POOL);
        StructurePool structurePool = registry.get(UnderGroundVillageStructure.START_POOL);

        ChunkRandom chunkRandom = new ChunkRandom(inContext.random());
        chunkRandom.setCarverSeed(inContext.seed(), inContext.chunkPos().x, inContext.chunkPos().z);

        StructurePoolElement startingElement = structurePool.getRandomElement(chunkRandom);
        if (startingElement == EmptyPoolElement.INSTANCE)
            return Optional.empty();

        ChunkGenerator chunkGenerator = inContext.chunkGenerator();
        StructureTemplateManager structureManager = inContext.structureTemplateManager();
        HeightLimitView heightLimitView = inContext.world();
        Predicate<RegistryEntry<Biome>> biomePredicate = inContext.biomePredicate();

        BlockRotation blockRotation = BlockRotation.random(chunkRandom);
        PoolStructurePiece poolStructurePiece = new PoolStructurePiece(structureManager, startingElement, pos, startingElement.getGroundLevelDelta(), blockRotation, startingElement.getBoundingBox(structureManager, pos, blockRotation));
        BlockBox pieceBoundingBox = poolStructurePiece.getBoundingBox();

        int centerX = (pieceBoundingBox.getMaxX() + pieceBoundingBox.getMinX()) / 2;
        int centerZ = (pieceBoundingBox.getMaxZ() + pieceBoundingBox.getMinZ()) / 2;
        int y = pos.getY() + chunkGenerator.getHeightOnGround(centerX, centerZ, Heightmap.Type.WORLD_SURFACE_WG, heightLimitView, inContext.noiseConfig());

        if (!biomePredicate.test(chunkGenerator.getBiomeSource().getBiome(BiomeCoords.fromBlock(centerX), BiomeCoords.fromBlock(y), BiomeCoords.fromBlock(centerZ), inContext.noiseConfig().getMultiNoiseSampler())))
            return Optional.empty();

        int yOffset = pieceBoundingBox.getMinY() + poolStructurePiece.getGroundLevelDelta();
        poolStructurePiece.translate(0, y - yOffset, 0);

        return Optional.of(new Structure.StructurePosition(new BlockPos(centerX, y, centerZ), (collector) -> {
            ArrayList<PoolStructurePiece> list = Lists.newArrayList(poolStructurePiece);

            Box box = new Box(centerX - 80, y - 80, centerZ - 80, centerX + 80 + 1, y + 80 + 1, centerZ + 80 + 1);
            StoneholmStructurePoolGenerator structurePoolGenerator = new StoneholmStructurePoolGenerator(registry, size, chunkGenerator, structureManager, list, chunkRandom);
            structurePoolGenerator.structurePieces.addLast(new StoneholmShapedPoolStructurePiece(poolStructurePiece, new MutableObject<>(VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box), VoxelShapes.cuboid(Box.from(pieceBoundingBox)), BooleanBiFunction.ONLY_FIRST)), 0, null));

            // Go through all structure pieces in the project.
            while (!structurePoolGenerator.structurePieces.isEmpty()) {
                StoneholmShapedPoolStructurePiece shapedPoolStructurePiece = structurePoolGenerator.structurePieces.removeFirst();
                structurePoolGenerator.generatePiece(shapedPoolStructurePiece.piece, shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.currentSize, shapedPoolStructurePiece.sourceBlockPos, heightLimitView, inContext.noiseConfig());
            }
            list.forEach(collector::addPiece);
        }));
    }


    static final class StoneholmStructurePoolGenerator {
        final Registry<StructurePool> registry;
        final int maxSize;
        final ChunkGenerator chunkGenerator;
        final StructureTemplateManager structureManager;
        final List<? super PoolStructurePiece> children;
        final ChunkRandom random;
        final Deque<StoneholmShapedPoolStructurePiece> structurePieces = Queues.newArrayDeque();

        final StructurePool fallback_down;
        final StructurePool fallback_side;
        final StructurePool end_cap;

        // Terrible hack. Ignore these pools when doing terrainchecks.
        static final HashSet<Identifier> terrainCheckIgnoredPools = new HashSet<>(Arrays.asList(
                new Identifier(Stoneholm.MODID, "bee"),
                new Identifier(Stoneholm.MODID, "deco_blocks"),
                new Identifier(Stoneholm.MODID, "deco_coverings"),
                new Identifier(Stoneholm.MODID, "deco_wallpapers"),
                new Identifier(Stoneholm.MODID, "iron_golem"),
                new Identifier(Stoneholm.MODID, "villagers"),
                new Identifier(Stoneholm.MODID, "armor_stands")
        ));

        StoneholmStructurePoolGenerator(Registry<StructurePool> registry, int maxSize, ChunkGenerator chunkGenerator, StructureTemplateManager structureManager, List<? super PoolStructurePiece> children, ChunkRandom random) {
            this.registry = registry;
            this.maxSize = maxSize;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.children = children;
            this.random = random;

            // TODO: Eventually move fallback pools somewhere else.
            fallback_down = registry.get(new Identifier(Stoneholm.MODID, "fallback_down_pool"));
            fallback_side = registry.get(new Identifier(Stoneholm.MODID, "fallback_side_pool"));
            end_cap = registry.get(new Identifier(Stoneholm.MODID, "end"));
        }

        void generatePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize, BlockPos sourceStructureBlockPos, HeightLimitView world, NoiseConfig noiseConfig) {
            StructurePoolElement structurePoolElement = piece.getPoolElement();
            BlockPos sourcePos = piece.getPos();
            BlockRotation sourceRotation = piece.getRotation();
            MutableObject<VoxelShape> mutableObject = new MutableObject<>();
            BlockBox sourceBoundingBox = piece.getBoundingBox();
            int boundsMinY = sourceBoundingBox.getMinY();

            BlockPos sourceBlock = sourcePos.add(sourceStructureBlockPos == null ? BlockPos.ORIGIN : sourceStructureBlockPos);

            // For every structure block in the piece.
            for (StructureTemplate.StructureBlockInfo structureBlock : structurePoolElement.getStructureBlockInfos(this.structureManager, sourcePos, sourceRotation, this.random)) {
                if(sourceBlock.equals(structureBlock.pos))
                    continue;

                MutableObject<VoxelShape> structureShape;
                Direction structureBlockFaceDirection = JigsawBlock.getFacing(structureBlock.state);
                BlockPos structureBlockPosition = structureBlock.pos;
                BlockPos structureBlockAimPosition = structureBlockPosition.offset(structureBlockFaceDirection);

                // Get pool that structure block is targeting.
                Identifier structureBlockTargetPoolId = new Identifier(structureBlock.nbt.getString("pool"));
                Optional<StructurePool> targetPool = this.registry.getOrEmpty(structureBlockTargetPoolId);
                if (targetPool.isEmpty() || targetPool.get().getElementCount() == 0 && !Objects.equals(structureBlockTargetPoolId, StructurePools.EMPTY.getValue())) {
                    LOGGER.warn("Empty or non-existent pool: {}", structureBlockTargetPoolId);
                    continue;
                }

                boolean ignoredPool = terrainCheckIgnoredPools.contains(structureBlockTargetPoolId);

                // Get end cap pool for target pool.
                RegistryEntry<StructurePool> entry = targetPool.get().getFallback();
                StructurePool fallbackPool = entry.value();
                if (fallbackPool.getElementCount() == 0 && !entry.matchesKey(StructurePools.EMPTY)) {
                    LOGGER.warn("Empty or non-existent fallback pool: {}", entry.getKey().get().getValue());
                    continue;
                }

                // Check if target position is inside current piece's bounding box.
                boolean containsPosition = sourceBoundingBox.contains(structureBlockAimPosition);
                if (containsPosition) {
                    structureShape = mutableObject;
                    if (mutableObject.getValue() == null) {
                        mutableObject.setValue(VoxelShapes.cuboid(Box.from(sourceBoundingBox)));
                    }
                } else {
                    structureShape = pieceShape;
                }

                // Get spawnable elements
                ArrayList<StructurePoolElement> possibleElementsToSpawn = Lists.newArrayList();
                if (currentSize < this.maxSize) {
                    possibleElementsToSpawn.addAll(targetPool.get().getElementIndicesInRandomOrder(this.random)); // Add in pool elements if we haven't reached max size.
                }
                possibleElementsToSpawn.addAll(fallbackPool.getElementIndicesInRandomOrder(this.random)); // Add in terminator elements.

                for (StructurePoolElement iteratedStructureElement : possibleElementsToSpawn) {
                    if (iteratedStructureElement == EmptyPoolElement.INSTANCE)
                        break;

                    boolean placed = tryPlacePiece(piece, currentSize, world, noiseConfig, boundsMinY, structureBlock, structureShape, structureBlockFaceDirection, structureBlockPosition, structureBlockAimPosition, iteratedStructureElement, currentSize >= 2 && !ignoredPool);
                    if(placed)
                        break;
                }
            }
        }

        // Returns true if we could place piece.
        boolean tryPlacePiece(PoolStructurePiece piece, int currentSize, HeightLimitView world, NoiseConfig noiseConfig, int boundsMinY, StructureTemplate.StructureBlockInfo structureBlock, MutableObject<VoxelShape> structureShape, Direction structureBlockFaceDirection, BlockPos structureBlockPosition, BlockPos structureBlockAimPosition, StructurePoolElement element, boolean doTerrainCheck) {
            int j = structureBlockPosition.getY() - boundsMinY;
            int t = boundsMinY + j;
            int pieceGroundLevelDelta = piece.getGroundLevelDelta();

            for (BlockRotation randomizedRotation : BlockRotation.randomRotationOrder(this.random)) {
                // Get all structure blocks in structure.
                List<StructureTemplate.StructureBlockInfo> structureBlocksInStructure = element.getStructureBlockInfos(this.structureManager, BlockPos.ORIGIN, randomizedRotation, this.random);

                // Loop through all blocks in piece we are trying to place.
                for (StructureTemplate.StructureBlockInfo structureBlockInfo : structureBlocksInStructure) {
                    // If the attachment ID doesn't match then skip this one.
                    if (!JigsawBlock.attachmentMatches(structureBlock, structureBlockInfo))
                        continue;

                    BlockPos structureBlockPos = structureBlockInfo.pos;
                    BlockPos structureBlockAimDelta = structureBlockAimPosition.subtract(structureBlockPos);
                    BlockBox iteratedStructureBoundingBox = element.getBoundingBox(this.structureManager, structureBlockAimDelta, randomizedRotation);

                    int structureBlockY = structureBlockPos.getY();
                    int o = j - structureBlockY + JigsawBlock.getFacing(structureBlock.state).getOffsetY();
                    int adjustedMinY = boundsMinY + o;
                    int pieceYOffset = adjustedMinY - iteratedStructureBoundingBox.getMinY();
                    BlockBox offsetBoundingBox = iteratedStructureBoundingBox.offset(0, pieceYOffset, 0);

                    // If bounding boxes overlap at all; skip.
                    if (VoxelShapes.matchesAnywhere(structureShape.getValue(), VoxelShapes.cuboid(Box.from(offsetBoundingBox).contract(0.25)), BooleanBiFunction.ONLY_SECOND))
                        continue;

                    // STONEHOLM CUSTOM: Skip if top of bounding box is above terrain. This is extremely hacky. Like, genuinely this is terrible.
                    if(doTerrainCheck && structureBlockFaceDirection != Direction.DOWN) {
                        int maxYBuffer = 3;
                        int maxY = offsetBoundingBox.getMaxY() + maxYBuffer;

                        boolean minCorner = maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMinX(), offsetBoundingBox.getMinZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
                        boolean maxCorner = maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMaxX(), offsetBoundingBox.getMaxZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
                        boolean minXmaxZ = maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMinX(), offsetBoundingBox.getMaxZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
                        boolean maxXminZ = maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMaxX(), offsetBoundingBox.getMinZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);

                        int overTerrainCorners = (minCorner ? 1 : 0) + (minXmaxZ ? 1 : 0) + (maxCorner ? 1 : 0) + (maxXminZ ? 1 : 0);

                        if (overTerrainCorners > 1) {
                            element = end_cap.getRandomElement(random);

                            if (overTerrainCorners > 2) {
                                if(currentSize + 2 > maxSize)
                                    element = end_cap.getRandomElement(random);
                                else
                                    element = fallback_side.getRandomElement(random);
                            }

                            // If failing switch pool elements to fallback
                            return tryPlacePiece(piece, currentSize, boundsMinY, structureBlock, structureShape, structureBlockPosition, structureBlockAimPosition, element);
                        }
                    }
                    // END STONEHOLM CUSTOM.

                    StructurePool.Projection iteratedProjection = element.getProjection();
                    BlockPos offsetBlockPos = structureBlockAimDelta.add(0, pieceYOffset, 0);

                    // All checks have passed,
                    structureShape.setValue(VoxelShapes.combine(structureShape.getValue(), VoxelShapes.cuboid(Box.from(offsetBoundingBox)), BooleanBiFunction.ONLY_FIRST));

                    int s = pieceGroundLevelDelta - o;
                    PoolStructurePiece poolStructurePiece = new PoolStructurePiece(this.structureManager, element, offsetBlockPos, s, randomizedRotation, offsetBoundingBox);

                    piece.addJunction(new JigsawJunction(structureBlockAimPosition.getX(), t - j + pieceGroundLevelDelta, structureBlockAimPosition.getZ(), o, iteratedProjection));
                    poolStructurePiece.addJunction(new JigsawJunction(structureBlockPosition.getX(), t - structureBlockY + s, structureBlockPosition.getZ(), -o, StructurePool.Projection.RIGID));
                    this.children.add(poolStructurePiece);

                    if (currentSize + 1 <= this.maxSize) // Whilst this is not the end.
                        this.structurePieces.addLast(new StoneholmShapedPoolStructurePiece(poolStructurePiece, structureShape, currentSize + 1, structureBlockPos));

                    return true;
                }
            }

            return false;
        }

        // Returns true if we could place piece.
        boolean tryPlacePiece(PoolStructurePiece piece, int currentSize, int boundsMinY, StructureTemplate.StructureBlockInfo structureBlock, MutableObject<VoxelShape> structureShape, BlockPos structureBlockPosition, BlockPos structureBlockAimPosition, StructurePoolElement element) {
            int j = structureBlockPosition.getY() - boundsMinY;
            int t = boundsMinY + j;
            int pieceGroundLevelDelta = piece.getGroundLevelDelta();

            for (BlockRotation randomizedRotation : BlockRotation.randomRotationOrder(this.random)) {
                // Get all structure blocks in structure.
                List<StructureTemplate.StructureBlockInfo> structureBlocksInStructure = element.getStructureBlockInfos(this.structureManager, BlockPos.ORIGIN, randomizedRotation, this.random);

                // Loop through all blocks in piece we are trying to place.
                for (StructureTemplate.StructureBlockInfo structureBlockInfo : structureBlocksInStructure) {
                    // If the attachment ID doesn't match then skip this one.
                    if (JigsawBlock.attachmentMatches(structureBlock, structureBlockInfo))
                        continue;

                    BlockPos structureBlockPos = structureBlockInfo.pos;
                    BlockPos structureBlockAimDelta = structureBlockAimPosition.subtract(structureBlockPos);
                    BlockBox iteratedStructureBoundingBox = element.getBoundingBox(this.structureManager, structureBlockAimDelta, randomizedRotation);

                    int structureBlockY = structureBlockPos.getY();
                    int o = j - structureBlockY + JigsawBlock.getFacing(structureBlock.state).getOffsetY();
                    int adjustedMinY = boundsMinY + o;
                    int pieceYOffset = adjustedMinY - iteratedStructureBoundingBox.getMinY();
                    BlockBox offsetBoundingBox = iteratedStructureBoundingBox.offset(0, pieceYOffset, 0);

                    // If bounding boxes overlap at all; skip.
                    if (VoxelShapes.matchesAnywhere(structureShape.getValue(), VoxelShapes.cuboid(Box.from(offsetBoundingBox).contract(0.25)), BooleanBiFunction.ONLY_SECOND))
                        continue;

                    StructurePool.Projection iteratedProjection = element.getProjection();
                    BlockPos offsetBlockPos = structureBlockAimDelta.add(0, pieceYOffset, 0);

                    // All checks have passed,
                    structureShape.setValue(VoxelShapes.combine(structureShape.getValue(), VoxelShapes.cuboid(Box.from(offsetBoundingBox)), BooleanBiFunction.ONLY_FIRST));

                    int s = pieceGroundLevelDelta - o;
                    PoolStructurePiece poolStructurePiece = new PoolStructurePiece(this.structureManager, element, offsetBlockPos, s, randomizedRotation, offsetBoundingBox);

                    piece.addJunction(new JigsawJunction(structureBlockAimPosition.getX(), t - j + pieceGroundLevelDelta, structureBlockAimPosition.getZ(), o, iteratedProjection));
                    poolStructurePiece.addJunction(new JigsawJunction(structureBlockPosition.getX(), t - structureBlockY + s, structureBlockPosition.getZ(), -o, StructurePool.Projection.RIGID));
                    this.children.add(poolStructurePiece);

                    if (currentSize + 1 <= this.maxSize) // Whilst this is not the end.
                        this.structurePieces.addLast(new StoneholmShapedPoolStructurePiece(poolStructurePiece, structureShape, currentSize + 1, structureBlockPos));

                    return true;
                }
            }

            return false;
        }
    }

    record StoneholmShapedPoolStructurePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize, BlockPos sourceBlockPos) {}
}

