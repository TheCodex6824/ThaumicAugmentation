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

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import thaumcraft.api.casters.FocusPackage;

public interface IAugment {

    public default void onEquip(ItemStack stack, Entity user) {}
    
    public default void onUnequip(ItemStack stack, Entity user) {}
    
    public default void onTick(ItemStack stack, Entity user) {}
    
    public default void onCast(ItemStack stack, ItemStack caster, FocusPackage focusPackage, Entity user) {}
    
    public default void onHurtEntity(ItemStack stack, Entity user, Entity attacked) {}
    
    public default void onDamagedEntity(ItemStack stack, Entity user, Entity attacked) {}
    
    public default void onHurt(ItemStack stack, Entity user, @Nullable Entity attacker) {}
    
    public default void onDamaged(ItemStack stack, Entity user, @Nullable Entity attacker) {}
    
    public default void onInteractEntity(ItemStack stack, Entity user, ItemStack used, Entity target, EnumHand hand) {}
    
    public default void onInteractBlock(ItemStack stack, Entity user, ItemStack used, BlockPos target, EnumFacing face, EnumHand hand) {}  
    
    public default void onInteractAir(ItemStack stack, Entity user, ItemStack used, EnumHand hand) {}
    
    public default void onUseItem(ItemStack stack, Entity user, ItemStack used) {}
    
    public default boolean isCompatible(ItemStack stack, ItemStack otherAugment) {
        return true;
    }
    
    public default boolean canBeAppliedToItem(ItemStack stack, ItemStack augmentable) {
        return true;
    }
    
    public default boolean hasAdditionalAugmentTooltip(ItemStack stack) {
        return false;
    }
    
    public default void appendAdditionalAugmentTooltip(ItemStack stack, List<String> tooltip) {}
    
    public default boolean shouldSync(ItemStack stack) {
        return false;
    }
    
    public default int getSyncInterval(ItemStack stack) {
        return 20;
    }
    
}
