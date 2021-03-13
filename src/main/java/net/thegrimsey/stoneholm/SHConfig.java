package net.thegrimsey.stoneholm;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = Stoneholm.MODID)
public class SHConfig implements ConfigData {
    public int VILLAGE_SIZE = 12;
    public int VILLAGE_SPACING = 15;
    public int VILLAGE_SEPARATION = 5;

    public int VILLAGE_SALT = 8698777;
}
