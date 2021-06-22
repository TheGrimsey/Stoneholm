package net.thegrimsey.stoneholm.util;

import net.minecraft.structure.pool.StructurePool;
import net.thegrimsey.stoneholm.mixin.StructurePoolAccessor;

import java.util.ArrayList;

public class StructurePoolUtils {
    public static void appendPool(StructurePool primaryPool, StructurePool secondaryPool)
    {
        var primaryPoolAccessor = (StructurePoolAccessor) primaryPool;
        var secondaryPoolAccessor = (StructurePoolAccessor) secondaryPool;

        var elementCounts = new ArrayList<>(primaryPoolAccessor.getElementCounts());
        var elements = new ArrayList<>(primaryPoolAccessor.getElements());

        elementCounts.addAll(secondaryPoolAccessor.getElementCounts());
        elements.addAll(secondaryPoolAccessor.getElements());

        primaryPoolAccessor.setElements(elements);
        primaryPoolAccessor.setElementCounts(elementCounts);
    }
}
