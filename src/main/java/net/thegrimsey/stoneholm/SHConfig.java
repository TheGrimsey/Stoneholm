package net.thegrimsey.stoneholm;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = Stoneholm.MODID)
public class SHConfig implements ConfigData {
    @Comment("How many iterations of jigsaw generation we do when generating the village. (Default: 25)")
    public int VILLAGE_SIZE = 25;
}
