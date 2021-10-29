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

import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import thaumcraft.api.aspects.Aspect;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.ConsumeResult;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.SimpleImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class TileVoidRechargePedestal extends TileEntity implements ITickable {

    protected ItemStackHandler inventory;
    protected SimpleImpetusConsumer consumer;
    protected int ticks;
    
    public TileVoidRechargePedestal() {
        inventory = new ItemStackHandler(1) {
            
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (stack.hasCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null))
                    return true;
                else {
                    IAugmentableItem item = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                    if (item != null) {
                        for (ItemStack s : item.getAllAugments()) {
                            if (s.hasCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null))
                                return true;
                        }
                    }
                }
                
                return false;
            }
            
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
            
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 6);
            }
            
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (isItemValid(slot, stack))
                    return super.insertItem(slot, stack, simulate);
                else
                    return stack;
            }
            
        };
        
        consumer = new SimpleImpetusConsumer(1, 0);
        ticks = ThreadLocalRandom.current().nextInt(20);
    }
    
    @Override
    public void update() {
        if (!world.isRemote && ticks++ % 10 == 0 && !inventory.getStackInSlot(0).isEmpty()) {
            boolean sync = false;
            ArrayList<Map<Deque<IImpetusNode>, Long>> transactions = new ArrayList<>();
            ItemStack stack = inventory.getStackInSlot(0);
            IImpetusStorage storage = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
            if (storage != null) {
                long receivable = storage.receiveEnergy(Long.MAX_VALUE, true);
                ConsumeResult consume = consumer.consume(receivable, false);
                if (storage.receiveEnergy(consume.energyConsumed, false) > 0) {
                    sync = true;
                    transactions.add(consume.paths);
                }
            }
            
            IAugmentableItem aug = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
            if (aug != null) {
                for (ItemStack augment : aug.getAllAugments()) {
                    storage = augment.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                    if (storage != null) {
                        long receivable = storage.receiveEnergy(Long.MAX_VALUE, true);
                        ConsumeResult consume = consumer.consume(receivable, false);
                        if (storage.receiveEnergy(consume.energyConsumed, false) > 0) {
                            sync = true;
                            transactions.add(consume.paths);
                        }
                    }
                }
            }
            
            if (sync) {
                markDirty();
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 6);
                for (Map<Deque<IImpetusNode>, Long> map : transactions) {
                    NodeHelper.syncAllImpetusTransactions(map.keySet());
                    for (Map.Entry<Deque<IImpetusNode>, Long> entry : map.entrySet())
                        NodeHelper.damageEntitiesFromTransaction(entry.getKey(), entry.getValue());
                }
                
                TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.SPARK,
                        pos.getX() + 0.5 + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.25, pos.getY() + 0.9 + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.25,
                        pos.getZ() + 0.5 + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.25, 1.5, Aspect.ELDRITCH.getColor()),
                        new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64.0));
            }
        }
    }
    
    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, inventory.serializeNBT());
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        inventory.deserializeNBT(pkt.getNbtCompound());
    }
    
    @Override
    public void setPos(BlockPos posIn) {
        super.setPos(posIn);
        if (world != null)
            consumer.setLocation(new DimensionalBlockPos(pos.toImmutable(), world.provider.getDimension()));
    }
    
    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        consumer.setLocation(new DimensionalBlockPos(pos.toImmutable(), world.provider.getDimension()));
    }
    
    @Override
    public void onLoad() {
        consumer.init(world);
        ThaumicAugmentation.proxy.registerRenderableImpetusNode(consumer);
    }
    
    @Override
    public void invalidate() {
        if (!world.isRemote)
            NodeHelper.syncDestroyedImpetusNode(consumer);
        
        consumer.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(consumer);
        super.invalidate();
    }
    
    @Override
    public void onChunkUnload() {
        consumer.unload();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(consumer);
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("node", consumer.serializeNBT());
        tag.setTag("inv", inventory.serializeNBT());
        return super.writeToNBT(tag);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        consumer.deserializeNBT(nbt.getCompoundTag("node"));
        inventory.deserializeNBT(nbt.getCompoundTag("inv"));
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("node", consumer.serializeNBT());
        tag.setTag("inv", inventory.serializeNBT());
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        consumer.init(world);
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return true;
        else
            return capability == CapabilityImpetusNode.IMPETUS_NODE ? true : super.hasCapability(capability, facing);
    }
    
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        else if (capability == CapabilityImpetusNode.IMPETUS_NODE)
            return CapabilityImpetusNode.IMPETUS_NODE.cast(consumer);
        else
            return super.getCapability(capability, facing);
    }
    
}
