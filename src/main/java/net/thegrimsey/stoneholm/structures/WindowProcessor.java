package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.thegrimsey.stoneholm.Stoneholm;
import org.jetbrains.annotations.Nullable;

public class WindowProcessor extends StructureProcessor {
    public static final Codec<WindowProcessor> CODEC = Codec.unit(WindowProcessor::new);

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlacementData data) {
        if(structureBlockInfoLocal.state().isOf(Blocks.PINK_CONCRETE)) {
            BlockPos worldPos = structureBlockInfoWorld.pos();
            Chunk chunk = world.getChunk(worldPos);

            boolean north = chunk.getBlockState(worldPos.offset(Direction.NORTH)).isAir();
            boolean south = chunk.getBlockState(worldPos.offset(Direction.SOUTH)).isAir();

            boolean east = chunk.getBlockState(worldPos.offset(Direction.EAST)).isAir();
            boolean west = chunk.getBlockState(worldPos.offset(Direction.WEST)).isAir();

            // Create windows if two opposite directions are air.
            boolean shouldCreate = north && south
                        || east && west;

            if(shouldCreate)
            {
                return new StructureTemplate.StructureBlockInfo(worldPos, Blocks.IRON_BARS.getDefaultState(), null);
            } else {
                return new StructureTemplate.StructureBlockInfo(worldPos, Blocks.COBBLESTONE.getDefaultState(), null);
            }
        }

        return structureBlockInfoWorld;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Stoneholm.NOWATER_PROCESSOR;
    }
}
