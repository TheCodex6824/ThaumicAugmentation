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

package thecodex6824.thaumicaugmentation.api.augment;

import java.util.Collections;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import thaumcraft.api.casters.FocusPackage;

public interface IAugment {

    public default void onEquip(ItemStack stack, Entity user) {}
    
    public default void onUnequip(ItemStack stack, Entity user) {}
    
    public default void onUserTick(ItemStack stack, Entity user) {}
    
    public default void onUserCast(ItemStack stack, ItemStack caster, FocusPackage focusPackage, Entity user) {}
    
    public default void onUserHurtEntity(ItemStack stack, Entity user, Entity attacked) {}
    
    public default void onUserDamageEntity(ItemStack stack, Entity user, Entity attacked) {}
    
    public default void onUserHurt(ItemStack stack, Entity user, @Nullable Entity attacker) {}
    
    public default void onUserDamaged(ItemStack stack, Entity user, @Nullable Entity attacker) {}
    
    public default boolean isCompatible(ItemStack stack, ItemStack otherAugment) {
        return true;
    }
    
    public default boolean canBeAppliedToItem(ItemStack stack, ItemStack augmentable) {
        return true;
    }
    
    public default boolean hasAdditionalAugmentTooltip(ItemStack stack) {
        return false;
    }
    
    public default Iterable<String> getAdditionalAugmentTooltip(ItemStack stack) {
        return Collections.emptyList();
    }
    
}
