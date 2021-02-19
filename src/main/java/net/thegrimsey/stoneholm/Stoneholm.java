package net.thegrimsey.stoneholm;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Stoneholm implements ModInitializer {
	public static final String MODID = "stoneholm";
	public static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitialize() {
		SHStructures.setupAndRegisterStructureFeatures();
		SHConfiguredStructures.registerConfiguredStructures();

		BiomeModifications.create(new Identifier(MODID, "underground_village"))
				.add(   // Describes what we are doing. SInce we are adding a structure, we choose ADDITIONS.
						ModificationPhase.ADDITIONS,

						// Add our structure to all biomes including other modded biomes.
						// You can filter to certain biomes based on stuff like temperature, scale, precipitation, mod id.
						BiomeSelectors.categories(Biome.Category.PLAINS, Biome.Category.DESERT, Biome.Category.JUNGLE, Biome.Category.SWAMP, Biome.Category.FOREST).and(BiomeSelectors.foundInOverworld()),

						// context is basically the biome itself. This is where you do the changes to the biome.
						// Here, we will add our ConfiguredStructureFeature to the biome.
						context -> {
							context.getGenerationSettings().addBuiltInStructure(SHConfiguredStructures.CONFIGURED_UNDERGROUND_VILLAGE);
						});

		System.out.println("Stoneholm initialized!");
	}
}
