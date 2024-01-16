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
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.lib.utils.BlockStateUtils;
import thaumcraft.common.tiles.crafting.TileVoidSiphon;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.AugmentAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusAPI;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.ConsumeResult;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.SimpleImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.tile.trait.IAnimatedTile;
import thecodex6824.thaumicaugmentation.common.util.AnimationHelper;

public class TileImpetusDiffuser extends TileEntity implements ITickable, IAnimatedTile {
    
    protected SimpleImpetusConsumer consumer;
    protected IAnimationStateMachine asm;
    protected boolean lastState = false;
    protected int ticks;
    
    public TileImpetusDiffuser() {
        super();
        consumer = new SimpleImpetusConsumer(2, 0) {
            @Override
            public Vec3d getBeamEndpoint() {
                return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.21875, pos.getZ() + 0.5);
            }
        };
        
        ticks = ThreadLocalRandom.current().nextInt(20);
        asm = ThaumicAugmentation.proxy.loadASM(new ResourceLocation(ThaumicAugmentationAPI.MODID, "asms/block/impetus_diffuser.json"), 
                ImmutableMap.<String, ITimeValue>of());
    }
    
    @Override
    public void update() {
        if (!world.isRemote && ticks++ % 20 == 0 && world.getBlockState(pos).getValue(IEnabledBlock.ENABLED)) {
            for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos).grow(7))) {
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    for (ItemStack stack : player.inventory.mainInventory) {
                        IImpetusStorage storage = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                        if (storage != null && storage.canReceive()) {
                            long canReceive = Math.min(storage.receiveEnergy(Long.MAX_VALUE, true), 25);
                            ConsumeResult result = consumer.consume(canReceive, false);
                            if (storage.receiveEnergy(result.energyConsumed, false) > 0) {
                                ImpetusAPI.createImpetusParticles(world, new Vec3d(pos).add(0.5, 0.5, 0.5), player.getPositionVector().add(0, player.height / 2, 0));
                                NodeHelper.syncAllImpetusTransactions(result.paths.keySet());
                                for (Map.Entry<Deque<IImpetusNode>, Long> entry : result.paths.entrySet())
                                    NodeHelper.damageEntitiesFromTransaction(entry.getKey(), entry.getValue());
                            }
                        }
                        
                        IAugmentableItem augmentable = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                        if (augmentable != null) {
                            for (ItemStack s : augmentable.getAllAugments()) {
                                IImpetusStorage augStorage = s.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                                if (augStorage != null && augStorage.canReceive()) {
                                    long canReceive = Math.min(augStorage.receiveEnergy(Long.MAX_VALUE, true), 25);
                                    ConsumeResult result = consumer.consume(canReceive, false);
                                    if (augStorage.receiveEnergy(result.energyConsumed, false) > 0) {
                                        ImpetusAPI.createImpetusParticles(world, new Vec3d(pos).add(0.5, 0.5, 0.5), player.getPositionVector().add(0, player.height / 2, 0));
                                        NodeHelper.syncAllImpetusTransactions(result.paths.keySet());
                                        for (Map.Entry<Deque<IImpetusNode>, Long> entry : result.paths.entrySet())
                                            NodeHelper.damageEntitiesFromTransaction(entry.getKey(), entry.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
                
                for (Function<Entity, Iterable<ItemStack>> func : AugmentAPI.getAugmentableItemSources()) {
                    for (ItemStack stack : func.apply(entity)) {
                        IImpetusStorage storage = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                        if (storage != null && storage.canReceive()) {
                            long canReceive = Math.min(storage.receiveEnergy(Long.MAX_VALUE, true), 25);
                            ConsumeResult result = consumer.consume(canReceive, false);
                            if (storage.receiveEnergy(result.energyConsumed, false) > 0) {
                                ImpetusAPI.createImpetusParticles(world, new Vec3d(pos).add(0.5, 0.5, 0.5), entity.getPositionVector());
                                NodeHelper.syncAllImpetusTransactions(result.paths.keySet());
                                for (Map.Entry<Deque<IImpetusNode>, Long> entry : result.paths.entrySet())
                                    NodeHelper.damageEntitiesFromTransaction(entry.getKey(), entry.getValue());
                            }
                        }
                        
                        IAugmentableItem augmentable = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                        if (augmentable != null) {
                            for (ItemStack s : augmentable.getAllAugments()) {
                                IImpetusStorage augStorage = s.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                                if (augStorage != null && augStorage.canReceive()) {
                                    long canReceive = Math.min(augStorage.receiveEnergy(Long.MAX_VALUE, true), 25);
                                    ConsumeResult result = consumer.consume(canReceive, false);
                                    if (augStorage.receiveEnergy(result.energyConsumed, false) > 0) {
                                        ImpetusAPI.createImpetusParticles(world, new Vec3d(pos).add(0.5, 0.5, 0.5), entity.getPositionVector());
                                        NodeHelper.syncAllImpetusTransactions(result.paths.keySet());
                                        for (Map.Entry<Deque<IImpetusNode>, Long> entry : result.paths.entrySet())
                                            NodeHelper.damageEntitiesFromTransaction(entry.getKey(), entry.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
                
                IImpetusStorage entityStorage = entity.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                if (entityStorage != null && entityStorage.canReceive()) {
                    long canReceive = Math.min(entityStorage.receiveEnergy(Long.MAX_VALUE, true), 25);
                    ConsumeResult result = consumer.consume(canReceive, false);
                    if (entityStorage.receiveEnergy(result.energyConsumed, false) > 0) {
                        ImpetusAPI.createImpetusParticles(world, new Vec3d(pos).add(0.5, 0.5, 0.5), entity.getPositionVector());
                        NodeHelper.syncAllImpetusTransactions(result.paths.keySet());
                        for (Map.Entry<Deque<IImpetusNode>, Long> entry : result.paths.entrySet())
                            NodeHelper.damageEntitiesFromTransaction(entry.getKey(), entry.getValue());
                    }
                }
            }
            
            MutableBlockPos check = new MutableBlockPos();
            for (int y = -4; y < 5; ++y) {
                for (int x = -4; x < 5; ++x) {
                    for (int z = -4; z < 5; ++z) {
                        check.setPos(x + pos.getX(), y + pos.getY(), z + pos.getZ());
                        IBlockState state = world.getBlockState(check);
                        if (state.getBlock() == BlocksTC.voidSiphon) {
                            TileEntity tile = world.getTileEntity(check);
                            if (tile instanceof TileVoidSiphon) {
                                TileVoidSiphon siphon = (TileVoidSiphon) tile;
                                if (BlockStateUtils.isEnabled(state)) {
                                    ItemStack initial = siphon.getStackInSlot(0);
                                    if (initial.isEmpty() ||
                                            (initial.getItem() == ItemsTC.voidSeed && initial.getCount() < initial.getMaxStackSize())) {
                                        
                                        ConsumeResult result = consumer.consume(75, false);
                                        if (result.energyConsumed > 0) {
                                            NodeHelper.syncAllImpetusTransactions(result.paths.keySet());
                                            for (Map.Entry<Deque<IImpetusNode>, Long> entry : result.paths.entrySet())
                                                NodeHelper.damageEntitiesFromTransaction(entry.getKey(), entry.getValue());
                                            
                                            siphon.progress += (int) Math.ceil(result.energyConsumed / 1.5F);
                                            if ((ticks - 1) % 40 == 0) {
                                                ImpetusAPI.createImpetusParticles(world, new Vec3d(pos).add(0.5, 0.65, 0.5),
                                                        new Vec3d(check).add(0.5, 0.85, 0.5));
                                            }
                                            
                                            boolean sync = false;
                                            while (siphon.progress >= 2000) {
                                                ItemStack contained = siphon.getStackInSlot(0);
                                                if (contained.isEmpty() ||
                                                        (contained.getItem() == ItemsTC.voidSeed && contained.getCount() < contained.getMaxStackSize())) {
                                                    
                                                    siphon.progress -= 2000;
                                                    if (contained.isEmpty())
                                                      siphon.setInventorySlotContents(0, new ItemStack(ItemsTC.voidSeed));
                                                    else
                                                      siphon.setInventorySlotContents(0, new ItemStack(contained.getItem(), contained.getCount() + 1));
                                                    
                                                    sync = true;
                                                }
                                                else
                                                    break;
                                            }
                                            
                                            if (sync)
                                                siphon.syncTile(false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else if (world.isRemote && world.getTotalWorldTime() % 20 == 0) {
            IBlockState state = world.getBlockState(pos);
            boolean enabled = state.getPropertyKeys().contains(IEnabledBlock.ENABLED) && 
                    state.getValue(IEnabledBlock.ENABLED);
            if (enabled != lastState) {
                lastState = enabled;
                AnimationHelper.transitionSafely(asm, lastState ? "enabled" : "disabled");
            }
        }
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
    public boolean hasFastRenderer() {
        return true;
    }
    
    @Override
    public void handleEvents(float time, Iterable<Event> pastEvents) {}
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("node", consumer.serializeNBT());
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        consumer.init(world);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("node", consumer.serializeNBT());
        return super.writeToNBT(tag);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        consumer.deserializeNBT(nbt.getCompoundTag("node"));
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE ||
                capability == CapabilityAnimation.ANIMATION_CAPABILITY)
            return true;
        else
            return super.hasCapability(capability, facing);
    }
    
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE)
            return CapabilityImpetusNode.IMPETUS_NODE.cast(consumer);
        else if (capability == CapabilityAnimation.ANIMATION_CAPABILITY)
            return CapabilityAnimation.ANIMATION_CAPABILITY.cast(asm);
        else
            return super.getCapability(capability, facing);
    }
    
}
