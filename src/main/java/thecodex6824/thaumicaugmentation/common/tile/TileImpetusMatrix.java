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

import java.util.Deque;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.animation.TimeValues.VariableValue;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.common.util.INBTSerializable;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.IImpetusCellInfo;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.ConsumeResult;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.BufferedImpetusProsumer;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.tile.trait.IAnimatedTile;
import thecodex6824.thaumicaugmentation.common.tile.trait.IBreakCallback;

public class TileImpetusMatrix extends TileEntity implements ITickable, IAnimatedTile, IBreakCallback {

    protected static final long CELL_CAPACITY = 500;
    
    protected class MatrixImpetusStorage implements IImpetusStorage, INBTSerializable<NBTTagCompound> {
        
        protected long energy;
        
        @Override
        public boolean canExtract() {
            return true;
        }
        
        @Override
        public boolean canReceive() {
            return true;
        }
        
        @Override
        public long getEnergyStored() {
            return energy;
        }
        
        @Override
        public long getMaxEnergyStored() {
            return getTotalCells() * CELL_CAPACITY;
        }
        
        @Override
        public long extractEnergy(long maxEnergy, boolean simulate) {
            if (canExtract()) {
                long amount = Math.min(energy, Math.min(getTotalCells() * CELL_CAPACITY, maxEnergy));
                if (!simulate) {
                    energy -= amount;
                    onEnergyChanged();
                }
                
                return amount;
            }
            
            return 0;
        }
        
        @Override
        public long receiveEnergy(long maxEnergy, boolean simulate) {
            if (canReceive()) {
                long amount = Math.min(Math.min(getTotalCells() * CELL_CAPACITY, maxEnergy), getMaxEnergyStored() - energy);
                if (!simulate) {
                    energy += amount;
                    onEnergyChanged();
                }
                
                return amount;
            }
            
            return 0;
        }
        
        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("energy", energy);
            return tag;
        }
        
        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            energy = nbt.getLong("energy");
        }
        
        @Override
        public void onEnergyChanged() {
            markDirty();
        }
        
    }
    
    protected MatrixImpetusStorage buffer;
    protected BufferedImpetusProsumer prosumer;
    protected IAnimationStateMachine asm;
    protected int delay = ThreadLocalRandom.current().nextInt(-5, 6);
    protected int ticks;
    
    public TileImpetusMatrix() {
        buffer = new MatrixImpetusStorage();
        prosumer = new BufferedImpetusProsumer(1, 1, buffer) {
            @Override
            public void onTransaction(IImpetusConsumer originator, Deque<IImpetusNode> path, long energy, boolean simulate) {
                markDirty();
            }
        };
        asm = ThaumicAugmentation.proxy.loadASM(new ResourceLocation(ThaumicAugmentationAPI.MODID, "asms/block/impetus_matrix.json"), 
                ImmutableMap.<String, ITimeValue>of("cycle_length", new VariableValue(20), "delay", new VariableValue(delay)));
    }
    
    @Override
    public void update() {
        if (!world.isRemote && (ticks++ + delay) % 20 == 0) {
            ConsumeResult result = prosumer.consume(getTotalCells() * CELL_CAPACITY, false);
            if (result.energyConsumed > 0)
                NodeHelper.syncAllImpetusTransactions(result.paths);
        }
    }
    
    public int getTotalCells() {
        int total = 0;
        IBlockState state = world.getBlockState(pos.up());
        if (state.getPropertyKeys().contains(IImpetusCellInfo.CELL_INFO))
            total += IImpetusCellInfo.getNumberOfCells(state.getValue(IImpetusCellInfo.CELL_INFO));
        
        state = world.getBlockState(pos.down());
        if (state.getPropertyKeys().contains(IImpetusCellInfo.CELL_INFO))
            total += IImpetusCellInfo.getNumberOfCells(state.getValue(IImpetusCellInfo.CELL_INFO));
        
        return total;
    }
    
    @Override
    public void setPos(BlockPos posIn) {
        super.setPos(posIn);
        prosumer.setLocation(new DimensionalBlockPos(pos, world.provider.getDimension()));
    }
    
    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        prosumer.setLocation(new DimensionalBlockPos(pos, world.provider.getDimension()));
    }
    
    @Override
    public void onLoad() {
        prosumer.init(world);
        ThaumicAugmentation.proxy.registerRenderableImpetusNode(prosumer);
    }
    
    @Override
    public void invalidate() {
        prosumer.unload();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(prosumer);
    }
    
    @Override
    public void onBlockBroken() {
        for (IImpetusNode input : prosumer.getInputs())
            NodeHelper.syncRemovedImpetusNodeOutput(input, prosumer.getLocation());
        
        for (IImpetusNode output : prosumer.getOutputs())
            NodeHelper.syncRemovedImpetusNodeInput(output, prosumer.getLocation());
        
        prosumer.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(prosumer);
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("node", prosumer.serializeNBT());
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        prosumer.init(world);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("node", prosumer.serializeNBT());
        tag.setTag("energy", buffer.serializeNBT());
        return super.writeToNBT(tag);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        buffer.deserializeNBT(nbt.getCompoundTag("energy"));
        prosumer.deserializeNBT(nbt.getCompoundTag("node"));
    }
    
    @Override
    public void handleEvents(float time, Iterable<Event> pastEvents) {}
    
    @Override
    public boolean hasFastRenderer() {
        return true;
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE || capability == CapabilityAnimation.ANIMATION_CAPABILITY)
            return true;
        else
            return super.hasCapability(capability, facing);
    }
    
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE)
            return CapabilityImpetusNode.IMPETUS_NODE.cast(prosumer);
        else if (capability == CapabilityAnimation.ANIMATION_CAPABILITY)
            return CapabilityAnimation.ANIMATION_CAPABILITY.cast(asm);
        else
            return super.getCapability(capability, facing);
    }
    
    
}
