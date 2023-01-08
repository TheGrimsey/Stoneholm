package net.thegrimsey.stoneholm;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.thegrimsey.stoneholm.structures.CandleProcessor;
import net.thegrimsey.stoneholm.structures.NoWaterProcessor;
import net.thegrimsey.stoneholm.util.StructurePoolUtils;

public class Stoneholm implements ModInitializer {
    public static final String MODID = "stoneholm";
    public static final Identifier UNDERGROUNDVILLAGE_IDENTIFIER = new Identifier(Stoneholm.MODID, "underground_village");
    public static SHConfig CONFIG;
    public static final StructureProcessorType<NoWaterProcessor> NOWATER_PROCESSOR = () -> NoWaterProcessor.CODEC;
    public static final StructureProcessorType<CandleProcessor> CANDLE_PROCESSOR = () -> CandleProcessor.CODEC;

    // Suppress deprecation warnings from Fabric's Biome API.
    @Override
    public void onInitialize() {
        // Register config file.
        AutoConfig.register(SHConfig.class, JanksonConfigSerializer::new);
        // Get config.
        CONFIG = AutoConfig.getConfigHolder(SHConfig.class).getConfig();

        // Register structures & configured structures.
        SHStructures.registerStructureFeatures();
        Registry.register(Registry.STRUCTURE_PROCESSOR, new Identifier(MODID, "nowater_processor"), NOWATER_PROCESSOR);
        Registry.register(Registry.STRUCTURE_PROCESSOR, new Identifier(MODID, "candle_processor"), CANDLE_PROCESSOR);

        ServerLifecycleEvents.SERVER_STARTING.register((MinecraftServer server) -> handleModSupport(server.getRegistryManager()));
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
