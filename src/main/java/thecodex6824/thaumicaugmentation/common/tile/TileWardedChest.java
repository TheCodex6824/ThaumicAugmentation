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

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.animation.TimeValues.VariableValue;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.warded.CapabilityWardedInventory;
import thecodex6824.thaumicaugmentation.api.warded.WardedInventory;
import thecodex6824.thaumicaugmentation.common.tile.trait.IAnimatedTile;

public class TileWardedChest extends TileWarded implements IAnimatedTile {

    protected WardedInventory inventory;
    protected IAnimationStateMachine asm;
    protected VariableValue openTime;

    public TileWardedChest() {
        super();
        inventory = new WardedInventory(27);
        openTime = new VariableValue(-1);
        asm = ThaumicAugmentation.proxy.loadASM(new ResourceLocation(ThaumicAugmentationAPI.MODID, "asms/block/warded_chest.json"), 
                ImmutableMap.<String, ITimeValue>of("open_speed", new VariableValue(0.5F), "open_time", openTime));
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public void onOpenInventory() {
        if (!world.isRemote)
            world.playSound(null, pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, 1.0F);
        else {
            openTime.setValue(Animation.getWorldTime(world, Animation.getPartialTickTime()));
            asm.transition("opening");
        }
    }

    public void onCloseInventory() {
        if (!world.isRemote)
            world.playSound(null, pos, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, 1.0F);
        else {
            openTime.setValue(Animation.getWorldTime(world, Animation.getPartialTickTime()));
            asm.transition("closing");
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("inventory", inventory.serializeNBT());
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        inventory.deserializeNBT(compound.getCompoundTag("inventory"));
        super.readFromNBT(compound);
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public void handleEvents(float time, Iterable<Event> pastEvents) {
        // nope
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityAnimation.ANIMATION_CAPABILITY || capability == CapabilityWardedInventory.WARDED_INVENTORY)
            return true;
        else
            return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityAnimation.ANIMATION_CAPABILITY)
            return CapabilityAnimation.ANIMATION_CAPABILITY.cast(asm);
        else if (capability == CapabilityWardedInventory.WARDED_INVENTORY)
            return CapabilityWardedInventory.WARDED_INVENTORY.cast(inventory);
        else
            return super.getCapability(capability, facing);
    }

}
