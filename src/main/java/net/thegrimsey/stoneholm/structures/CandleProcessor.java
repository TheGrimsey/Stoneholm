package net.thegrimsey.stoneholm.structures;

import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.thegrimsey.stoneholm.Stoneholm;
import org.jetbrains.annotations.Nullable;

public class CandleProcessor extends StructureProcessor {
    public static final Codec<CandleProcessor> CODEC = Codec.unit(CandleProcessor::new);

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlacementData data) {
        // Check so this is a stone brick.
        if(structureBlockInfoWorld.state.getBlock() != Blocks.YELLOW_CONCRETE_POWDER) {
            return structureBlockInfoWorld;
        }
        DebugHud

        Chunk chunk = world.getChunk(structureBlockInfoWorld.pos);

        Random random = data.getRandom(structureBlockInfoWorld.pos);

        if(random.nextFloat() < 0.7) {
            chunk.setBlockState(structureBlockInfoWorld.pos, Blocks.CANDLE.getDefaultState().with(CandleBlock.CANDLES, 4).with(CandleBlock.LIT, true), false);
        } else {
            chunk.setBlockState(structureBlockInfoWorld.pos, Blocks.STONE_BRICKS.getDefaultState(), false);
        }

        return structureBlockInfoWorld;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Stoneholm.NOWATER_PROCESSOR;
    }
}
