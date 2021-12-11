package net.thegrimsey.stoneholm;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = Stoneholm.MODID)
public class SHConfig implements ConfigData {
    @Comment("How many iterations of jigsaw generation we do when generating the village. (Default: 10)")
    public int VILLAGE_SIZE = 10;
    @Comment("Max distance in chunks between villages. (Default: 32)")
    public int VILLAGE_SPACING = 32;
    @Comment("Minimum distance in chunks between villages. (Default: 8)")
    public int VILLAGE_SEPARATION = 8;

    @Comment("Whether to disable generating vanilla villages. (Default: false)")
    public boolean disableVanillaVillages = false;
}
