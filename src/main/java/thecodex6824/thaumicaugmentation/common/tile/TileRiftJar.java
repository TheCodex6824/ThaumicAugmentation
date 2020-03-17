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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import thaumcraft.api.aspects.Aspect;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.event.RiftJarVoidItemEvent;
import thecodex6824.thaumicaugmentation.api.tile.CapabilityRiftJar;
import thecodex6824.thaumicaugmentation.api.tile.RiftJar;
import thecodex6824.thaumicaugmentation.api.util.FluxRiftReconstructor;
import thecodex6824.thaumicaugmentation.common.network.PacketRiftJarInstability;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class TileRiftJar extends TileEntity implements ITickable {

    protected class VoidInventory implements IItemHandler {
        
        @Override
        @Nonnull
        @SuppressWarnings("null")
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            RiftJarVoidItemEvent event = new RiftJarVoidItemEvent(stack, rift, world, pos, simulate);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled()) {
                if (!simulate)
                    setRiftStability(Math.max(stability - stack.getCount() * 3, -200));
                
                return ItemStack.EMPTY;
            }
            else
                return stack;
        }
        
        @Override
        @Nonnull
        @SuppressWarnings("null")
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }
        
        @Override
        public int getSlots() {
            return 1;
        }
        
        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }
        
        @Override
        @Nonnull
        @SuppressWarnings("null")
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    }
    
    protected RiftJar rift;
    protected IItemHandler inventory;
    protected int stability;
    protected long lastStabilityUpdate;
    
    public TileRiftJar() {
        super();
        rift = new RiftJar(0, 0) {
        
            @Override
            public void setRift(FluxRiftReconstructor newRift) {
                super.setRift(newRift);
                if (!world.isRemote) {
                    markDirty();
                    world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
                }
            }
            
        };
        inventory = new VoidInventory();
    }
    
    @Override
    public void update() {
        if (world.getTotalWorldTime() - lastStabilityUpdate >= 100)
            setRiftStability(Math.min(0, (int) (stability + lastStabilityUpdate / 100)));
    }
    
    public void setRiftStability(int newStability) {
        if (stability != newStability) {
            stability = newStability;
            lastStabilityUpdate = world.getTotalWorldTime();
            if (!world.isRemote) {
                TANetwork.INSTANCE.sendToAllTracking(new PacketRiftJarInstability(pos, stability),
                        new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64.0));
            }
            else {
                ThaumicAugmentation.proxy.getRenderHelper().renderSpark(world, pos.getX() + 0.1865 + world.rand.nextDouble() * 0.626,
                        pos.getY() + world.rand.nextDouble() * 0.75, pos.getZ() + 0.1865 + world.rand.nextDouble() * 0.626, 1.5F, Aspect.ELDRITCH.getColor(), false);
            }
        }
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, rift.serializeNBT());
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        rift.deserializeNBT(pkt.getNbtCompound());
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("rift", rift.serializeNBT());
        return tag;
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return rift.hasRift();
        else
            return capability == CapabilityRiftJar.RIFT_JAR ? true : super.hasCapability(capability, facing);
    }
    
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && rift.hasRift())
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        else if (capability == CapabilityRiftJar.RIFT_JAR)
            return CapabilityRiftJar.RIFT_JAR.cast(rift);
        else
            return super.getCapability(capability, facing);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("rift", rift.serializeNBT());
        compound.setInteger("stability", stability);
        compound.setLong("lastStabUpdate", lastStabilityUpdate);
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        rift.deserializeNBT(compound.getCompoundTag("rift"));
        if (compound.hasKey("stability", NBT.TAG_INT))
            stability = compound.getInteger("stability");
        if (compound.hasKey("lastStabUpdate", NBT.TAG_LONG))
            lastStabilityUpdate = compound.getLong("lastStabUpdate");
    }
    
}
