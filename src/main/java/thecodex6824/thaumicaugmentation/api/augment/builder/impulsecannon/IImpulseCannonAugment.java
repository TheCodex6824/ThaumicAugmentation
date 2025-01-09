package thecodex6824.thaumicaugmentation.api.augment.builder.impulsecannon;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;

public interface IImpulseCannonAugment extends IAugment {

    @Override
    default boolean canBeAppliedToItem(ItemStack augmentable) {
        return augmentable.getItem() == TAItems.IMPULSE_CANNON;
    }

    default double getImpulseCostModifier(IImpetusStorage buffer) {
        return 1;
    }

    default float getBaseDamageModifier(IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
        return 1;
    }

    default float getMagicDamageModifier(IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
        return 1;
    }

    default float getNormalDamageModifier(IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
        return 1;
    }

    /**
     * Should always be called before impetus damage is dealt. Reset the hurt time of the entity if this deals damage!
     */
    default void applyAdditionalEffectsToEntity(Vec3d firingOrigin, Vec3d firingEnd, Entity entityHit, float baseDamage) {}

    /**
     * Should always be called after entity processing is finished.
     */
    default void applyAdditionalEffects(Vec3d firingOrigin, Vec3d firingEnd) {}
}
