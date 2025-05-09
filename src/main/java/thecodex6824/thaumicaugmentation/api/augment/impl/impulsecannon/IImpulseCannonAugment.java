/**
 *  Thaumic Augmentation
 *  Copyright (c) 2019 TheCodex6824.
 *
 *  This file is part of Thaumic Augmentation.
 *
 *  Thaumic Augmentation is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Thaumic Augmentation is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Thaumic Augmentation.  If not, see <https://www.gnu.org/licenses/>.
 */

package thecodex6824.thaumicaugmentation.api.augment.impl.impulsecannon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.common.item.ItemImpulseCannon;

public interface IImpulseCannonAugment extends IAugment {

    @Override
    default boolean canBeAppliedToItem(ItemStack augmentable) {
        return augmentable.getItem() instanceof ItemImpulseCannon;
    }

    default double getImpulseCostModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer) {
        return 1;
    }

    default float getBaseDamageModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
        return 1;
    }

    default float getMagicDamageModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
        return 1;
    }

    default float getNormalDamageModifier(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, IImpetusStorage buffer, double normalImpetusConsumed, double actualImpetusConsumed) {
        return 1;
    }

    /**
     * Should always be called before impetus damage is dealt. Reset the hurt time of the entity if this deals damage!
     */
    default void applyAdditionalEffectsToEntity(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, Vec3d firingOrigin, Vec3d firingEnd, Entity entityHit, float baseDamage) {}

    /**
     * Should always be called after entity processing is finished, for every beam the cannon emits.
     */
    default void applyAdditionalEffects(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, Vec3d firingOrigin, Vec3d firingEnd, float baseDamage) {}

    /**
     * Returns if the cannon this augment is installed in should enable its 'spare' slot.
     * Does not stack if multiple installed augments return true.
     */
    default boolean givesExtraSlot() {
        return false;
    }
}
