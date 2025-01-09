package thecodex6824.thaumicaugmentation.api.augment.builder.impulsecannon;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;

public interface IImpulseCannonRaytraceOverridingAugment extends IImpulseCannonAugment {

    @Override
    default boolean isCompatible(ItemStack otherAugment) {
        IAugment a = otherAugment.getCapability(CapabilityAugment.AUGMENT, null);
        return !(a instanceof IImpulseCannonRaytraceOverridingAugment);
    }

    @NotNull Vec3d overrideFiringRayTrace(World world, Vec3d sourcePosition, Vec3d originalRayTrace);
}
