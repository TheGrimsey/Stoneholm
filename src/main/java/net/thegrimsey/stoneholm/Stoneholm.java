package net.thegrimsey.stoneholm;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.function.Predicate;

public class Stoneholm implements ModInitializer {
	public static final String MODID = "stoneholm";
	public static final Identifier UNDERGROUNDVILLAGE_IDENTIFIER = new Identifier(Stoneholm.MODID, "underground_village");
	public static final Identifier CONFIGURED_UNDERGROUNDVILLAGE_IDENTIFIER = new Identifier(Stoneholm.MODID, "configured_underground_village");
	public static SHConfig CONFIG;

	// Suppress deprecation warnings from Fabric's Biome API.
	@SuppressWarnings ("deprecation")
	@Override
	public void onInitialize() {
		// Register config file.
		AutoConfig.register(SHConfig.class, Toml4jConfigSerializer::new);
		// Get config.
		CONFIG = AutoConfig.getConfigHolder(SHConfig.class).getConfig();

		// Register structures & configured structures.
		SHStructures.registerStructureFeatures();
		SHConfiguredStructures.registerConfiguredStructures();

		// Set up Biomes to spawn in. We only want to spawn in relatively dry biomes. There is an additional check in UndergroundVillageStructure:init to ensure we don't spawn too high up and accidentally build out in the air.
		Predicate<BiomeSelectionContext> biomes = BiomeSelectors.categories(Biome.Category.FOREST, Biome.Category.JUNGLE, Biome.Category.DESERT, Biome.Category.PLAINS, Biome.Category.SAVANNA);

		// Add structures to biomes.
		BiomeModifications.create(UNDERGROUNDVILLAGE_IDENTIFIER)
				.add(ModificationPhase.ADDITIONS,
						biomes,
						context -> context.getGenerationSettings().addBuiltInStructure(SHConfiguredStructures.CONFIGURED_UNDERGROUND_VILLAGE));
	}
}
