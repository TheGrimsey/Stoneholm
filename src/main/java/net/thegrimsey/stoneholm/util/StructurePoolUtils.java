package net.thegrimsey.stoneholm.util;

import net.minecraft.structure.pool.StructurePool;
import net.thegrimsey.stoneholm.mixin.StructurePoolAccessor;

import java.util.ArrayList;

public class StructurePoolUtils {
    public static void appendPool(StructurePool primaryPool, StructurePool secondaryPool)
    {
        StructurePoolAccessor primaryPoolAccessor = (StructurePoolAccessor) primaryPool;
        StructurePoolAccessor secondaryPoolAccessor = (StructurePoolAccessor) secondaryPool;

        ArrayList elementCounts = new ArrayList<>(primaryPoolAccessor.getElementCounts());
        ArrayList elements = new ArrayList<>(primaryPoolAccessor.getElements());

        elementCounts.addAll(secondaryPoolAccessor.getElementCounts());
        elements.addAll(secondaryPoolAccessor.getElements());

        primaryPoolAccessor.setElements(elements);
        primaryPoolAccessor.setElementCounts(elementCounts);
    }
}
