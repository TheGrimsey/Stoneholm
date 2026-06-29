package net.thegrimsey.stoneholm.structures;

public enum BlockSet {
    STONE_BRICKS(0),
    DEEPSLATE(1);

    final int id;
    BlockSet(int id) {
        this.id = id;
    }
}
