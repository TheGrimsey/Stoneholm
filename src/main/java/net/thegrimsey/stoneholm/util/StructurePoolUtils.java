package net.thegrimsey.stoneholm.util;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.thegrimsey.stoneholm.mixin.StructurePoolAccessor;

import java.util.ArrayList;

public class StructurePoolUtils {
    public static void appendPool(StructurePool primaryPool, StructurePool secondaryPool)
    {
        StructurePoolAccessor primaryPoolAccessor = (StructurePoolAccessor) primaryPool;
        StructurePoolAccessor secondaryPoolAccessor = (StructurePoolAccessor) secondaryPool;

        ArrayList<Pair<StructurePoolElement, Integer>> elementCounts = new ArrayList<>(primaryPoolAccessor.getElementCounts());
        ObjectArrayList<StructurePoolElement> elements = new ObjectArrayList<>(primaryPoolAccessor.getElements());

        elementCounts.addAll(secondaryPoolAccessor.getElementCounts());
        elements.addAll(secondaryPoolAccessor.getElements());

        primaryPoolAccessor.setElements(elements);
        primaryPoolAccessor.setElementCounts(elementCounts);
    }
}
