package thecodex6824.thaumicaugmentation.api.augment.impl.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;

public interface IBuilderCannonEffectProvider extends IBuilderEffectProvider {

    @Override
    default boolean compatibleWith(IBuilderStrengthProvider strengthProvider) {
        return strengthProvider instanceof IBuilderCannonStrengthProvider;
    }

    default double getImpulseCostModifier(ICustomAugment augment, ItemStack cannonStack, EntityLivingBase user, IImpetusStorage buffer, double strength) {
        return 1;
    }

    default float getBaseDamageModifier(ICustomAugment augment, ItemStack cannonStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed, double strength) {
        return 1;
    }

    default float getMagicDamageModifier(ICustomAugment augment, ItemStack cannonStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed, double strength) {
        return 1;
    }

    default float getNormalDamageModifier(ICustomAugment augment, ItemStack cannonStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed, double strength) {
        return 1;
    }

    /**
     * Should always be called before impetus damage is dealt. Reset the hurt time of the entity if this deals damage!
     */
    default void applyAdditionalEffectsToEntity(ICustomAugment augment, ItemStack cannonStack, EntityLivingBase user, Vec3d firingOrigin, Vec3d firingEnd, Entity entityHit, float baseDamage, double strength) {}

    /**
     * Should always be called after entity processing is finished, for every beam the cannon emits.
     */
    default void applyAdditionalEffects(ICustomAugment augment, ItemStack cannonStack, EntityLivingBase user, Vec3d firingOrigin, Vec3d firingEnd, float baseDamage, double strength) {}

}
