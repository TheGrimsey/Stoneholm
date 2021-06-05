package net.thegrimsey.stoneholm;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.thegrimsey.stoneholm.mixin.StructurePoolAccessor;
import net.thegrimsey.stoneholm.mixin.StructuresConfigAccessor;
import net.thegrimsey.stoneholm.util.StructurePoolUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Stoneholm implements ModInitializer {
    public static final String MODID = "stoneholm";
    public static final Identifier UNDERGROUNDVILLAGE_IDENTIFIER = new Identifier(Stoneholm.MODID, "underground_village");
    public static final Identifier CONFIGURED_UNDERGROUNDVILLAGE_IDENTIFIER = new Identifier(Stoneholm.MODID, "configured_underground_village");
    public static SHConfig CONFIG;

    // Suppress deprecation warnings from Fabric's Biome API.
    @SuppressWarnings("deprecation")
    @Override
    public void onInitialize() {
        // Register config file.
        AutoConfig.register(SHConfig.class, JanksonConfigSerializer::new);
        // Get config.
        CONFIG = AutoConfig.getConfigHolder(SHConfig.class).getConfig();

        // Register structures & configured structures.
        SHStructures.registerStructureFeatures();
        SHConfiguredStructures.registerConfiguredStructures();

        // Set up Biomes to spawn in. We only want to spawn in relatively dry biomes.
        Predicate<BiomeSelectionContext> biomes = BiomeSelectors.categories(Biome.Category.FOREST, Biome.Category.JUNGLE, Biome.Category.DESERT, Biome.Category.PLAINS, Biome.Category.SAVANNA).and(BiomeSelectors.foundInOverworld());

        // Add structures to biomes.
        BiomeModifications.create(UNDERGROUNDVILLAGE_IDENTIFIER)
                .add(ModificationPhase.ADDITIONS,
                        biomes,
                        context -> context.getGenerationSettings().addBuiltInStructure(SHConfiguredStructures.CONFIGURED_UNDERGROUND_VILLAGE));

        // Disable vanilla villages if chosen in config.
        if (CONFIG.disableVanillaVillages)
            removeVanillaVillages();

        ServerLifecycleEvents.SERVER_STARTING.register((MinecraftServer server) -> {
            handleModSupport(server.getRegistryManager());
        });
    }

    /*
     *	Co-opting TelepathicGrunt's removeStructureSpawningFromSelectedDimension function for removing vanilla villages instead.
     * 	https://github.com/TelepathicGrunt/StructureTutorialMod/
     */
    void removeVanillaVillages() {
        // Controls the dimension blacklisting
        ServerWorldEvents.LOAD.register((MinecraftServer minecraftServer, ServerWorld serverWorld) -> {
            // Need temp map as some mods use custom chunk generators with immutable maps in themselves.
            Map<StructureFeature<?>, StructureConfig> tempMap = new HashMap<>(serverWorld.getChunkManager().getChunkGenerator().getStructuresConfig().getStructures());

            if (serverWorld.getRegistryKey().getValue().getNamespace().equals("minecraft")) {
                tempMap.keySet().remove(StructureFeature.VILLAGE);
            }

            // Set the new modified map of structure spacing to the dimension's chunkgenerator.
            ((StructuresConfigAccessor) serverWorld.getChunkManager().getChunkGenerator().getStructuresConfig()).setStructures(tempMap);
        });
    }

    void handleModSupport(DynamicRegistryManager registry)
    {
        // TODO.
    }
}
