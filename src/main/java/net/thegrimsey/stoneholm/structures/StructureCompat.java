package net.thegrimsey.stoneholm.structures;

import net.minecraft.block.BlockState;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.thegrimsey.stoneholm.Stoneholm;
//? if >=1.21 {
/*import net.minecraft.structure.StructureLiquidSettings;*/
//?}

public class StructureCompat {
    //? if >=1.21.4 {
    /*public static BlockPos getPos(StructureTemplate.JigsawBlockInfo info) { return info.info().pos(); }
    public static BlockState getState(StructureTemplate.JigsawBlockInfo info) { return info.info().state(); }
    public static Identifier getPoolId(StructureTemplate.JigsawBlockInfo info) { return info.pool(); }*/
    //?} else {
    public static BlockPos getPos(StructureTemplate.StructureBlockInfo info) { return info.pos(); }
    public static BlockState getState(StructureTemplate.StructureBlockInfo info) { return info.state(); }
    public static Identifier getPoolId(StructureTemplate.StructureBlockInfo info) { return Stoneholm.parseId(info.nbt().getString("pool")); }
    //?}

    public static PoolStructurePiece makePiece(StructureTemplateManager manager, StructurePoolElement element, BlockPos pos, int groundLevelDelta, BlockRotation rotation, BlockBox box) {
        //? if >=1.21 {
        /*return new PoolStructurePiece(manager, element, pos, groundLevelDelta, rotation, box, StructureLiquidSettings.APPLY_WATERLOGGING);*/
        //?} else {
        return new PoolStructurePiece(manager, element, pos, groundLevelDelta, rotation, box);
        //?}
    }
}
