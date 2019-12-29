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
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
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
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusAPI;
import thecodex6824.thaumicaugmentation.api.impetus.WeakImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusConsumer;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.NodeHelper;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.BufferedImpetusProvider;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.tile.trait.IAnimatedTile;
import thecodex6824.thaumicaugmentation.common.tile.trait.IBreakCallback;

public class TileImpetusDrainer extends TileEntity implements ITickable, IAnimatedTile, IBreakCallback {

    protected BufferedImpetusProvider provider;
    protected Vec3d lastRiftPos;
    protected WeakImpetusStorage storage;
    protected IAnimationStateMachine asm;
    protected VariableValue actionTime;
    protected boolean lastState = false;
    protected int ticks;
    
    public TileImpetusDrainer() {
        super();
        storage = new WeakImpetusStorage() {
            @Override
            public long extractEnergy(long maxToExtract, boolean simulate) {
                long result = super.extractEnergy(maxToExtract, simulate);
                if (result > 0 && !simulate)
                    ImpetusAPI.createImpetusParticles(world, lastRiftPos, new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                    
                return result;
            }
            
            @Override
            public void onEnergyChanged() {
                markDirty();
            }
        };
        provider = new BufferedImpetusProvider(0, 2, storage) {
            @Override
            public Vec3d getBeamEndpoint() {
                return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.4375, pos.getZ() + 0.5);
            }
            
            @Override
            public long onTransaction(IImpetusConsumer originator, Deque<IImpetusNode> path, long energy, boolean simulate) {
                if (!simulate)
                    markDirty();
                
                return energy;
            }
        };
        
        ticks = ThreadLocalRandom.current().nextInt(20);
        actionTime = new VariableValue(-1);
        asm = ThaumicAugmentation.proxy.loadASM(new ResourceLocation(ThaumicAugmentationAPI.MODID, "asms/block/impetus_drainer.json"), 
                ImmutableMap.<String, ITimeValue>of("cycle_length", new VariableValue(1), "act_time", actionTime, "delay", new VariableValue(ticks)));
    }
    
    protected void findRift() {
        List<EntityFluxRift> rifts = world.getEntitiesWithinAABB(EntityFluxRift.class, new AxisAlignedBB(pos).grow(8.0));
        rifts.sort((rift1, rift2) -> Double.compare(rift1.getPosition().distanceSq(pos), rift2.getPosition().distanceSq(pos)));
        for (EntityFluxRift rift : rifts) {
            if (!rift.isDead && rift.hasCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null)) {
                //RayTraceResult result = world.rayTraceBlocks(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                //        rift.getPositionVector().add(0.0, rift.height / 2.0, 0.0));
                //if (result == null || result.getBlockPos() == null) {
                    storage.bind(rift.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null));
                    lastRiftPos = rift.getPositionVector();
                    break;
                //}
            }
        }
    }
    
    @Override
    public void update() {
        if (!world.isRemote && ticks++ % 60 == 0 && world.getBlockState(pos).getValue(IEnabledBlock.ENABLED))
            findRift();
        else if (!world.isRemote && !world.getBlockState(pos).getValue(IEnabledBlock.ENABLED) && storage.isValid())
            storage.bind(null);
        else if (world.isRemote && ticks++ % 5 == 0) {
            boolean enabled = world.getBlockState(pos).getValue(IEnabledBlock.ENABLED);
            if (enabled != lastState) {
                lastState = enabled;
                actionTime.setValue(Animation.getWorldTime(world, Animation.getPartialTickTime()));
                asm.transition(lastState ? "starting" : "stopping");
            }
        }
        
        if (!world.isRemote && ticks % 20 == 0)
            NodeHelper.validateOutputs(world, provider);
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
        provider.init(world);
        ThaumicAugmentation.proxy.registerRenderableImpetusNode(provider);
    }
    
    @Override
    public void invalidate() {
        provider.unload();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(provider);
    }
    
    @Override
    public void onBlockBroken() {
        for (IImpetusNode input : provider.getInputs())
            NodeHelper.syncRemovedImpetusNodeOutput(input, provider.getLocation());
        
        for (IImpetusNode output : provider.getOutputs())
            NodeHelper.syncRemovedImpetusNodeInput(output, provider.getLocation());
        
        provider.destroy();
        ThaumicAugmentation.proxy.deregisterRenderableImpetusNode(provider);
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
        tag.setTag("node", provider.serializeNBT());
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        provider.init(world);
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
