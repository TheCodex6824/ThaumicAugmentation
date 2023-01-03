/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.api.augment.builder.caster;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import thecodex6824.thaumicaugmentation.api.util.DamageWrapper;

import java.util.List;

public interface IBuilderCasterCallback {
    
    default void onEquip(ICustomCasterAugment augment, Entity user) {}
    
    default void onUnequip(ICustomCasterAugment augment, Entity user) {}
    
    default void onTick(ICustomCasterAugment augment, Entity user) {}
    
    default void onHurtEntity(ICustomCasterAugment augment, DamageSource source, Entity attacked, DamageWrapper damage) {}
    
    default void onDamagedEntity(ICustomCasterAugment augment, DamageSource source, Entity attacked, DamageWrapper damage) {}
    
    default void onHurt(ICustomCasterAugment augment, Entity attacked, DamageSource source, DamageWrapper damage) {}
    
    default void onDamaged(ICustomCasterAugment augment, Entity attacked, DamageSource source, DamageWrapper damage) {}
    
    default void appendAdditionalTooltip(ItemStack component, List<String> tooltip) {}
    
}
