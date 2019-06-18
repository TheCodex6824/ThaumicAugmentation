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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;
import thaumcraft.api.casters.FocusPackage;

public interface IAugment extends INBTSerializable<NBTTagCompound> {

    public default void onEquip(Entity user) {}
    
    public default void onUnequip(Entity user) {}
    
    public default void onTick(Entity user) {}
    
    public default void onCast(ItemStack caster, FocusPackage focusPackage, Entity user) {}
    
    public default void onHurtEntity(Entity user, Entity attacked) {}
    
    public default void onDamagedEntity(Entity user, Entity attacked) {}
    
    public default void onHurt(Entity user, @Nullable Entity attacker) {}
    
    public default void onDamaged(Entity user, @Nullable Entity attacker) {}
    
    public default void onInteractEntity(Entity user, ItemStack used, Entity target, EnumHand hand) {}
    
    public default void onInteractBlock(Entity user, ItemStack used, BlockPos target, EnumFacing face, EnumHand hand) {}  
    
    public default void onInteractAir(Entity user, ItemStack used, EnumHand hand) {}
    
    public default void onUseItem(Entity user, ItemStack used) {}
    
    public default boolean isCompatible(ItemStack otherAugment) {
        return true;
    }
    
    public default boolean canBeAppliedToItem(ItemStack augmentable) {
        return true;
    }
    
    public default boolean hasAdditionalAugmentTooltip() {
        return false;
    }
    
    public default void appendAdditionalAugmentTooltip(List<String> tooltip) {}
    
    public default boolean shouldSync() {
        return false;
    }
    
}
