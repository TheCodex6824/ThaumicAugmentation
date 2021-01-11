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

package thecodex6824.thaumicaugmentation.common.tile;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;

public class TileItemGrate extends TileEntity implements ITickable {

    protected int ticks;
    protected ItemStackHandler inventory;
    
    public TileItemGrate() {
        ticks = ThreadLocalRandom.current().nextInt(20);
        inventory = new ItemStackHandler(1) {
            @Override
            @Nonnull
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (world.getBlockState(pos).getValue(IEnabledBlock.ENABLED))
                    return super.insertItem(slot, stack, simulate);
                else
                    return stack;
            }
        };
    }
    
    @Override
    public void update() {
        if (!world.isRemote && ticks++ % 5 == 0) {
            ItemStack extracted = inventory.extractItem(0, 64, false);
            if (!extracted.isEmpty()) {
                Entity item = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.75, pos.getZ() + 0.5, extracted);
                Entity newItem = extracted.getItem().createEntity(world, item, extracted);
                item = newItem != null ? newItem : item;
                item.setPosition(item.posX, item.posY - item.height, item.posZ);
                item.motionX = 0.0;
                item.motionZ = 0.0;
                item.velocityChanged = true;
                world.spawnEntity(item);
            }
            
            if (world.getBlockState(pos).getValue(IEnabledBlock.ENABLED)) {
                List<EntityItem> above = world.getEntitiesWithinAABB(EntityItem.class,
                        new AxisAlignedBB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 1.125, pos.getZ() + 1));
                for (EntityItem e : above) {
                    e.motionX = 0.0;
                    e.motionZ = 0.0;
                    e.velocityChanged = true;
                    e.setPosition(e.posX, pos.getY() + 0.75 - e.height, e.posZ);
                }
            }
        }
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
                super.hasCapability(capability, facing);
    }
    
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        else
            return super.getCapability(capability, facing);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("inventory", inventory.serializeNBT());
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        inventory.deserializeNBT(compound.getCompoundTag("inventory"));
    }
    
}
