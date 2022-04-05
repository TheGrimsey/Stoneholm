package net.thegrimsey.stoneholm.mixin;
/*
*   Taken from https://github.com/TelepathicGrunt/StructureTutorialMod/blob/1.18.2-Fabric-Jigsaw/src/main/java/com/telepathicgrunt/structure_tutorial/mixin/StructureFeatureAccessor.java
 */
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructureFeature.class)
public interface StructureFeatureAccessor {
    @Invoker
    static <F extends StructureFeature<?>> F callRegister(String name, F structureFeature, GenerationStep.Feature step) {
        throw new UnsupportedOperationException();
    }
}
