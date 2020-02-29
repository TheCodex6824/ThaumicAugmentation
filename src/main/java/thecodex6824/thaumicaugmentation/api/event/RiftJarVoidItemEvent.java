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

package thecodex6824.thaumicaugmentation.api.event;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import thecodex6824.thaumicaugmentation.api.tile.IRiftJar;

@Cancelable
public class RiftJarVoidItemEvent extends Event {
    
    protected final ItemStack stack;
    protected final IRiftJar jar;
    protected final World world;
    protected final BlockPos pos;
    
    public RiftJarVoidItemEvent(ItemStack toVoid, IRiftJar jarCap, World jarWorld, BlockPos jarPos) {
        stack = toVoid;
        jar = jarCap;
        world = jarWorld;
        pos = jarPos;
    }
    
    public ItemStack getItemStack() {
        return stack;
    }
    
    public IRiftJar getRiftJar() {
        return jar;
    }
    
    public World getWorld() {
        return world;
    }
    
    public BlockPos getPosition() {
        return pos;
    }
    
}
