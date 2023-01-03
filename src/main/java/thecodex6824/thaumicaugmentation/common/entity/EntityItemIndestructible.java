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

package thecodex6824.thaumicaugmentation.common.entity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityItemIndestructible extends EntityItem {
    
    public EntityItemIndestructible(World world) {
        super(world);
    }
    
    public EntityItemIndestructible(World world, double x, double y, double z) {
        super(world, x, y, z);
    }
    
    public EntityItemIndestructible(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.canHarmInCreative())
            return super.attackEntityFrom(source, amount);
        else
            return false;
    }
    
}
