/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.thegrimsey.stoneholm.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import net.minecraft.block.JigsawBlock;
import net.minecraft.structure.*;
import net.minecraft.structure.pool.EmptyPoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import net.minecraft.world.gen.random.AtomicSimpleRandom;
import net.minecraft.world.gen.random.ChunkRandom;
import net.thegrimsey.stoneholm.Stoneholm;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;

public class StoneholmGenerator {
    static final Logger LOGGER = LogManager.getLogger();

    public static Optional<StructurePiecesGenerator<StructurePoolFeatureConfig>> generate(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context2, PieceFactory pieceFactory, BlockPos pos, boolean idk, boolean placeOnTopOfTerrain) {
        ChunkRandom chunkRandom = new ChunkRandom(new AtomicSimpleRandom(0L));
        chunkRandom.setCarverSeed(context2.seed(), context2.chunkPos().x, context2.chunkPos().z);
        DynamicRegistryManager dynamicRegistryManager = context2.registryManager();
        StructurePoolFeatureConfig structurePoolFeatureConfig = context2.config();
        ChunkGenerator chunkGenerator = context2.chunkGenerator();
        StructureManager structureManager = context2.structureManager();
        HeightLimitView heightLimitView = context2.world();
        Predicate<Biome> predicate = context2.validBiome();
        StructureFeature.init();
        Registry<StructurePool> registry = dynamicRegistryManager.get(Registry.STRUCTURE_POOL_KEY);
        BlockRotation blockRotation = BlockRotation.random(chunkRandom);
        StructurePool structurePool = structurePoolFeatureConfig.getStartPool().get();
        StructurePoolElement structurePoolElement = structurePool.getRandomElement(chunkRandom);
        if (structurePoolElement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }
        PoolStructurePiece poolStructurePiece = pieceFactory.create(structureManager, structurePoolElement, pos, structurePoolElement.getGroundLevelDelta(), blockRotation, structurePoolElement.getBoundingBox(structureManager, pos, blockRotation));
        BlockBox pieceBoundingBox = poolStructurePiece.getBoundingBox();
        int centerX = (pieceBoundingBox.getMaxX() + pieceBoundingBox.getMinX()) / 2;
        int centerZ = (pieceBoundingBox.getMaxZ() + pieceBoundingBox.getMinZ()) / 2;
        int y = placeOnTopOfTerrain ? pos.getY() + chunkGenerator.getHeightOnGround(centerX, centerZ, Heightmap.Type.WORLD_SURFACE_WG, heightLimitView) : pos.getY();
        if (!predicate.test(chunkGenerator.getBiomeForNoiseGen(BiomeCoords.fromBlock(centerX), BiomeCoords.fromBlock(y), BiomeCoords.fromBlock(centerZ)))) {
            return Optional.empty();
        }
        int l = pieceBoundingBox.getMinY() + poolStructurePiece.getGroundLevelDelta();
        poolStructurePiece.translate(0, y - l, 0);
        return Optional.of((structurePiecesCollector, context) -> {
            ArrayList<PoolStructurePiece> list = Lists.newArrayList();
            list.add(poolStructurePiece);
            if (structurePoolFeatureConfig.getSize() <= 0) {
                return;
            }

            Box box = new Box(centerX - 80, y - 80, centerZ - 80, centerX + 80 + 1, y + 80 + 1, centerZ + 80 + 1);
            StructurePoolGenerator structurePoolGenerator = new StructurePoolGenerator(registry, structurePoolFeatureConfig.getSize(), pieceFactory, chunkGenerator, structureManager, list, chunkRandom);
            structurePoolGenerator.structurePieces.addLast(new ShapedPoolStructurePiece(poolStructurePiece, new MutableObject<>(VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box), VoxelShapes.cuboid(Box.from(pieceBoundingBox)), BooleanBiFunction.ONLY_FIRST)), 0));
            while (!structurePoolGenerator.structurePieces.isEmpty()) {
                ShapedPoolStructurePiece shapedPoolStructurePiece = structurePoolGenerator.structurePieces.removeFirst();
                structurePoolGenerator.generatePiece(shapedPoolStructurePiece.piece, shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.currentSize, idk, heightLimitView);
            }
            list.forEach(structurePiecesCollector::addPiece);
        });
    }

    public interface PieceFactory {
        PoolStructurePiece create(StructureManager var1, StructurePoolElement var2, BlockPos var3, int var4, BlockRotation var5, BlockBox var6);
    }

    static final class StructurePoolGenerator {
        private final Registry<StructurePool> registry;
        private final int maxSize;
        private final PieceFactory pieceFactory;
        private final ChunkGenerator chunkGenerator;
        private final StructureManager structureManager;
        private final List<? super PoolStructurePiece> children;
        private final Random random;
        final Deque<ShapedPoolStructurePiece> structurePieces = Queues.newArrayDeque();

        final StructurePool fallback_down;
        final StructurePool fallback_side;
        final StructurePool end_cap;

        StructurePoolGenerator(Registry<StructurePool> registry, int maxSize, PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, List<? super PoolStructurePiece> children, Random random) {
            this.registry = registry;
            this.maxSize = maxSize;
            this.pieceFactory = pieceFactory;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.children = children;
            this.random = random;

            fallback_down = registry.get(new Identifier(Stoneholm.MODID, "fallback_down_pool"));
            fallback_side = registry.get(new Identifier(Stoneholm.MODID, "fallback_side_pool"));
            end_cap = registry.get(new Identifier(Stoneholm.MODID, "end"));
        }

        void generatePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize, boolean modifyBoundingBox, HeightLimitView world) {
            StructurePoolElement structurePoolElement = piece.getPoolElement();
            BlockPos sourcePos = piece.getPos();
            BlockRotation sourceRotation = piece.getRotation();
            MutableObject<VoxelShape> mutableObject = new MutableObject<>();
            BlockBox sourceBoundingBox = piece.getBoundingBox();
            int boundsMinY = sourceBoundingBox.getMinY();

            // For every structure block in the piece.
            for (Structure.StructureBlockInfo structureBlock : structurePoolElement.getStructureBlockInfos(this.structureManager, sourcePos, sourceRotation, this.random)) {
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

                // Get end cap pool for target pool.
                Identifier terminatorPoolId = targetPool.get().getTerminatorsId();
                Optional<StructurePool> terminatorPool = this.registry.getOrEmpty(terminatorPoolId);
                if (terminatorPool.isEmpty() || terminatorPool.get().getElementCount() == 0 && !Objects.equals(terminatorPoolId, StructurePools.EMPTY.getValue())) {
                    LOGGER.warn("Empty or non-existent fallback pool: {}", terminatorPoolId);
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
                possibleElementsToSpawn.addAll(terminatorPool.get().getElementIndicesInRandomOrder(this.random)); // Add in terminator elements.

                for (StructurePoolElement iteratedStructureElement : possibleElementsToSpawn) {
                    if (iteratedStructureElement == EmptyPoolElement.INSTANCE)
                        break;

                    boolean placed = tryPlacePiece(piece, currentSize, modifyBoundingBox, world, boundsMinY, structureBlock, structureShape, structureBlockFaceDirection, structureBlockPosition, structureBlockAimPosition, iteratedStructureElement, false, true);
                    if(placed)
                        break;
                }
            }
        }

        // Returns true if we could place piece.
        boolean tryPlacePiece(PoolStructurePiece piece, int currentSize, boolean modifyBoundingBox, HeightLimitView world, int boundsMinY, Structure.StructureBlockInfo structureBlock, MutableObject<VoxelShape> structureShape, Direction structureBlockFaceDirection, BlockPos structureBlockPosition, BlockPos structureBlockAimPosition, StructurePoolElement element, boolean skipAttachmentCheck, boolean doTerrainCheck) {
            int j = structureBlockPosition.getY() - boundsMinY;
            int t = boundsMinY + j;
            int r = piece.getGroundLevelDelta();

            for (BlockRotation randomizedRotation : BlockRotation.randomRotationOrder(this.random)) {
                // Get all structure blocks in structure.
                List<Structure.StructureBlockInfo> structureBlocksInStructure = element.getStructureBlockInfos(this.structureManager, BlockPos.ORIGIN, randomizedRotation, this.random);

                // Loop through all blocks in piece we are trying to place.
                for (Structure.StructureBlockInfo structureBlockInfo : structureBlocksInStructure) {
                    // If the attachment ID doesn't match then skip this one.
                    if (!skipAttachmentCheck && !JigsawBlock.attachmentMatches(structureBlock, structureBlockInfo))
                        continue;

                    BlockPos structureBlockPos = structureBlockInfo.pos;
                    BlockPos structureBlockAimDelta = structureBlockAimPosition.subtract(structureBlockPos);
                    BlockBox iteratedStructureBoundingBox = element.getBoundingBox(this.structureManager, structureBlockAimDelta, randomizedRotation);
                    StructurePool.Projection iteratedProjection = element.getProjection();
                    int structureBlockY = structureBlockPos.getY();
                    int o = j - structureBlockY + JigsawBlock.getFacing(structureBlock.state).getOffsetY();

                    int adjustedMinY = boundsMinY + o;

                    int pieceYOffset = adjustedMinY - iteratedStructureBoundingBox.getMinY();
                    BlockBox offsetBoundingBox = iteratedStructureBoundingBox.offset(0, pieceYOffset, 0);
                    BlockPos offsetBlockPos = structureBlockAimDelta.add(0, pieceYOffset, 0);

                    // STONEHOLM CUSTOM: Skip if top of bounding box is above terrain. This is extremely hacky. Like, genuinely this is terrible.
                    if(doTerrainCheck) {
                        int maxY = offsetBoundingBox.getMaxY();
                        int maxOffset = 3;

                        boolean minCorner = maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMinX(), offsetBoundingBox.getMinZ(), Heightmap.Type.WORLD_SURFACE_WG, world) + maxOffset;
                        boolean maxCorner = maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMaxX(), offsetBoundingBox.getMaxZ(), Heightmap.Type.WORLD_SURFACE_WG, world) + maxOffset;
                        boolean minXmaxZ = maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMinX(), offsetBoundingBox.getMaxZ(), Heightmap.Type.WORLD_SURFACE_WG, world) + maxOffset;
                        boolean maxXminZ = maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMaxX(), offsetBoundingBox.getMinZ(), Heightmap.Type.WORLD_SURFACE_WG, world) + maxOffset;

                        int overTerrainCorners = (minCorner ? 1 : 0) + (minXmaxZ ? 1 : 0) + (maxCorner ? 1 : 0) + (maxXminZ ? 1 : 0);

                        if (overTerrainCorners > 1) {
                            element = end_cap.getRandomElement(random);

                            if (overTerrainCorners > 2) {
                                if(currentSize + 2 > maxSize)
                                    element = end_cap.getRandomElement(random);
                                else if (structureBlockFaceDirection == Direction.DOWN)
                                    element = fallback_down.getRandomElement(random);
                                else if (structureBlockFaceDirection != Direction.UP)
                                    element = fallback_side.getRandomElement(random);
                            }

                            // If failing switch pool elements to fallback
                            return tryPlacePiece(piece, currentSize, modifyBoundingBox, world, boundsMinY, structureBlock, structureShape, structureBlockFaceDirection, structureBlockPosition, structureBlockAimPosition, element, true, false);
                        }
                    }
                    // END STONEHOLM CUSTOM.

                    // If bounding boxes overlap at all; skip.
                    if (VoxelShapes.matchesAnywhere(structureShape.getValue(), VoxelShapes.cuboid(Box.from(offsetBoundingBox).contract(0.25)), BooleanBiFunction.ONLY_SECOND))
                        continue;

                    // All checks have passed,
                    structureShape.setValue(VoxelShapes.combine(structureShape.getValue(), VoxelShapes.cuboid(Box.from(offsetBoundingBox)), BooleanBiFunction.ONLY_FIRST));

                    int s = r - o;
                    PoolStructurePiece poolStructurePiece = this.pieceFactory.create(this.structureManager, element, offsetBlockPos, s, randomizedRotation, offsetBoundingBox);

                    piece.addJunction(new JigsawJunction(structureBlockAimPosition.getX(), t - j + r, structureBlockAimPosition.getZ(), o, iteratedProjection));
                    poolStructurePiece.addJunction(new JigsawJunction(structureBlockPosition.getX(), t - structureBlockY + s, structureBlockPosition.getZ(), -o, StructurePool.Projection.RIGID));
                    this.children.add(poolStructurePiece);

                    if (!(currentSize + 1 > this.maxSize)) // TODO Clean up.
                        this.structurePieces.addLast(new ShapedPoolStructurePiece(poolStructurePiece, structureShape, currentSize + 1));

                    return true;
                }
            }

            return false;
        }
    }

    record ShapedPoolStructurePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize) {}
}
