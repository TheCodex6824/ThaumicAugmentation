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

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.animation.TimeValues.VariableValue;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import thaumcraft.api.casters.IInteractWithCaster;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusAPI;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.ConsumeResult;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.SimpleImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.tile.trait.IAnimatedTile;
import thecodex6824.thaumicaugmentation.common.tile.trait.IBreakCallback;

public class TileImpetusDiffuser extends TileEntity implements ITickable, IAnimatedTile, IInteractWithCaster, IBreakCallback {

    protected IImpetusConsumer consumer;
    protected IAnimationStateMachine asm;
    protected VariableValue actionTime;
    protected int delay = ThreadLocalRandom.current().nextInt(-5, 6);
    protected boolean lastState = false;
    
    public TileImpetusDiffuser() {
        super();
        consumer = new SimpleImpetusConsumer(2, 0) {
            @Override
            public Vec3d getBeamEndpoint() {
                return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.21875, pos.getZ() + 0.5);
            }
        };
        actionTime = new VariableValue(Float.MIN_VALUE);
        asm = ThaumicAugmentation.proxy.loadASM(new ResourceLocation(ThaumicAugmentationAPI.MODID, "asms/block/impetus_diffuser.json"), 
                ImmutableMap.<String, ITimeValue>of("cycle_length", new VariableValue(2), "act_time", actionTime, "delay", new VariableValue(delay)));
    }
    
    @Override
    public void update() {
        if (!world.isRemote && world.getTotalWorldTime() % 60 == 0 && world.getBlockState(pos).getValue(IEnabledBlock.ENABLED)) {
            for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos).grow(5))) {
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    for (ItemStack stack : player.inventory.mainInventory) {
                        IImpetusStorage storage = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                        if (storage != null && storage.canReceive()) {
                            long canReceive = Math.min(storage.receiveEnergy(Long.MAX_VALUE, true), 10);
                            ConsumeResult result = consumer.consume(canReceive);
                            if (storage.receiveEnergy(result.energyConsumed, false) > 0) {
                                ImpetusAPI.createImpetusParticles(world, new Vec3d(pos).add(0.5, 0.5, 0.5), player.getPositionVector().add(0, player.height / 2, 0));
                                NodeHelper.syncAllImpetusTransactions(result.paths);
                            }
                        }
                    }
                }
                
                for (ItemStack stack : entity.getEquipmentAndArmor()) {
                    IImpetusStorage storage = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                    if (storage != null && storage.canReceive()) {
                        long canReceive = Math.min(storage.receiveEnergy(Long.MAX_VALUE, true), 10);
                        ConsumeResult result = consumer.consume(canReceive);
                        if (storage.receiveEnergy(result.energyConsumed, false) > 0) {
                            ImpetusAPI.createImpetusParticles(world, new Vec3d(pos).add(0.5, 0.5, 0.5), entity.getPositionVector());
                            NodeHelper.syncAllImpetusTransactions(result.paths);
                        }
                    }
                }
            }
        }
        else if (world.isRemote && (world.getTotalWorldTime() + delay) % 5 == 0) {
            boolean enabled = world.getBlockState(pos).getValue(IEnabledBlock.ENABLED);
            if (enabled != lastState) {
                lastState = enabled;
                actionTime.setValue(Animation.getWorldTime(world, Animation.getPartialTickTime()));
                asm.transition(lastState ? "starting" : "stopping");
            }
        }
    }
    
    @Override
    public boolean onCasterRightClick(World world, ItemStack stack, EntityPlayer player, BlockPos pos, 
            EnumFacing face, EnumHand hand) {
        
        boolean result = NodeHelper.handleCasterInteract(this, world, stack, player, pos, face, hand);
        markDirty();
        return result;
    }
    
    @Override
    public void setPos(BlockPos posIn) {
        super.setPos(posIn);
        consumer.setLocation(new DimensionalBlockPos(pos, world.provider.getDimension()));
    }
    
    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        consumer.setLocation(new DimensionalBlockPos(pos, world.provider.getDimension()));
    }
    
    @Override
    public void onLoad() {
        consumer.init(world);
        ThaumicAugmentation.proxy.registerRenderableImpetusNode(consumer);
    }
    
    @Override
    public void onChunkUnload() {
        consumer.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(consumer);
    }
    
    @Override
    public void onBlockBroken() {
        consumer.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(consumer);
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        if (oldState.getBlock() != newState.getBlock()) {
            consumer.destroy();
            ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(consumer);
            return true;
        }
        
        return false;
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
