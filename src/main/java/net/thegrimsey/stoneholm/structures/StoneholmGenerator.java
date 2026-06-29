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
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
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
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import net.thegrimsey.stoneholm.Stoneholm;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class StoneholmGenerator {
    static final Logger LOGGER = LogManager.getLogger();

    static final Identifier[] WALL_LIGHTING_POOLS = {
        Stoneholm.id("wall_lighting_lantern"),
        Stoneholm.id("wall_lighting_torch"),
    };

    // Indexed by blockset
    static final Identifier[] CORRIDORS = {
        Stoneholm.id("stone_bricks/corridors"),
        Stoneholm.id("deepslate/corridors")
    };
    static final Identifier[] FUSILAGE = {
        Stoneholm.id("stone_bricks/fusilage"),
        Stoneholm.id("deepslate/fusilage")
    };
    static final Identifier[] CISTERN_FUSILAGE = {
        Stoneholm.id("stone_bricks/cistern_fusilage"),
        Stoneholm.id("deepslate/cistern_fusilage")
    };
    static final Identifier[] CISTERN = {
        Stoneholm.id("stone_bricks/cistern"),
        Stoneholm.id("deepslate/cistern")
    };
    static final Identifier[] BEDROOM = {
        Stoneholm.id("stone_bricks/bedroom"),
        Stoneholm.id("deepslate/bedroom")
    };
    static final Identifier[] COURTYARD = {
        Stoneholm.id("stone_bricks/courtyard"),
        Stoneholm.id("deepslate/courtyard")
    };
    static final Identifier[] JOB = {
        Stoneholm.id("stone_bricks/job"),
        Stoneholm.id("deepslate/job")
    };
    static final Identifier[] EASTER_EGGS = {
        Stoneholm.id("stone_bricks/easter_eggs"),
        Stoneholm.id("deepslate/easter_eggs")
    };
    static final Identifier[] STAIRS = {
        Stoneholm.id("stone_bricks/stairs"),
        Stoneholm.id("deepslate/stairs")
    };
    static final Identifier[] STAIRS_START = {
        Stoneholm.id("stone_bricks/stairs_start"),
        Stoneholm.id("deepslate/stairs_start")
    };
    static final Identifier[] STAIRS_END = {
        Stoneholm.id("stone_bricks/stairs_end"),
        Stoneholm.id("deepslate/stairs_end")
    };
    static final Identifier[] CLUTTER = {
        Stoneholm.id("stone_bricks/clutter"),
        Stoneholm.id("deepslate/clutter")
    };
    static final Identifier[] END_CAP = {
        Stoneholm.id("stone_bricks/end_cap"),
        Stoneholm.id("deepslate/end_cap")
    };
    static final Identifier[] START_POOLS = {
        Stoneholm.id("stone_bricks/start_pool"),
        Stoneholm.id("deepslate/start_pool")
    };

    static final double EXTENTS = 64.0;

    public static Optional<Structure.StructurePosition> generate(Structure.Context inContext, BlockPos pos, BlockSet blockSet) {
        int size = Stoneholm.CONFIG.VILLAGE_SIZE;
        if (size <= 0)
            return Optional.empty();

        DynamicRegistryManager registryManager = inContext.dynamicRegistryManager();
        //? if >=1.21.4 {
        /*Registry<StructurePool> registry = registryManager.getOrThrow(RegistryKeys.TEMPLATE_POOL);*/
        //?} else {
        Registry<StructurePool> registry = registryManager.get(RegistryKeys.TEMPLATE_POOL);
        //?}
        StructurePool structurePool = registry.get(START_POOLS[blockSet.id]);

        ChunkRandom chunkRandom = new ChunkRandom(inContext.random());
        chunkRandom.setCarverSeed(inContext.seed(), inContext.chunkPos().x, inContext.chunkPos().z);

        StructurePoolElement startingElement = structurePool.getRandomElement(chunkRandom);
        if (startingElement == EmptyPoolElement.INSTANCE)
            return Optional.empty();

        ChunkGenerator chunkGenerator = inContext.chunkGenerator();
        StructureTemplateManager structureManager = inContext.structureTemplateManager();
        HeightLimitView heightLimitView = inContext.world();

        BlockRotation blockRotation = BlockRotation.random(chunkRandom);
        PoolStructurePiece poolStructurePiece = StructureCompat.makePiece(structureManager, startingElement, pos, startingElement.getGroundLevelDelta(), blockRotation, startingElement.getBoundingBox(structureManager, pos, blockRotation));
        BlockBox pieceBoundingBox = poolStructurePiece.getBoundingBox();

        int centerX = (pieceBoundingBox.getMaxX() + pieceBoundingBox.getMinX()) / 2;
        int centerZ = (pieceBoundingBox.getMaxZ() + pieceBoundingBox.getMinZ()) / 2;
        int y = pos.getY() + chunkGenerator.getHeightOnGround(centerX, centerZ, Heightmap.Type.WORLD_SURFACE_WG, heightLimitView, inContext.noiseConfig());

        int yOffset = pieceBoundingBox.getMinY() + poolStructurePiece.getGroundLevelDelta();
        poolStructurePiece.translate(0, y - yOffset, 0);

        Box maxExtents = new Box((double) centerX - EXTENTS, inContext.world().getBottomY(), (double) centerZ - EXTENTS,
                //? if >=1.21.4 {
                /*(double) centerX + EXTENTS, inContext.world().getTopYInclusive() + 1, (double) centerZ + EXTENTS);*/
                //?} else {
                (double) centerX + EXTENTS, inContext.world().getTopY(), (double) centerZ + EXTENTS);
                //?}

        return Optional.of(new Structure.StructurePosition(new BlockPos(centerX, y, centerZ), (collector) -> {
            ArrayList<PoolStructurePiece> list = Lists.newArrayList(poolStructurePiece);

            Box box = new Box(centerX - 80, y - 80, centerZ - 80, centerX + 80 + 1, y + 80 + 1, centerZ + 80 + 1);
            StoneholmStructurePoolGenerator structurePoolGenerator = new StoneholmStructurePoolGenerator(registry, size, chunkGenerator, structureManager, list, chunkRandom, blockSet, maxExtents);
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

        final StructurePool fallback_side;
        final StructurePool end_cap;

        final StructurePool wall_lighting;
        final StructurePool corridors;
        final StructurePool bedroom;
        final StructurePool courtyard;

        final StructurePool job;
        final StructurePool easter_eggs;
        final StructurePool stairs;
        final StructurePool stairs_end;
        final StructurePool stairs_start;


        final StructurePool clutter;
        final StructurePool cistern;
        final StructurePool cisternFusilagePool;
        final StructurePoolElement fusilage;
        final StructurePoolElement cisternFusilage;

        final Box maxExtents;

        int yieldedCorridors = 0;
        int yieldedBedrooms = 0;
        int yieldedCourtyards = 0;
        int yieldedJobs = 0;
        int yieldedRooms = 0;

        // Pools that should not be terrain-checked, pre-expanded with all theme variants.
        static final HashSet<Identifier> terrainCheckIgnoredPools;
        static {
            String[] base = { "bee", "deco_blocks", "deco_coverings", "deco_wallpapers", "iron_golem", "villagers", "armor_stands", "corridors", "clutter" };
            String[] themes = { "stone_bricks", "deepslate" };
            terrainCheckIgnoredPools = new HashSet<>(base.length * (themes.length + 1));
            for (String name : base) {
                terrainCheckIgnoredPools.add(Stoneholm.id(name));
                for (String theme : themes) {
                    terrainCheckIgnoredPools.add(Stoneholm.id(theme + "/" + name));
                }
            }
        }

        static final Identifier WALL_LIGHTING = Stoneholm.id("wall_lighting");
        static final Identifier CONNECTORS = Stoneholm.id("connectors");
        static final Identifier STAIRS_ID = Stoneholm.id("stairs");
        static final Identifier STAIRS_START_ID = Stoneholm.id("stairs_start");

        static final Identifier CISTERN_ID = Stoneholm.id("cistern");
        static final Identifier CISTERN_FUSILAGE_ID = Stoneholm.id("cistern_fusilage");
        static final Identifier CLUTTER_ID = Stoneholm.id("clutter");

        static final HashSet<Identifier> NO_FUSILAGE = new HashSet<>(Arrays.asList(
                WALL_LIGHTING,
                Stoneholm.id("misc_room"),
                Stoneholm.id("villager"),
                CISTERN_FUSILAGE_ID,
                CLUTTER_ID
        ));

        StoneholmStructurePoolGenerator(Registry<StructurePool> registry, int maxSize, ChunkGenerator chunkGenerator, StructureTemplateManager structureManager, List<? super PoolStructurePiece> children, ChunkRandom random, BlockSet blockSet, Box maxExtents) {
            this.registry = registry;
            this.maxSize = maxSize;
            this.chunkGenerator = chunkGenerator;
            this.structureManager = structureManager;
            this.children = children;
            this.random = random;

            this.maxExtents = maxExtents;

            wall_lighting = registry.get(WALL_LIGHTING_POOLS[random.nextInt(WALL_LIGHTING_POOLS.length)]);
            clutter = registry.get(CLUTTER[blockSet.id]);
            corridors = registry.get(CORRIDORS[blockSet.id]);
            cistern = registry.get(CISTERN[blockSet.id]);
            cisternFusilagePool = registry.get(CISTERN_FUSILAGE[blockSet.id]);
            fusilage = registry.get(FUSILAGE[blockSet.id]).getRandomElement(random);
            cisternFusilage = cisternFusilagePool.getRandomElement(random);
            bedroom = registry.get(BEDROOM[blockSet.id]);
            courtyard = registry.get(COURTYARD[blockSet.id]);
            job = registry.get(JOB[blockSet.id]);
            easter_eggs = registry.get(EASTER_EGGS[blockSet.id]);

            stairs = registry.get(STAIRS[blockSet.id]);
            stairs_start = registry.get(STAIRS_START[blockSet.id]);
            stairs_end = registry.get(STAIRS_END[blockSet.id]);

            end_cap = registry.get(END_CAP[blockSet.id]);
            fallback_side = end_cap;
        }

        Optional<StructurePool> getPool(Identifier id, ChunkGenerator chunkGenerator, HeightLimitView world, NoiseConfig noiseConfig, BlockPos sourceConnector) {
            if(id.equals(WALL_LIGHTING)) {
                return Optional.of(wall_lighting);
            } else if(id.equals(CONNECTORS)) {
                yieldedRooms++;

                if(yieldedCorridors < 3) {
                    yieldedCorridors++;
                    return Optional.of(corridors);
                }
                float bedroomRatio = (float)yieldedBedrooms / (float)yieldedRooms;
                if(bedroomRatio < 0.2 || this.random.nextInt(100) < 20) {
                    yieldedBedrooms++;
                    return  Optional.of(bedroom);
                }
                float jobRatio = (float)yieldedJobs / (float)yieldedRooms;
                if(jobRatio < 0.3 || this.random.nextInt(100) < 30) {
                    yieldedJobs++;
                    return Optional.of(job);
                }

                double courtyardChance = 0.85 * Math.pow(0.7, yieldedCourtyards);
                if(yieldedCourtyards < 2 && this.random.nextDouble() < courtyardChance) {
                    yieldedCourtyards++;
                    return Optional.of(courtyard);
                }

                if(this.random.nextDouble() < 0.03 && yieldedRooms >= 8) {
                    return Optional.of(easter_eggs);
                }

                yieldedCorridors++;
                return Optional.of(corridors);
            } else if (id.equals(STAIRS_ID)) {
                if(sourceConnector.getY() < chunkGenerator.getHeightOnGround(sourceConnector.getX(), sourceConnector.getZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig) - 21 && this.random.nextDouble() < 0.9) {
                    return Optional.of(stairs_end);
                } else {
                    return Optional.of(stairs);
                }
            } else if (id.equals(STAIRS_START_ID)) {
                return Optional.of(stairs_start);
            } else if (id.equals(CISTERN_ID)) {
                return Optional.of(cistern);
            } else if (id.equals(CISTERN_FUSILAGE_ID)) {
                return Optional.of(cisternFusilagePool);
            } else if (id.equals(CLUTTER_ID)) {
                return Optional.of(clutter);
            } else {
                //? if >=1.21.4 {
                /*return this.registry.getOptionalValue(id);*/
                //?} else {
                return this.registry.getOrEmpty(id);
                //?}
            }
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
            for (var structureBlock : structurePoolElement.getStructureBlockInfos(this.structureManager, sourcePos, sourceRotation, this.random)) {
                BlockPos structureBlockPosition = StructureCompat.getPos(structureBlock);
                if(sourceBlock.equals(structureBlockPosition))
                    continue;
                Identifier structureBlockTargetPoolId = StructureCompat.getPoolId(structureBlock);
                boolean noFusilage = NO_FUSILAGE.contains(structureBlockTargetPoolId);
                int offset = noFusilage ? 1 : 2;

                MutableObject<VoxelShape> structureShape;
                Direction structureBlockFaceDirection = JigsawBlock.getFacing(StructureCompat.getState(structureBlock));
                BlockPos structureBlockAimPosition = structureBlockPosition.offset(structureBlockFaceDirection, offset);

                // Get pool that structure block is targeting.
                Optional<StructurePool> targetPool = this.getPool(structureBlockTargetPoolId, chunkGenerator, world, noiseConfig, structureBlockPosition);
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

                if(structureBlockTargetPoolId.equals(CISTERN_ID)) {
                    tryPlacePiece(piece, this.maxSize, world, noiseConfig, boundsMinY, structureBlock, structureShape, structureBlockFaceDirection, structureBlockPosition, structureBlockPosition.offset(structureBlockFaceDirection), this.cisternFusilage, false);
                } else if(!noFusilage) {
                    tryPlacePiece(piece, this.maxSize, world, noiseConfig, boundsMinY, structureBlock, structureShape, structureBlockFaceDirection, structureBlockPosition, structureBlockPosition.offset(structureBlockFaceDirection), this.fusilage, false);
                }

                boolean doTerrainCheck = currentSize >= 2 && !ignoredPool;
                boolean placed = false;
                if (currentSize < this.maxSize) {
                    for (StructurePoolElement element : targetPool.get().getElementIndicesInRandomOrder(this.random)) {
                        if (element == EmptyPoolElement.INSTANCE) break;
                        placed = tryPlacePiece(piece, currentSize, world, noiseConfig, boundsMinY, structureBlock, structureShape, structureBlockFaceDirection, structureBlockPosition, structureBlockAimPosition, element, doTerrainCheck);
                        if (placed) break;
                    }
                }
                if (!placed) {
                    for (StructurePoolElement element : fallbackPool.getElementIndicesInRandomOrder(this.random)) {
                        if (element == EmptyPoolElement.INSTANCE) break;
                        if (tryPlacePiece(piece, currentSize, world, noiseConfig, boundsMinY, structureBlock, structureShape, structureBlockFaceDirection, structureBlockPosition, structureBlockAimPosition, element, doTerrainCheck)) break;
                    }
                }
            }
        }

        // Returns true if we could place piece.
        boolean tryPlacePiece(PoolStructurePiece piece, int currentSize, HeightLimitView world, NoiseConfig noiseConfig, int boundsMinY,
                //? if >=1.21.4 {
                /*StructureTemplate.JigsawBlockInfo*/
                //?} else {
                StructureTemplate.StructureBlockInfo
                //?}
                structureBlock, MutableObject<VoxelShape> structureShape, Direction structureBlockFaceDirection, BlockPos structureBlockPosition, BlockPos structureBlockAimPosition, StructurePoolElement element, boolean doTerrainCheck) {
            int j = structureBlockPosition.getY() - boundsMinY;
            int t = boundsMinY + j;
            int pieceGroundLevelDelta = piece.getGroundLevelDelta();

            for (BlockRotation randomizedRotation : BlockRotation.randomRotationOrder(this.random)) {
                // Get all structure blocks in structure.
                var structureBlocksInStructure = element.getStructureBlockInfos(this.structureManager, BlockPos.ORIGIN, randomizedRotation, this.random);

                // Loop through all blocks in piece we are trying to place.
                for (var structureBlockInfo : structureBlocksInStructure) {
                    // If the attachment ID doesn't match then skip this one.
                    if (!JigsawBlock.attachmentMatches(structureBlock, structureBlockInfo))
                        continue;

                    BlockPos structureBlockPos = StructureCompat.getPos(structureBlockInfo);
                    BlockPos structureBlockAimDelta = structureBlockAimPosition.subtract(structureBlockPos);
                    BlockBox iteratedStructureBoundingBox = element.getBoundingBox(this.structureManager, structureBlockAimDelta, randomizedRotation);

                    int structureBlockY = structureBlockPos.getY();
                    int o = j - structureBlockY + structureBlockFaceDirection.getOffsetY();
                    int adjustedMinY = boundsMinY + o;
                    int pieceYOffset = adjustedMinY - iteratedStructureBoundingBox.getMinY();
                    BlockBox offsetBoundingBox = iteratedStructureBoundingBox.offset(0, pieceYOffset, 0);
                    Box contractedBox = Box.from(offsetBoundingBox).contract(0.25);
                    VoxelShape offsetVoxelShape = VoxelShapes.cuboid(contractedBox);

                    // If bounding boxes overlap at all; skip.
                    if (VoxelShapes.matchesAnywhere(structureShape.getValue(), offsetVoxelShape, BooleanBiFunction.ONLY_SECOND))
                        continue;

                    boolean entirelyContained = contractedBox.minX >= this.maxExtents.minX && contractedBox.maxX <= this.maxExtents.maxX && contractedBox.minZ >= this.maxExtents.minZ && contractedBox.maxZ <= this.maxExtents.maxZ;
                    if (!entirelyContained)
                        continue;

                    // STONEHOLM CUSTOM: Skip if top of bounding box is above terrain.
                    if(doTerrainCheck && structureBlockFaceDirection != Direction.DOWN) {
                        int maxY = offsetBoundingBox.getMaxY() + 3;
                        int overTerrainCorners = 0;
                        if (maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMinX(), offsetBoundingBox.getMinZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig)) overTerrainCorners++;
                        if (maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMaxX(), offsetBoundingBox.getMaxZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig)) overTerrainCorners++;
                        if (maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMinX(), offsetBoundingBox.getMaxZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig)) overTerrainCorners++;
                        if (overTerrainCorners < 3 && maxY > chunkGenerator.getHeightOnGround(offsetBoundingBox.getMaxX(), offsetBoundingBox.getMinZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig)) overTerrainCorners++;

                        if (overTerrainCorners > 2) {
                            element = (overTerrainCorners > 2 && currentSize + 2 <= maxSize)
                                ? fallback_side.getRandomElement(random)
                                : end_cap.getRandomElement(random);
                            return tryPlacePiece(piece, currentSize, boundsMinY, structureBlock, structureShape, structureBlockPosition, structureBlockAimPosition, element);
                        }
                    }
                    // END STONEHOLM CUSTOM.

                    StructurePool.Projection iteratedProjection = element.getProjection();
                    BlockPos offsetBlockPos = structureBlockAimDelta.add(0, pieceYOffset, 0);

                    // All checks have passed,
                    structureShape.setValue(VoxelShapes.combine(structureShape.getValue(), offsetVoxelShape, BooleanBiFunction.ONLY_FIRST));

                    int s = pieceGroundLevelDelta - o;
                    PoolStructurePiece poolStructurePiece = StructureCompat.makePiece(this.structureManager, element, offsetBlockPos, s, randomizedRotation, offsetBoundingBox);

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
        boolean tryPlacePiece(PoolStructurePiece piece, int currentSize, int boundsMinY,
                //? if >=1.21.4 {
                /*StructureTemplate.JigsawBlockInfo*/
                //?} else {
                StructureTemplate.StructureBlockInfo
                //?}
                structureBlock, MutableObject<VoxelShape> structureShape, BlockPos structureBlockPosition, BlockPos structureBlockAimPosition, StructurePoolElement element) {
            int j = structureBlockPosition.getY() - boundsMinY;
            int t = boundsMinY + j;
            int pieceGroundLevelDelta = piece.getGroundLevelDelta();
            int facingOffsetY = JigsawBlock.getFacing(StructureCompat.getState(structureBlock)).getOffsetY();

            for (BlockRotation randomizedRotation : BlockRotation.randomRotationOrder(this.random)) {
                // Get all structure blocks in structure.
                var structureBlocksInStructure = element.getStructureBlockInfos(this.structureManager, BlockPos.ORIGIN, randomizedRotation, this.random);

                // Loop through all blocks in piece we are trying to place.
                for (var structureBlockInfo : structureBlocksInStructure) {
                    // If the attachment ID doesn't match then skip this one.
                    if (JigsawBlock.attachmentMatches(structureBlock, structureBlockInfo))
                        continue;

                    BlockPos structureBlockPos = StructureCompat.getPos(structureBlockInfo);
                    BlockPos structureBlockAimDelta = structureBlockAimPosition.subtract(structureBlockPos);
                    BlockBox iteratedStructureBoundingBox = element.getBoundingBox(this.structureManager, structureBlockAimDelta, randomizedRotation);

                    int structureBlockY = structureBlockPos.getY();
                    int o = j - structureBlockY + facingOffsetY;
                    int adjustedMinY = boundsMinY + o;
                    int pieceYOffset = adjustedMinY - iteratedStructureBoundingBox.getMinY();
                    BlockBox offsetBoundingBox = iteratedStructureBoundingBox.offset(0, pieceYOffset, 0);
                    VoxelShape offsetVoxelShape = VoxelShapes.cuboid(Box.from(offsetBoundingBox).contract(0.25));

                    // If bounding boxes overlap at all; skip.
                    if (VoxelShapes.matchesAnywhere(structureShape.getValue(), offsetVoxelShape, BooleanBiFunction.ONLY_SECOND))
                        continue;

                    StructurePool.Projection iteratedProjection = element.getProjection();
                    BlockPos offsetBlockPos = structureBlockAimDelta.add(0, pieceYOffset, 0);

                    // All checks have passed,
                    structureShape.setValue(VoxelShapes.combine(structureShape.getValue(), offsetVoxelShape, BooleanBiFunction.ONLY_FIRST));

                    int s = pieceGroundLevelDelta - o;
                    PoolStructurePiece poolStructurePiece = StructureCompat.makePiece(this.structureManager, element, offsetBlockPos, s, randomizedRotation, offsetBoundingBox);

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

