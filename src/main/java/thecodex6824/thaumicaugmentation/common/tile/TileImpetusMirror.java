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
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import thaumcraft.api.aura.AuraHelper;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.ImpetusNode;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

public class TileImpetusMirror extends TileEntity implements ITickable {

    protected ImpetusNode node;
    protected DimensionalBlockPos linked;
    protected int ticks;
    protected boolean needsSync;
    protected boolean open;
    protected long fluxProgress;

    public TileImpetusMirror() {
	node = new ImpetusNode(2, 2) {

	    @Override
	    public boolean canConnectNodeAsInput(IImpetusNode toConnect) {
		return (getNumInputs() < getMaxInputs() - 1 && !toConnect.getLocation().equals(linked)) ||
			(inputs.contains(linked) && !toConnect.getLocation().equals(linked));
	    }

	    @Override
	    public boolean canConnectNodeAsOutput(IImpetusNode toConnect) {
		return (getNumOutputs() < getMaxOutputs() - 1 && !toConnect.getLocation().equals(linked)) ||
			(outputs.contains(linked) && !toConnect.getLocation().equals(linked));
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
	    public boolean shouldPhysicalBeamLinkTo(IImpetusNode other) {
		return !other.getLocation().equals(linked);
	    }

	    @Override
	    public boolean shouldEnforceBeamLimitsWith(IImpetusNode other) {
		return !other.getLocation().equals(linked);
	    }

	    @Override
	    public void onConnected(IImpetusNode other) {
		if (!world.isRemote && !linked.isInvalid() && other.getLocation().equals(linked)) {
		    open = true;
		    needsSync = true;
		}
	    }

	    @Override
	    public void onDisconnected(IImpetusNode other) {
		if (!world.isRemote && !linked.isInvalid() && other.getLocation().equals(linked)) {
		    open = false;
		    needsSync = true;
		}
	    }

	    @Override
	    public Vec3d getBeamEndpoint() {
		Vec3d position = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
		IBlockState state = world.getBlockState(pos);
		if (state.getPropertyKeys().contains(IDirectionalBlock.DIRECTION)) {
		    switch (world.getBlockState(pos).getValue(IDirectionalBlock.DIRECTION)) {
		    case DOWN:  return position.add(0.5, 0.98125, 0.5);
		    case EAST:  return position.add(0.01875, 0.5, 0.5);
		    case NORTH: return position.add(0.5, 0.5, 0.98125);
		    case SOUTH: return position.add(0.5, 0.5, 0.01875);
		    case WEST:  return position.add(0.98125, 0.5, 0.5);
		    case UP:
		    default:    return position.add(0.5, 0.01875, 0.5);
		    }
		}

		return position.add(0.5, 0.01875, 0.5);
	    }

	    @Override
	    public long onTransaction(Deque<IImpetusNode> path, long energy,
		    boolean simulate) {

		energy = Math.min(energy, 45);
		fluxProgress += energy;
		markDirty();
		return energy;
	    }

	};

	linked = DimensionalBlockPos.INVALID;
	ticks = ThreadLocalRandom.current().nextInt(20);
    }

    @Override
    public void update() {
	if (!world.isRemote) {
	    if (ticks++ % 20 == 0) {
		if (!linked.isInvalid() && !node.getLocation().isInvalid()) {
		    World targetWorld = DimensionManager.getWorld(linked.getDimension());
		    if (targetWorld != null && targetWorld.isBlockLoaded(linked.getPos())) {
			TileEntity tile = targetWorld.getTileEntity(linked.getPos());
			if (tile instanceof TileImpetusMirror) {
			    IImpetusNode otherNode = tile.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
			    if (otherNode != null) {
				TileImpetusMirror otherMirror = (TileImpetusMirror) tile;
				if (otherMirror.getLink().isInvalid() || !otherNode.getInputLocations().contains(otherMirror.getLink())) {
				    otherMirror.setLink(node.getLocation());
				    markDirty();
				    needsSync = true;
				}
			    }
			}
		    }
		}

		while (fluxProgress >= 1000) {
		    AuraHelper.polluteAura(world, pos, world.rand.nextInt(3) + 1, true);
		    fluxProgress -= 1000;
		    markDirty();
		}
	    }

	    if (needsSync) {
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		needsSync = false;
	    }

	    if (ticks % 20 == 0)
		NodeHelper.validate(node, world);
	}

    }

    public void setLink(DimensionalBlockPos linkTo) {
	if (!linkTo.equals(linked)) {
	    if (!linked.isInvalid()) {
		IImpetusNode link = node.getGraph().findNodeByPosition(linked);
		if (link != null) {
		    node.removeInput(link);
		    node.removeOutput(link);
		}
		else {
		    node.removeInputLocation(linked);
		    node.removeOutputLocation(linked);
		}

		NodeHelper.syncRemovedImpetusNodeInput(node, linked);
		NodeHelper.syncRemovedImpetusNodeOutput(node, linked);
	    }

	    linked = linkTo;
	    if (!linked.isInvalid()) {
		World targetWorld = DimensionManager.getWorld(linked.getDimension());
		if (targetWorld != null && targetWorld.isBlockLoaded(linked.getPos())) {
		    TileEntity tile = targetWorld.getTileEntity(linked.getPos());
		    if (tile instanceof TileImpetusMirror) {
			IImpetusNode otherNode = tile.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
			if (otherNode != null) {
			    TileImpetusMirror otherMirror = (TileImpetusMirror) tile;
			    if (otherMirror.getLink().isInvalid() || !otherNode.getInputLocations().contains(otherMirror.getLink())) {
				otherMirror.setLink(node.getLocation());
				node.addInput(otherNode);
				node.addOutput(otherNode);
				NodeHelper.syncAddedImpetusNodeInput(node, linked);
				NodeHelper.syncAddedImpetusNodeOutput(node, linked);
			    }
			}
		    }
		}
	    }

	    markDirty();
	}
    }

    public DimensionalBlockPos getLink() {
	return linked;
    }

    public boolean shouldShowOpenMirror() {
	return open;
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
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
	return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
	tag.setTag("node", node.serializeNBT());
	if (!linked.isInvalid()) {
	    tag.setIntArray("link", linked.toArray());
	}

	tag.setLong("flux", fluxProgress);

	return super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
	super.readFromNBT(nbt);
	node.deserializeNBT(nbt.getCompoundTag("node"));
	if (nbt.hasKey("link", NBT.TAG_INT_ARRAY)) {
	    linked = new DimensionalBlockPos(nbt.getIntArray("link"));
	}
	else {
	    linked = DimensionalBlockPos.INVALID;
	}

	fluxProgress = nbt.getLong("flux");
    }

    @Override
    public NBTTagCompound getUpdateTag() {
	NBTTagCompound tag = super.getUpdateTag();
	tag.setTag("node", node.serializeNBT());
	if (!linked.isInvalid()) {
	    tag.setIntArray("link", linked.toArray());
	}

	tag.setBoolean("open", open);
	return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
	super.handleUpdateTag(tag);
	NodeHelper.tryConnectNewlyLoadedPeers(node, world);
	open = tag.getBoolean("open");
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
	NBTTagCompound tag = new NBTTagCompound();
	if (!linked.isInvalid()) {
	    tag.setIntArray("link", linked.toArray());
	}

	tag.setBoolean("open", open);
	return new SPacketUpdateTileEntity(pos, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
	if (world.isRemote) {
	    if (packet.getNbtCompound().hasKey("link", NBT.TAG_INT_ARRAY)) {
		linked = new DimensionalBlockPos(packet.getNbtCompound().getIntArray("link"));
	    }
	    else {
		linked = DimensionalBlockPos.INVALID;
	    }

	    world.markBlockRangeForRenderUpdate(pos, pos);
	    open = packet.getNbtCompound().getBoolean("open");
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
