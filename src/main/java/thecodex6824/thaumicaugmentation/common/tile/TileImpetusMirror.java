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
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.ImpetusNode;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.tile.trait.IBreakCallback;

public class TileImpetusMirror extends TileEntity implements ITickable, IBreakCallback {

    protected ImpetusNode node;
    protected DimensionalBlockPos linked;
    protected int ticks;
    
    public TileImpetusMirror() {
        node = new ImpetusNode(2, 2) {
            
            @Override
            public boolean canConnectNodeAsInput(IImpetusNode toConnect) {
                return (getNumInputs() < getMaxInputs() - 1 && !toConnect.getLocation().equals(linked)) ||
                        (graph.findNodeByPosition(linked) != null && !toConnect.getLocation().equals(linked));
            }
            
            @Override
            public boolean canConnectNodeAsOutput(IImpetusNode toConnect) {
                return (getNumOutputs() < getMaxOutputs() - 1 && !toConnect.getLocation().equals(linked)) ||
                        (graph.findNodeByPosition(linked) != null && !toConnect.getLocation().equals(linked));
            }
            
            @Override
            public boolean canRemoveNodeAsInput(IImpetusNode toRemove) {
                return !toRemove.getLocation().equals(linked);
            }
            
            @Override
            public boolean canRemoveNodeAsOutput(IImpetusNode toRemove) {
                return !toRemove.getLocation().equals(linked);
            }
            
            @Override
            public boolean shouldDrawBeamTo(IImpetusNode other) {
                return !other.getLocation().equals(linked);
            }
            
            @Override
            public Vec3d getBeamEndpoint() {
                Vec3d position = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                switch (Minecraft.getMinecraft().world.getBlockState(pos).getValue(IDirectionalBlock.DIRECTION)) {
                    case DOWN:  return position.add(0.5, 0.98125, 0.5);
                    case EAST:  return position.add(0.01875, 0.5, 0.5);
                    case NORTH: return position.add(0.5, 0.5, 0.98125);
                    case SOUTH: return position.add(0.5, 0.5, 0.01875);
                    case WEST:  return position.add(0.98125, 0.5, 0.5);
                    case UP:
                    default:    return position.add(0.5, 0.01875, 0.5);
                }
            }
            
        };
        
        linked = DimensionalBlockPos.INVALID;
    }
    
    @Override
    public void update() {
        if (!world.isRemote && ticks++ % 100 == 0 && !linked.isInvalid() &&
                !node.getLocation().isInvalid() && node.getGraph().findNodeByPosition(linked) == null) {
                
            World targetWorld = DimensionManager.getWorld(linked.getDimension());
            if (targetWorld != null && targetWorld.isBlockLoaded(linked.getPos())) {
                TileEntity tile = world.getTileEntity(linked.getPos());
                if (tile != null) {
                    IImpetusNode otherNode = tile.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                    if (otherNode != null) {
                        if (tile instanceof TileImpetusMirror)
                            ((TileImpetusMirror) tile).setLink(node.getLocation());
                        
                        node.addInput(otherNode);
                        node.addOutput(otherNode);
                        markDirty();
                        NodeHelper.syncAddedImpetusNodeInput(node, otherNode.getLocation());
                        NodeHelper.syncAddedImpetusNodeInput(otherNode, node.getLocation());
                        NodeHelper.syncAddedImpetusNodeOutput(node, otherNode.getLocation());
                        NodeHelper.syncAddedImpetusNodeOutput(otherNode, node.getLocation());
                    }
                }
            }
        }
    }
    
    @Override
    public void onBlockBroken() {
        if (!world.isRemote && !linked.isInvalid() && node.getLocation() != DimensionalBlockPos.INVALID &&
                node.getGraph().findNodeByPosition(linked) != null) {
                    
            World targetWorld = DimensionManager.getWorld(linked.getDimension());
            if (targetWorld != null && targetWorld.isBlockLoaded(linked.getPos())) {
                TileEntity tile = world.getTileEntity(linked.getPos());
                if (tile instanceof TileImpetusMirror)
                    ((TileImpetusMirror) tile).setLink(DimensionalBlockPos.INVALID);
            }
        }
        
        for (IImpetusNode input : node.getInputs())
            NodeHelper.syncRemovedImpetusNodeOutput(input, node.getLocation());
        
        for (IImpetusNode output : node.getOutputs())
            NodeHelper.syncRemovedImpetusNodeInput(output, node.getLocation());
        
        node.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(node);
    }
    
    public void setLink(DimensionalBlockPos linkTo) {
        if (linkTo != linked) {
            boolean removed = false;
            IImpetusNode link = node.getGraph().findNodeByPosition(linked);
            if (link != null) {
                removed = node.removeInput(link);
                removed |= node.removeOutput(link);
            }
            else {
                removed = node.removeInputLocation(linked);
                removed |= node.removeOutputLocation(linked);
            }
            
            if (removed && !world.isRemote) {
                NodeHelper.syncRemovedImpetusNodeInput(node, linked);
                NodeHelper.syncRemovedImpetusNodeOutput(node, linked);
            }
            
            if (!linkTo.isInvalid()) {
                node.addInputLocation(linkTo);
                node.addOutputLocation(linkTo);
                linked = linkTo;
                markDirty();
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
                if (!world.isRemote) {
                    World targetWorld = DimensionManager.getWorld(linked.getDimension());
                    if (targetWorld != null && targetWorld.isBlockLoaded(linked.getPos())) {
                        TileEntity tile = world.getTileEntity(linked.getPos());
                        if (tile != null) {
                            IImpetusNode otherNode = tile.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                            if (otherNode != null) {
                                if (tile instanceof TileImpetusMirror)
                                    ((TileImpetusMirror) tile).setLink(node.getLocation());
                                
                                node.addInput(otherNode);
                                node.addOutput(otherNode);
                                markDirty();
                                NodeHelper.syncAddedImpetusNodeInput(node, otherNode.getLocation());
                                NodeHelper.syncAddedImpetusNodeInput(otherNode, node.getLocation());
                                NodeHelper.syncAddedImpetusNodeOutput(node, otherNode.getLocation());
                                NodeHelper.syncAddedImpetusNodeOutput(otherNode, node.getLocation());
                            }
                        }
                    }
                }
            }
        }
    }
    
    public DimensionalBlockPos getLink() {
        return linked;
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
        if (!linked.isInvalid()) {
            node.addInputLocation(linked);
            node.addOutputLocation(linked);
        }
        node.init(world);
        ThaumicAugmentation.proxy.registerRenderableImpetusNode(node);
    }
    
    @Override
    public void invalidate() {
        node.unload();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(node);
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("node", node.serializeNBT());
        if (!linked.isInvalid())
            tag.setIntArray("link", linked.toArray());
        
        return super.writeToNBT(tag);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        node.deserializeNBT(nbt.getCompoundTag("node"));
        if (nbt.hasKey("link", NBT.TAG_INT_ARRAY))
            linked = new DimensionalBlockPos(nbt.getIntArray("link"));
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("node", node.serializeNBT());
        if (!linked.isInvalid())
            tag.setIntArray("link", linked.toArray());
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        node.init(world);
        if (tag.hasKey("link", NBT.TAG_INT_ARRAY))
            linked = new DimensionalBlockPos(tag.getIntArray("link"));
    }
    
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        if (!linked.isInvalid())
            tag.setIntArray("link", linked.toArray());
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        if (world.isRemote && packet.getNbtCompound().hasKey("link", NBT.TAG_INT_ARRAY)) {
            linked = new DimensionalBlockPos(packet.getNbtCompound().getIntArray("link"));
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
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
