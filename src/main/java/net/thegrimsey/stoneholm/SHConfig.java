package net.thegrimsey.stoneholm;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = Stoneholm.MODID)
public class SHConfig implements ConfigData {
    public int VILLAGE_SIZE = 12;
    public int VILLAGE_SPACING = 15;
    public int VILLAGE_SEPARATION = 5;

    // These you should generally not change as a player.
    public int VILLAGE_SALT = 8698777;
    public int VILLAGE_MAX_DISTANCE_ABOVE_SEALEVEL = 12;
}
