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

    final static Block[] BARS = {
            Blocks.IRON_BARS,
            Blocks.GLASS_PANE,
            Blocks.WHITE_STAINED_GLASS_PANE,
            Blocks.ORANGE_STAINED_GLASS_PANE,
            Blocks.MAGENTA_STAINED_GLASS_PANE,
            Blocks.LIGHT_BLUE_STAINED_GLASS_PANE,
            Blocks.YELLOW_STAINED_GLASS_PANE,
            Blocks.LIME_STAINED_GLASS_PANE,
            Blocks.PINK_STAINED_GLASS_PANE,
            Blocks.GRAY_STAINED_GLASS_PANE,
            Blocks.LIGHT_GRAY_STAINED_GLASS_PANE,
            Blocks.CYAN_STAINED_GLASS_PANE,
            Blocks.PURPLE_STAINED_GLASS_PANE,
            Blocks.BLUE_STAINED_GLASS_PANE,
            Blocks.BROWN_STAINED_GLASS_PANE,
            Blocks.GREEN_STAINED_GLASS_PANE,
            Blocks.RED_STAINED_GLASS_PANE,
            Blocks.BLACK_STAINED_GLASS_PANE,
    };

    @Override
    public void postPlace(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox box, ChunkPos chunkPos, StructurePiecesList pieces) {
        Block newBlock = BARS[random.nextInt(BARS.length)];

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
                                BlockState newState = newBlock.getDefaultState();

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
