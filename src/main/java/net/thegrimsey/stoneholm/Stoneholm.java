package net.thegrimsey.stoneholm;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.impl.biome.modification.BiomeSelectionContextImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.thegrimsey.stoneholm.mixin.StructuresConfigAccessor;
import net.thegrimsey.stoneholm.structures.NoWaterProcessor;
import net.thegrimsey.stoneholm.util.StructurePoolUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Stoneholm implements ModInitializer {
    public static final String MODID = "stoneholm";
    public static final Identifier UNDERGROUNDVILLAGE_IDENTIFIER = new Identifier(Stoneholm.MODID, "underground_village");
    public static final Identifier CONFIGURED_UNDERGROUNDVILLAGE_IDENTIFIER = new Identifier(Stoneholm.MODID, "configured_underground_village");
    public static SHConfig CONFIG;
    public static final StructureProcessorType<NoWaterProcessor> NOWATER_PROCESSOR = () -> NoWaterProcessor.CODEC;

    // Suppress deprecation warnings from Fabric's Biome API.
    @Override
    public void onInitialize() {
        // Register config file.
        AutoConfig.register(SHConfig.class, JanksonConfigSerializer::new);
        // Get config.
        CONFIG = AutoConfig.getConfigHolder(SHConfig.class).getConfig();

        // Register structures & configured structures.
        SHStructures.registerStructureFeatures();
        SHConfiguredStructures.registerConfiguredStructures();
        Registry.register(Registry.STRUCTURE_PROCESSOR, new Identifier(MODID, "nowater_processor"), NOWATER_PROCESSOR);

        // BIOME MODIFICATIONS ARE CURRENTLY BROKEN. Fix when fixed.
        // Set up Biomes to spawn in.
        Predicate<BiomeSelectionContext> biomes = BiomeSelectors.categories(Biome.Category.OCEAN, Biome.Category.UNDERGROUND).negate().and(BiomeSelectors.foundInOverworld());

        // Add structures to biomes.
        //BiomeModifications.addStructure(biomes, RegistryKey.of(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY, BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.getId(SHConfiguredStructures.CONFIGURED_UNDERGROUND_VILLAGE)));

        // TEMP DELETE WHEN BIOME MODICATIONS IS FIXED.
        ServerWorldEvents.LOAD.register((server, world) -> {
            var structureSettings = world.getChunkManager().getChunkGenerator().getStructuresConfig();

            if(!((StructuresConfigAccessor) structureSettings).getConfiguredStructures().containsKey(SHConfiguredStructures.CONFIGURED_UNDERGROUND_VILLAGE.feature)) {
                ImmutableMap.Builder<StructureFeature<?>, ImmutableMultimap<ConfiguredStructureFeature<?, ?>, RegistryKey<Biome>>> immutableStructureMap = ImmutableMap.builder();
                immutableStructureMap.putAll(((StructuresConfigAccessor) structureSettings).getConfiguredStructures());

                ImmutableMultimap.Builder<ConfiguredStructureFeature<?, ?>, RegistryKey<Biome>> structureImmutableMapBuilder = ImmutableMultimap.builder();

                BuiltinRegistries.BIOME.forEach(biome -> {
                    var key = BuiltinRegistries.BIOME.getKey(biome).get();
                    if(biomes.test(new BiomeSelectionContextImpl(server.getRegistryManager(), key, biome))) {
                        structureImmutableMapBuilder.put(SHConfiguredStructures.CONFIGURED_UNDERGROUND_VILLAGE, key);
                    }
                });
                immutableStructureMap.put(SHConfiguredStructures.CONFIGURED_UNDERGROUND_VILLAGE.feature, structureImmutableMapBuilder.build());

                // Set it in the field.
                ((StructuresConfigAccessor) structureSettings).setConfiguredStructures(immutableStructureMap.build());
            }
        });
        // TEMP DELETE WHEN BIOME MODICATIONS IS FIXED.

        // Disable vanilla villages if chosen in config.
        if (CONFIG.disableVanillaVillages)
            removeVanillaVillages();

        ServerLifecycleEvents.SERVER_STARTING.register((MinecraftServer server) -> handleModSupport(server.getRegistryManager()));
    }

    /*
     *	Adapting TelepathicGrunt's removeStructureSpawningFromSelectedDimension function for removing vanilla villages instead.
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
        Registry<StructurePool> structurePoolRegistry = registry.get(Registry.STRUCTURE_POOL_KEY);

        // TODO: This should really be defined in JSON or something.

        // MoreVillagers mod.
        if(FabricLoader.getInstance().isModLoaded("morevillagers-fabric"))
        {
            StructurePool point_of_interest = structurePoolRegistry.get(new Identifier(MODID, "point_of_interest"));
            StructurePool morevillagers_point_of_interest = structurePoolRegistry.get(new Identifier(MODID, "addons/morevillagers/morevillagers_point_of_interest"));

            StructurePoolUtils.appendPool(point_of_interest, morevillagers_point_of_interest);

            StructurePool abandoned_point_of_interest = structurePoolRegistry.get(new Identifier(MODID, "abandoned_point_of_interest"));
            StructurePool morevillagers_abandoned_point_of_interest = structurePoolRegistry.get(new Identifier(MODID, "addons/morevillagers/morevillagers_abandoned_point_of_interest"));

            StructurePoolUtils.appendPool(abandoned_point_of_interest, morevillagers_abandoned_point_of_interest);
        }
    }
}
