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

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.ImpetusNode;
import thecodex6824.thaumicaugmentation.api.tile.IImpetusGate;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.tile.trait.IBreakCallback;

public class TileImpetusGate extends TileEntity implements ITickable, IBreakCallback, IImpetusGate {
    
    protected ImpetusNode node;
    protected byte limit;
    protected boolean mode;
    protected int ticks;
    
    public TileImpetusGate() {
        node = new ImpetusNode(1, 1) {
            @Override
            public long onTransaction(IImpetusConsumer originator, Deque<IImpetusNode> path, long energy,
                    boolean simulate) {
                
                return energy >= getLimit() ? energy : 0;
            }
            
            @Override
            public Vec3d getBeamEndpoint() {
                Vec3d position = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                IBlockState state = world.getBlockState(pos);
                if (state.getPropertyKeys().contains(IDirectionalBlock.DIRECTION)) {
                    switch (state.getValue(IDirectionalBlock.DIRECTION)) {
                        case DOWN:  return position.add(0.5, 0.5625, 0.5);
                        case EAST:  return position.add(0.4375, 0.5, 0.5);
                        case NORTH: return position.add(0.5, 0.5, 0.5625);
                        case SOUTH: return position.add(0.5, 0.5, 0.4375);
                        case WEST:  return position.add(0.5625, 0.5, 0.5);
                        case UP:
                        default:    return position.add(0.5, 0.4375, 0.5);
                    }
                }
                
                return position.add(0.5, 0.4375, 0.5);
            }
        };
        
        ticks = ThreadLocalRandom.current().nextInt(20);
    }
    
    @Override
    public void update() {
        if (!world.isRemote && ticks++ % 20 == 0)
            NodeHelper.validateOutputs(world, node);
    }
    
    @Override
    public void cycleLimit() {
        if (limit == 15)
            limit = 0;
        else
            ++limit;
        
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }
    
    @Override
    public void cycleMode() {
        mode = !mode;
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }
    
    @Override
    public boolean isInRedstoneMode() {
        return mode;
    }
    
    @Override
    public long getLimit() {
        if (!mode) {
            if (limit == 0)
                return 0;
            else if (limit == 15)
                return Long.MAX_VALUE;
            else
                return (long) Math.pow(2, limit);
        }
        else {
            int level = Math.min(world.getRedstonePowerFromNeighbors(pos), 15);
            if (level == 0)
                return 0;
            else if (level == 15)
                return Long.MAX_VALUE;
            else
                return (long) Math.pow(2, level);
        }
    }
    
    @Override
    public int getManualLimitLevel() {
        return mode ? -1 : limit;
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
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("node", node.serializeNBT());
        tag.setBoolean("mode", mode);
        tag.setByte("limit", limit);
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        node.init(world);
        mode = tag.getBoolean("mode");
        limit = tag.getByte("limit");
        world.markBlockRangeForRenderUpdate(pos, pos);
    }
    
    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("mode", mode);
        tag.setByte("limit", limit);
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        mode = pkt.getNbtCompound().getBoolean("mode");
        limit = pkt.getNbtCompound().getByte("limit");
        world.markBlockRangeForRenderUpdate(pos, pos);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("node", node.serializeNBT());
        tag.setByte("limit", limit);
        tag.setBoolean("mode", mode);
        return super.writeToNBT(tag);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        node.deserializeNBT(nbt.getCompoundTag("node"));
        limit = nbt.getByte("limit");
        mode = nbt.getBoolean("mode");
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE)
            return true;
        else
            return super.hasCapability(capability, facing);
    }
    
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE)
            return CapabilityImpetusNode.IMPETUS_NODE.cast(node);
        else
            return super.getCapability(capability, facing);
    }
    
}
