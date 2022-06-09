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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.animation.TimeValues.VariableValue;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.common.util.Constants.NBT;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.tile.INameableTile;
import thecodex6824.thaumicaugmentation.api.ward.tile.CapabilityWardedInventory;
import thecodex6824.thaumicaugmentation.api.ward.tile.WardedInventory;
import thecodex6824.thaumicaugmentation.common.tile.trait.IAnimatedTile;
import thecodex6824.thaumicaugmentation.common.util.AnimationHelper;

public class TileWardedChest extends TileWarded implements IAnimatedTile, INameableTile {

    protected static final float ANIM_TIME = 0.5F;
    
    protected WardedInventory inventory;
    protected IAnimationStateMachine asm;
    protected VariableValue openTime;
    protected String name;

    public TileWardedChest() {
        super();
        inventory = new WardedInventory(27);
        openTime = new VariableValue(-1);
        asm = ThaumicAugmentation.proxy.loadASM(new ResourceLocation(ThaumicAugmentationAPI.MODID, "asms/block/warded_chest.json"), 
                ImmutableMap.<String, ITimeValue>of("anim_time", new VariableValue(ANIM_TIME), "open_time", openTime));
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public void onOpenInventory() {
        if (!world.isRemote) {
            world.playSound(null, pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, 1.0F);
            world.addBlockEvent(pos, getBlockType(), 1, 1);
        }
    }

    public void onCloseInventory() {
        if (!world.isRemote) {
            world.playSound(null, pos, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, 1.0F);
            world.addBlockEvent(pos, getBlockType(), 1, 0);
        }
    }
    
    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            float time = Animation.getWorldTime(world, Animation.getPartialTickTime());
            float partialProgress = openTime.apply(time) < 0.0F ? 0.0F :
                MathHelper.clamp(ANIM_TIME - (time - openTime.apply(time)), 0.0F, ANIM_TIME);
            openTime.setValue(time - partialProgress);
            AnimationHelper.transitionSafely(asm, type == 1 ? "opening" : "closing");
            return true;
        }
        
        return false;
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("inventory", inventory.serializeNBT());
        if (name != null)
            compound.setString("CustomName", name);
        
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        inventory.deserializeNBT(compound.getCompoundTag("inventory"));
        if (compound.hasKey("CustomName", NBT.TAG_STRING))
            name = compound.getString("CustomName");
        
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
    public boolean hasCustomName() {
        return name != null;
    }
    
    @Override
    @Nullable
    public String getCustomName() {
        return name;
    }
    
    @Override
    public void setCustomName(String name) {
        this.name = name;
        markDirty();
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
