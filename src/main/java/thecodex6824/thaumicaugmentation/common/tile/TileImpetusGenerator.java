/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import thaumcraft.api.aspects.Aspect;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.ConsumeResult;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.SimpleImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

import javax.annotation.Nullable;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class TileImpetusGenerator extends TileEntity implements ITickable {

    protected class CustomEnergyStorage extends EnergyStorage {
        
        public CustomEnergyStorage(int capacity, int in, int out, int initial) {
            super(capacity, in, out, initial);
        }
        
        public void setEnergy(int newEnergy) {
            energy = MathHelper.clamp(newEnergy, 0, capacity);
            markDirty();
        }
        
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0)
                markDirty();
            
            return extracted;
        }
        
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0)
                markDirty();
            
            return received;
        }
        
    }
    
    protected SimpleImpetusConsumer node;
    protected CustomEnergyStorage forgeEnergy;
    protected int ticks;
    
    public TileImpetusGenerator() {
        super();
        node = new SimpleImpetusConsumer(1, 0) {
            @Override
            public Vec3d getBeamEndpoint() {
                Vec3d position = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                IBlockState state = world.getBlockState(pos);
                if (state.getPropertyKeys().contains(IDirectionalBlock.DIRECTION)) {
                    switch (state.getValue(IDirectionalBlock.DIRECTION)) {
                        case DOWN:  return position.add(0.5, 0.9, 0.5);
                        case EAST:  return position.add(0.1, 0.5, 0.5);
                        case NORTH: return position.add(0.5, 0.5, 0.9);
                        case SOUTH: return position.add(0.5, 0.5, 0.1);
                        case WEST:  return position.add(0.9, 0.5, 0.5);
                        case UP:
                        default:    return position.add(0.5, 0.1, 0.5);
                    }
                }
                
                return position.add(0.5, 0.4375, 0.5);
            }
        };
        forgeEnergy = new CustomEnergyStorage(TAConfig.impetusGeneratorBufferSize.getValue(), 0,
                TAConfig.impetusGeneratorMaxExtract.getValue(), 0);
        ticks = ThreadLocalRandom.current().nextInt(20);
    }
    
    @Override
    public void update() {
        if (!world.isRemote && ticks++ % 10 == 0) {
            IBlockState state = world.getBlockState(pos);
            if (state.getValue(IEnabledBlock.ENABLED)) {
                if (forgeEnergy.getEnergyStored() + TAConfig.impetusGeneratorEnergyPerImpetus.getValue() <= forgeEnergy.getMaxEnergyStored()) {
                    ConsumeResult result = node.consume(1, false);
                    if (result.energyConsumed == 1) {
                        forgeEnergy.setEnergy(forgeEnergy.getEnergyStored() + TAConfig.impetusGeneratorEnergyPerImpetus.getValue());
                        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 6);
                        world.addBlockEvent(pos, getBlockType(), 1, 0);
                        NodeHelper.syncAllImpetusTransactions(result.paths.keySet());
                        for (Map.Entry<Deque<IImpetusNode>, Long> entry : result.paths.entrySet())
                            NodeHelper.damageEntitiesFromTransaction(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        
        if (!world.isRemote) {
            EnumFacing facing = world.getBlockState(pos).getValue(IDirectionalBlock.DIRECTION);
            TileEntity neighbor = world.getTileEntity(pos.offset(facing));
            if (neighbor != null) {
                IEnergyStorage other = neighbor.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite());
                if (other != null && other.canReceive()) {
                    int extract = forgeEnergy.extractEnergy(TAConfig.impetusGeneratorMaxExtract.getValue(), true);
                    extract = other.receiveEnergy(extract, false);
                    if (extract > 0) {
                        forgeEnergy.extractEnergy(extract, false);
                        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 6);
                        world.addBlockEvent(pos, getBlockType(), 1, 0);
                    }
                }
            }
        }
    }
    
    @Override
    public void setPos(BlockPos posIn) {
        super.setPos(posIn);
        if (world != null)
            node.setLocation(new DimensionalBlockPos(pos.toImmutable(), world.provider.getDimension()));
    }
    
    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        node.setLocation(new DimensionalBlockPos(pos.toImmutable(), world.provider.getDimension()));
    }
    
    @Override
    public void onLoad() {
        node.init(world);
        ThaumicAugmentation.proxy.registerRenderableImpetusNode(node);
    }
    
    @Override
    public void invalidate() {
        if (!world.isRemote)
            NodeHelper.syncDestroyedImpetusNode(node);
        
        node.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(node);
        super.invalidate();
    }
    
    @Override
    public void onChunkUnload() {
        node.unload();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(node);
    }
    
    @Override
    public boolean receiveClientEvent(int id, int type) {
        ThaumicAugmentation.proxy.getRenderHelper().renderSpark(world, pos.getX() + world.rand.nextFloat(),
                pos.getY() + world.rand.nextFloat(), pos.getZ() + world.rand.nextFloat(), 1.5F, Aspect.ELDRITCH.getColor(), false);
        
        return true;
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
