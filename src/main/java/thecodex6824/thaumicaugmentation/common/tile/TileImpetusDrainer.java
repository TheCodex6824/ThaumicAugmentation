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

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
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
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.FluxRiftImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusAPI;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusProvider;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.BufferedImpetusProvider;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.tile.trait.IAnimatedTile;
import thecodex6824.thaumicaugmentation.common.tile.trait.IBreakCallback;

public class TileImpetusDrainer extends TileEntity implements ITickable, IAnimatedTile, IInteractWithCaster, IBreakCallback {

    protected IImpetusProvider provider;
    protected FluxRiftImpetusStorage storage;
    protected IAnimationStateMachine asm;
    protected VariableValue cycleLength;
    protected VariableValue delayTicks;
    protected VariableValue actionTime;
    protected int delay = ThreadLocalRandom.current().nextInt(-5, 6);
    protected boolean lastState = false;
    
    public TileImpetusDrainer() {
        super();
        storage = new FluxRiftImpetusStorage() {
            @Override
            public long extractEnergy(long maxToExtract, boolean simulate) {
                long result = super.extractEnergy(maxToExtract, simulate);
                EntityFluxRift rift = this.rift.get();
                if (rift != null)
                    ImpetusAPI.createImpetusParticles(world, rift.getPositionVector().add(0, rift.height / 2, 0), new Vec3d(pos).add(0.5, 0.5, 0.5));
                
                return result;
            }
        };
        provider = new BufferedImpetusProvider(0, 2, storage) {
            @Override
            public Vec3d getLocationForRendering() {
                return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.9375, pos.getZ() + 0.5);
            }
        };
        cycleLength = new VariableValue(1);
        delayTicks = new VariableValue(delay);
        actionTime = new VariableValue(Float.MIN_VALUE);
        asm = ThaumicAugmentation.proxy.loadASM(new ResourceLocation(ThaumicAugmentationAPI.MODID, "asms/block/impetus_drainer.json"), 
                ImmutableMap.<String, ITimeValue>of("cycle_length", cycleLength, "act_time", actionTime, "delay", delayTicks));
    }
    
    protected void findRift() {
        List<EntityFluxRift> rifts = world.getEntitiesWithinAABB(EntityFluxRift.class, new AxisAlignedBB(pos).grow(8.0));
        rifts.sort((rift1, rift2) -> Double.compare(rift1.getPosition().distanceSq(pos), rift2.getPosition().distanceSq(pos)));
        for (EntityFluxRift rift : rifts) {
            if (!rift.isDead) {
                //RayTraceResult result = world.rayTraceBlocks(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                //        rift.getPositionVector().add(0.0, rift.height / 2.0, 0.0));
                //if (result == null || result.getBlockPos() == null) {
                    storage.bindToRift(rift);
                    break;
                //}
            }
        }
    }
    
    @Override
    public void update() {
        if (!world.isRemote && world.getTotalWorldTime() % 60 == 0 && world.getBlockState(pos).getValue(IEnabledBlock.ENABLED))
            findRift();
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
        provider.setLocation(new DimensionalBlockPos(pos, world.provider.getDimension()));
    }
    
    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        provider.setLocation(new DimensionalBlockPos(pos, world.provider.getDimension()));
    }
    
    @Override
    public void onLoad() {
        provider.init();
        ThaumicAugmentation.proxy.registerRenderableImpetusNode(provider);
    }
    
    @Override
    public void onChunkUnload() {
        provider.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(provider);
    }
    
    @Override
    public void onBlockBroken() {
        provider.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(provider);
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        if (oldState.getBlock() != newState.getBlock()) {
            provider.destroy();
            ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(provider);
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
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("node", provider.serializeNBT());
        tag.setTag("energy", storage.serializeNBT());
        return super.writeToNBT(tag);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        provider.deserializeNBT(nbt.getCompoundTag("node"));
        storage.deserializeNBT(nbt.getCompoundTag("energy"));
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE ||
                capability == CapabilityImpetusStorage.IMPETUS_STORAGE ||
                capability == CapabilityAnimation.ANIMATION_CAPABILITY)
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
        else if (capability == CapabilityAnimation.ANIMATION_CAPABILITY)
            return CapabilityAnimation.ANIMATION_CAPABILITY.cast(asm);
        else
            return super.getCapability(capability, facing);
    }
    
}
