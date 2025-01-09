package thecodex6824.thaumicaugmentation.api.augment.builder.impulsecannon;

import net.minecraft.entity.EntityLivingBase;
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

    default @NotNull Vec3d overrideFiringRayTrace(EntityLivingBase user, Vec3d sourcePosition, Vec3d originalRayTrace) {
        return overrideFiringRayTrace(user, sourcePosition, originalRayTrace, 1);
    }


    @NotNull Vec3d overrideFiringRayTrace(EntityLivingBase user, Vec3d sourcePosition, Vec3d originalRayTrace, float partialTicks);
}
