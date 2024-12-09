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

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.BufferedImpetusProvider;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.capability.InfiniteImpetusStorage;

public class TileCreativeImpetusSource extends TileEntity implements ITickable {

    protected BufferedImpetusProvider provider;
    protected InfiniteImpetusStorage storage;
    protected int ticks;

    public TileCreativeImpetusSource() {
	super();
	storage = new InfiniteImpetusStorage(true, false);
	provider = new BufferedImpetusProvider(0, Integer.MAX_VALUE, storage);
	ticks = ThreadLocalRandom.current().nextInt(20);
    }

    @Override
    public void update() {
	if (!world.isRemote && ticks++ % 20 == 0)
	    NodeHelper.validate(provider, world);
    }

    @Override
    public void setPos(BlockPos posIn) {
	super.setPos(posIn);
	if (world != null)
	    provider.setLocation(new DimensionalBlockPos(pos.toImmutable(), world.provider.getDimension()));
    }

    @Override
    public void setWorld(World worldIn) {
	super.setWorld(worldIn);
	provider.setLocation(new DimensionalBlockPos(pos.toImmutable(), world.provider.getDimension()));
    }

    @Override
    public void onLoad() {
	ThaumicAugmentation.proxy.registerRenderableImpetusNode(provider);
    }

    @Override
    public void invalidate() {
	if (!world.isRemote)
	    NodeHelper.syncDestroyedImpetusNode(provider);

	provider.destroy();
	ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(provider);
	super.invalidate();
    }

    @Override
    public void onChunkUnload() {
	provider.unload();
	ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(provider);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
	NBTTagCompound tag = super.getUpdateTag();
	tag.setTag("node", provider.serializeNBT());
	return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
	super.handleUpdateTag(tag);
	NodeHelper.tryConnectNewlyLoadedPeers(provider, world);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
	tag.setTag("node", provider.serializeNBT());
	return super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
	super.readFromNBT(nbt);
	provider.deserializeNBT(nbt.getCompoundTag("node"));
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
	if (capability == CapabilityImpetusNode.IMPETUS_NODE ||
		capability == CapabilityImpetusStorage.IMPETUS_STORAGE)
	    return true;
	else
	    return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
	if (capability == CapabilityImpetusNode.IMPETUS_NODE)
	    return CapabilityImpetusNode.IMPETUS_NODE.cast(provider);
	else if (capability == CapabilityImpetusStorage.IMPETUS_STORAGE)
	    return CapabilityImpetusStorage.IMPETUS_STORAGE.cast(storage);
	else
	    return super.getCapability(capability, facing);
    }

}
