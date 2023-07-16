package net.thegrimsey.stoneholm.structures;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public enum BlockSet {
    STONE_BRICKS(0, Blocks.STONE_BRICKS);

    final int id;
    final Block fallback_block;

    BlockSet(int id, Block fallback_block) {
        this.id = id;
        this.fallback_block = fallback_block;
    }
}
