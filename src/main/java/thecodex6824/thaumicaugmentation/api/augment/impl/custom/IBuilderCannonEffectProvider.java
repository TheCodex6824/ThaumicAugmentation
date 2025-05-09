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
