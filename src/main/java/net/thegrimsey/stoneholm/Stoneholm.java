package net.thegrimsey.stoneholm;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;

import java.util.function.Predicate;

public class Stoneholm implements ModInitializer {
	public static final String MODID = "stoneholm";
	public static final Identifier UNDERGROUNDVILLAGE_IDENTIFIER = new Identifier(Stoneholm.MODID, "underground_village");
	public static final Identifier CONFIGURED_UNDERGROUNDVILLAGE_IDENTIFIER = new Identifier(Stoneholm.MODID, "configured_underground_village");

	@Override
	public void onInitialize() {
		SHStructures.registerStructureFeatures();
		SHConfiguredStructures.registerConfiguredStructures();

		// Set up Biomes to spawn in. We only spawn in relatively flat biomes to try and stop the issue of "Underground Village is not underground because we are in a hill and it went out of the side of it."
		Predicate<BiomeSelectionContext> biomes = BiomeSelectors.includeByKey(BiomeKeys.DARK_FOREST, BiomeKeys.FOREST, BiomeKeys.BIRCH_FOREST, BiomeKeys.JUNGLE, BiomeKeys.TALL_BIRCH_FOREST, BiomeKeys.TAIGA, BiomeKeys.FLOWER_FOREST, BiomeKeys.GIANT_TREE_TAIGA, BiomeKeys.GIANT_SPRUCE_TAIGA,
				BiomeKeys.SAVANNA, BiomeKeys.PLAINS, BiomeKeys.SNOWY_TUNDRA);

		BiomeModifications.create(UNDERGROUNDVILLAGE_IDENTIFIER)
				.add(ModificationPhase.ADDITIONS,
						biomes,
						context -> {
							context.getGenerationSettings().addBuiltInStructure(SHConfiguredStructures.CONFIGURED_UNDERGROUND_VILLAGE);
						});
	}
}
