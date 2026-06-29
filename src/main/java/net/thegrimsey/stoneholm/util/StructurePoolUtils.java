package net.thegrimsey.stoneholm.util;

//? if <1.21.4 {
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
//?}

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.thegrimsey.stoneholm.mixin.StructurePoolAccessor;

public class StructurePoolUtils {
    public static void appendPool(StructurePool primaryPool, StructurePool secondaryPool)
    {
        StructurePoolAccessor primaryPoolAccessor = (StructurePoolAccessor) primaryPool;
        StructurePoolAccessor secondaryPoolAccessor = (StructurePoolAccessor) secondaryPool;

        //? if <1.21.4 {
        ArrayList<Pair<StructurePoolElement, Integer>> elementCounts = new ArrayList<>(primaryPoolAccessor.getElementCounts());
        elementCounts.addAll(secondaryPoolAccessor.getElementCounts());
        primaryPoolAccessor.setElementCounts(elementCounts);
        //?}

        ObjectArrayList<StructurePoolElement> elements = new ObjectArrayList<>(primaryPoolAccessor.getElements());
        elements.addAll(secondaryPoolAccessor.getElements());
        primaryPoolAccessor.setElements(elements);
    }
}
