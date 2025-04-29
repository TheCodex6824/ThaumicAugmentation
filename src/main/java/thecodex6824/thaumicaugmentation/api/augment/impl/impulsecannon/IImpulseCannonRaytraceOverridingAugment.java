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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;

public interface IImpulseCannonRaytraceOverridingAugment extends IImpulseCannonAugment {

    @Override
    default boolean isCompatible(ItemStack otherAugment, IAugment otherAugmentCap) {
        return !(otherAugmentCap instanceof IImpulseCannonRaytraceOverridingAugment);
    }

    default @NotNull Vec3d overrideFiringRayTrace(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, Vec3d sourcePosition, Vec3d originalRayTrace) {
        return overrideFiringRayTrace(cannonStack, augmentStack, user, sourcePosition, originalRayTrace, 1);
    }


    @NotNull Vec3d overrideFiringRayTrace(ItemStack cannonStack, ItemStack augmentStack, EntityLivingBase user, Vec3d sourcePosition, Vec3d originalRayTrace, float partialTicks);
}
