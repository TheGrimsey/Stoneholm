package net.thegrimsey.stoneholm;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = Stoneholm.MODID)
public class SHConfig implements ConfigData {
    // How many iterations of jigsaw generation we do when generating the village.
    public int VILLAGE_SIZE = 12;
    // Max distance in chunks between villages.
    public int VILLAGE_MAX_DISTANCE = 34;
    // Minimum distance in chunks between villages.
    public int VILLAGE_MIN_DISTANCE = 18;
    // How far above sea level a village may be. This is used to prevent pieces from generating in the air in less even terrains.
    public int VILLAGE_MAX_DISTANCE_ABOVE_SEALEVEL = 10;
}
