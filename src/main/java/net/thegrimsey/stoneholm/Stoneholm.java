package net.thegrimsey.stoneholm;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class Stoneholm implements ModInitializer {
	public static final String MODID = "stoneholm";

	@Override
	public void onInitialize() {
		SHStructures.setupAndRegisterStructureFeatures();
		SHConfiguredStructures.registerConfiguredStructures();

		BiomeModifications.create(new Identifier(MODID, "underground_village"))
				.add(ModificationPhase.ADDITIONS,
						BiomeSelectors.includeByKey(BiomeKeys.DARK_FOREST, BiomeKeys.FOREST, BiomeKeys.BIRCH_FOREST, BiomeKeys.JUNGLE, BiomeKeys.TALL_BIRCH_FOREST, BiomeKeys.TAIGA, BiomeKeys.FLOWER_FOREST, BiomeKeys.GIANT_TREE_TAIGA, BiomeKeys.GIANT_SPRUCE_TAIGA,
								BiomeKeys.SAVANNA, BiomeKeys.PLAINS, BiomeKeys.SNOWY_TUNDRA),
						context -> {
							context.getGenerationSettings().addBuiltInStructure(SHConfiguredStructures.CONFIGURED_UNDERGROUND_VILLAGE);
						});
	}
}
