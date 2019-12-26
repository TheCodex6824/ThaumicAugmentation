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

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import thaumcraft.api.aspects.Aspect;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.ConsumeResult;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.SimpleImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.tile.trait.IBreakCallback;

public class TileImpetusGenerator extends TileEntity implements IBreakCallback, ITickable {

    protected static class CustomEnergyStorage extends EnergyStorage {
        
        public CustomEnergyStorage(int capacity, int in, int out, int initial) {
            super(capacity, in, out, initial);
        }
        
        public void setEnergy(int newEnergy) {
            energy = newEnergy;
        }
        
    }
    
    protected SimpleImpetusConsumer node;
    protected CustomEnergyStorage forgeEnergy;
    protected int ticks;
    
    public TileImpetusGenerator() {
        super();
        node = new SimpleImpetusConsumer(1, 0);
        forgeEnergy = new CustomEnergyStorage(3000, 1500, 30, 0);
    }
    
    @Override
    public void update() {
        if (!world.isRemote && ticks++ % 10 == 0) {
            IBlockState state = world.getBlockState(pos);
            if (state.getValue(IEnabledBlock.ENABLED)) {
                if (forgeEnergy.getEnergyStored() < forgeEnergy.getMaxEnergyStored() - 1500) {
                    ConsumeResult result = node.consume(1, false);
                    if (result.energyConsumed == 1) {
                        forgeEnergy.receiveEnergy(1500, false);
                        markDirty();
                        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 6);
                        NodeHelper.syncAllImpetusTransactions(result.paths);
                    }
                }
            }
        }
        else if (world.isRemote && ticks++ % 10 == 0 && world.getBlockState(pos).getValue(IEnabledBlock.ENABLED)) {
            ThaumicAugmentation.proxy.getRenderHelper().renderSpark(world, pos.getX() + 0.5 + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.25,
                    pos.getY() + 0.9  + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.25,
                    pos.getZ() + 0.5  + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.25, 1.5F, Aspect.ELDRITCH.getColor(), false);
        }
        
        if (!world.isRemote) {
            EnumFacing facing = world.getBlockState(pos).getValue(IDirectionalBlock.DIRECTION);
            TileEntity neighbor = world.getTileEntity(pos.offset(facing));
            if (neighbor != null) {
                IEnergyStorage other = neighbor.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite());
                if (other != null && other.canReceive()) {
                    int extract = Math.min(forgeEnergy.getEnergyStored(), 30);
                    extract = other.receiveEnergy(extract, false);
                    if (extract > 0) {
                        forgeEnergy.extractEnergy(extract, false);
                        markDirty();
                        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 6);
                    }
                }
            }
        }
    }
    
    @Override
    public void setPos(BlockPos posIn) {
        super.setPos(posIn);
        node.setLocation(new DimensionalBlockPos(pos, world.provider.getDimension()));
    }
    
    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        node.setLocation(new DimensionalBlockPos(pos, world.provider.getDimension()));
    }
    
    @Override
    public void onLoad() {
        node.init(world);
        ThaumicAugmentation.proxy.registerRenderableImpetusNode(node);
    }
    
    @Override
    public void invalidate() {
        node.unload();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(node);
    }
    
    @Override
    public void onBlockBroken() {
        for (IImpetusNode input : node.getInputs())
            NodeHelper.syncRemovedImpetusNodeOutput(input, node.getLocation());
        
        for (IImpetusNode output : node.getOutputs())
            NodeHelper.syncRemovedImpetusNodeInput(output, node.getLocation());
        
        node.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(node);
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("node", node.serializeNBT());
        tag.setInteger("energy", forgeEnergy.getEnergyStored());
        return super.writeToNBT(tag);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        node.deserializeNBT(nbt.getCompoundTag("node"));
        forgeEnergy.setEnergy(nbt.getInteger("energy"));
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("node", node.serializeNBT());
        tag.setInteger("energy", forgeEnergy.getEnergyStored());
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        node.init(world);
        forgeEnergy.setEnergy(tag.getInteger("energy"));
    }
    
    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("energy", forgeEnergy.getEnergyStored());
        return new SPacketUpdateTileEntity(pos, 1, compound);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        forgeEnergy.setEnergy(pkt.getNbtCompound().getInteger("energy"));
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE)
            return true;
        else if (capability == CapabilityEnergy.ENERGY && facing == world.getBlockState(pos).getValue(IDirectionalBlock.DIRECTION))
            return true;
        else
            return super.hasCapability(capability, facing);
    }
    
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE)
            return CapabilityImpetusNode.IMPETUS_NODE.cast(node);
        else if (capability == CapabilityEnergy.ENERGY && facing == world.getBlockState(pos).getValue(IDirectionalBlock.DIRECTION))
            return CapabilityEnergy.ENERGY.cast(forgeEnergy);
        else
            return super.getCapability(capability, facing);
    }
    
}
