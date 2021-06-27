package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.thegrimsey.stoneholm.Stoneholm;
import org.jetbrains.annotations.Nullable;

public class NoWaterProcessor extends StructureProcessor {
    public static final Codec<NoWaterProcessor> CODEC = Codec.unit(NoWaterProcessor::new);

    @Nullable
    @Override
    public Structure.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, Structure.StructureBlockInfo structureBlockInfoLocal, Structure.StructureBlockInfo structureBlockInfoWorld, StructurePlacementData data) {
        FluidState fluidState = world.getFluidState(structureBlockInfoWorld.pos);
        if(fluidState != null && !fluidState.isEmpty())
        {
            if(structureBlockInfoLocal.state.contains(Properties.WATERLOGGED) && !structureBlockInfoLocal.state.get(Properties.WATERLOGGED))
            {
                Chunk chunk = world.getChunk(structureBlockInfoWorld.pos);
                chunk.setBlockState(structureBlockInfoWorld.pos, Blocks.AIR.getDefaultState(), false);
            }
        }

        return structureBlockInfoWorld;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Stoneholm.NOWATER_PROCESSOR;
    }
}
