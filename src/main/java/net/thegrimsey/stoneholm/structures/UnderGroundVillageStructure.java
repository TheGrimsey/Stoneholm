package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.Codec;
import net.minecraft.block.*;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import net.thegrimsey.stoneholm.SHStructures;
import net.thegrimsey.stoneholm.Stoneholm;

import java.util.Optional;

public class UnderGroundVillageStructure extends Structure {
    public static final Codec<Structure> CODEC = createCodec(UnderGroundVillageStructure::new);
    public static final Identifier START_POOL = new Identifier(Stoneholm.MODID, "stone_bricks/start_pool");

    static final Direction[] directions = new Direction[] {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.EAST,
            Direction.WEST
    };
    static final BooleanProperty[] directionProperties = new BooleanProperty[] {
            HorizontalConnectingBlock.NORTH,
            HorizontalConnectingBlock.SOUTH,
            HorizontalConnectingBlock.EAST,
            HorizontalConnectingBlock.WEST
    };

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

    @Override
    public void postPlace(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox box, ChunkPos chunkPos, StructurePiecesList pieces) {


        pieces.pieces().forEach(structurePiece -> {
            BlockBox bounding_box = structurePiece.getBoundingBox();

            BlockPos.Mutable blockPos = new BlockPos.Mutable();
            for(int x = bounding_box.getMinX(); x <= bounding_box.getMaxX(); x++) {
                for(int y = bounding_box.getMinY(); y <= bounding_box.getMaxY(); y++) {
                    for(int z = bounding_box.getMinZ(); z <= bounding_box.getMaxZ(); z++) {
                        blockPos.set(x, y, z);
                        BlockState blockState = world.getBlockState(blockPos);
                        if(blockState.getBlock() == Blocks.PINK_CONCRETE) {
                            if(
                                world.getBlockState(blockPos.offset(Direction.NORTH)).isAir() && world.getBlockState(blockPos.offset(Direction.SOUTH)).isAir()
                                    || world.getBlockState(blockPos.offset(Direction.EAST)).isAir() && world.getBlockState(blockPos.offset(Direction.WEST)).isAir()
                            ) {
                                BlockState newState = Blocks.IRON_BARS.getDefaultState();

                                for(int i = 0; i < directions.length; i++) {
                                    Direction direction = directions[i];
                                    BooleanProperty directionProperty = directionProperties[i];

                                    BlockPos pos = blockPos.offset(direction);
                                    BlockState state = world.getBlockState(pos);

                                    if(((PaneBlock)Blocks.IRON_BARS).connectsTo(state, state.isSideSolidFullSquare(world, pos, direction.getOpposite()))) {
                                        newState = newState.with(directionProperty, true);
                                    }
                                }

                                world.setBlockState(blockPos, newState, Block.NOTIFY_ALL);
                            } else {
                                BlockState adjacentState = world.getBlockState(blockPos.offset(Direction.DOWN));

                                world.setBlockState(blockPos, adjacentState.getBlock().getDefaultState(), Block.NOTIFY_ALL);
                            }
                        }
                    }

                }
            }
        });

        super.postPlace(world, structureAccessor, chunkGenerator, random, box, chunkPos, pieces);
    }

    public StructureType<?> getType() {
        return SHStructures.UNDERGROUND_VILLAGE;
    }
}
