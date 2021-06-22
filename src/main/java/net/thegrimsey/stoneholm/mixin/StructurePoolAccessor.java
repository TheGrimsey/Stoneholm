package net.thegrimsey.stoneholm.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StructurePool.class)
public interface StructurePoolAccessor {
    @Accessor("elementCounts")
    @Mutable
    void setElementCounts(List<Pair<StructurePoolElement, Integer>> elementCounts);


    @Accessor("elementCounts")
    List<Pair<StructurePoolElement, Integer>> getElementCounts();

    @Accessor("elements")
    @Mutable
    void setElements(List<StructurePoolElement> elements);

    @Accessor("elements")
    List<StructurePoolElement> getElements();
}
