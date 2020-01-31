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

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.math.DoubleMath;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.animation.TimeValues.VariableValue;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.client.fx.beams.FXBeamBore;
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.api.util.RiftHelper;
import thecodex6824.thaumicaugmentation.common.tile.trait.IAnimatedTile;
import thecodex6824.thaumicaugmentation.common.tile.trait.IBreakCallback;

public class TileStabilityFieldGenerator extends TileEntity implements ITickable, IBreakCallback, IAnimatedTile {

    protected static class CustomEnergyStorage extends EnergyStorage {
        
        public CustomEnergyStorage(int capacity, int in, int out, int initial) {
            super(capacity, in, out, initial);
        }
        
        public void setEnergy(int newEnergy) {
            energy = newEnergy;
        }
        
    }
    
    protected static final float MAX_STABILITY = 4.0F;
    
    protected UUID serverLoadedID;
    protected WeakReference<EntityFluxRift> targetedRift;
    protected CustomEnergyStorage energy;
    protected float maxStabilityPerOperation;
    protected int ticks;
    
    protected int clientLoadedID;
    protected boolean lastEnabledState;
    protected Object beam;
    protected IAnimationStateMachine asm;
    protected VariableValue cycleLength;
    protected VariableValue actionTime;
    
    public TileStabilityFieldGenerator() {
        targetedRift = new WeakReference<>(null);
        energy = new CustomEnergyStorage(1000, 1000, 1000, 0);
        cycleLength = new VariableValue(1.0F);
        actionTime = new VariableValue(-1.0F);
        asm = ThaumicAugmentation.proxy.loadASM(new ResourceLocation(ThaumicAugmentationAPI.MODID, "asms/block/stability_field_generator.json"),
                ImmutableMap.of("cycle_length", cycleLength, "act_time", actionTime));
        maxStabilityPerOperation = MAX_STABILITY;
    }
    
    protected double getDistForFace(EnumFacing face, Entity entity) {
        return getDistForFace(face, entity.getPositionVector());
    }
    
    protected double getDistForFace(EnumFacing face, Vec3d vec) {
        if (face.getAxis() == Axis.X)
            return Math.abs(pos.getX() - vec.x);
        else if (face.getAxis() == Axis.Y)
            return Math.abs(pos.getY() - vec.y);
        else
            return Math.abs(pos.getZ() - vec.z);
    }
    
    @Nullable
    protected EntityFluxRift findClosestRift(EnumFacing face) {
        BlockPos pos1 = pos.offset(face).add(1.0 - face.getXOffset(), 1.0 - face.getYOffset(), 1.0 - face.getZOffset());
        BlockPos pos2 = pos.offset(face, 8).add(1.0 + face.getXOffset(), 1.0 + face.getYOffset(), 1.0 + face.getZOffset());
        List<EntityFluxRift> rifts = world.getEntitiesWithinAABB(EntityFluxRift.class, 
                new AxisAlignedBB(pos1.getX() - 1, pos1.getY() - 1, pos1.getZ() - 1, pos2.getX() + 2, pos2.getY() + 2, pos2.getZ() + 2),
                rift -> rift != null && !rift.getCollapse());
        if (!rifts.isEmpty()) {
            rifts.sort((rift1, rift2) -> Double.compare(getDistForFace(face, rift1), getDistForFace(face, rift2)));
            RayTraceResult trace = world.rayTraceBlocks(new Vec3d(pos.offset(face)), new Vec3d(pos.offset(face, 8)));
            EntityFluxRift chosenOne = rifts.get(0);
            if (trace == null || trace.hitVec == null || getDistForFace(face, chosenOne) < getDistForFace(face, trace.hitVec))
                return chosenOne;
        }
        
        return null;
    }
    
    @Override
    public void onLoad() {
        if (world.isRemote)
            lastEnabledState = world.getBlockState(pos).getValue(IEnabledBlock.ENABLED);
    }
    
    protected void loadTargetFromID() {
        if (!world.isRemote) {
            List<EntityFluxRift> test = world.getEntities(EntityFluxRift.class, e -> e != null && e.getUniqueID().equals(serverLoadedID));
            if (!test.isEmpty()) {
                targetedRift = new WeakReference<>(test.get(0));
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 1);
            }
            
            serverLoadedID = null;
        }
        else {
            List<EntityFluxRift> test = world.getEntities(EntityFluxRift.class, e -> e != null && e.getEntityId() == clientLoadedID);
            if (!test.isEmpty()) {
                targetedRift = new WeakReference<>(test.get(0));
                updateBeam();
                clientLoadedID = -1;
            }
        }
    }
    
    @Override
    public void update() {
        if (!world.isRemote && ticks++ % 5 == 0) {
            IBlockState state = world.getBlockState(pos);
            if (state.getValue(IEnabledBlock.ENABLED)) {
                int result = energy.extractEnergy(5, true);
                if (result == 5) {
                    energy.extractEnergy(5, false);
                    EntityFluxRift rift = targetedRift.get();
                    if (rift == null || rift.isDead) {
                        if (serverLoadedID != null)
                            loadTargetFromID();
                        
                        rift = targetedRift.get();
                        if (rift == null || rift.isDead) {
                            EnumFacing face = state.getValue(IDirectionalBlock.DIRECTION);
                            rift = findClosestRift(face);
                            if (rift != null)
                                targetedRift = new WeakReference<>(rift);
                            else
                                targetedRift.clear();

                            markDirty();
                            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                        }
                        else
                            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                    }
                    
                    if (rift != null && !rift.isDead) {
                        boolean update = false;
                        if (rift.getCollapse()) {
                            BlockPos boom = pos.offset(state.getValue(IDirectionalBlock.DIRECTION));
                            world.createExplosion(null, boom.getX(), boom.getY(), boom.getZ(), 2.0F, true);
                            targetedRift.clear();
                            markDirty();
                            update = true;
                        }
                        else if (rift.getRiftStability() < 100.0F && energy.extractEnergy(20, true) == 20) {
                            energy.extractEnergy(20, false);
                            rift.setRiftStability(rift.getRiftStability() + maxStabilityPerOperation);
                            markDirty();
                            update = true;
                        }
                        
                        float old = maxStabilityPerOperation;
                        maxStabilityPerOperation = Math.max(maxStabilityPerOperation * 0.95F, 0.05F);
                        if (!DoubleMath.fuzzyEquals(old, maxStabilityPerOperation, 0.00001F)) {
                            markDirty();
                            update = true;
                        }
                        
                        if (update)
                            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                    }
                }
                else if (targetedRift.get() != null) {
                    targetedRift.clear();
                    markDirty();
                    world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                }
            }
            else {
                float old = maxStabilityPerOperation;
                maxStabilityPerOperation = Math.min(Math.max(0.05F, maxStabilityPerOperation * 1.025F), MAX_STABILITY);
                if (!DoubleMath.fuzzyEquals(old, maxStabilityPerOperation, 0.00001F)) {
                    markDirty();
                    world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                }
            }
        }
        else if (world.isRemote && ticks++ % 5 == 0) {
            boolean on = world.getBlockState(pos).getValue(IEnabledBlock.ENABLED);
            if (!on)
                targetedRift.clear();
            
            if (on != lastEnabledState) {
                lastEnabledState = on;
                updateBeam();
            }
            
            if (world.rand.nextFloat() > maxStabilityPerOperation / MAX_STABILITY) {
                ThaumicAugmentation.proxy.getRenderHelper().renderSpark(world, pos.getX() + world.rand.nextFloat(),
                        pos.getY() + world.rand.nextFloat(), pos.getZ() + world.rand.nextFloat(), 5.0F, 0xAA0000, false);
            }
            
            EntityFluxRift rift = targetedRift.get();
            if (on && (rift == null || rift.isDead) && clientLoadedID != -1)
                loadTargetFromID();
        }
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    protected void updateBeam() {
        EntityFluxRift rift = targetedRift.get();
        if (rift != null && !rift.isDead && lastEnabledState && (beam == null || !((FXBeamBore) beam).isAlive())) {
            Vec3d dest = RiftHelper.getRiftCenter(rift).add(rift.posX, rift.posY, rift.posZ);
            EnumFacing face = world.getBlockState(pos).getValue(IDirectionalBlock.DIRECTION);
            double offsetX = 0.0, offsetY = 0.0, offsetZ = 0.0;
            switch (face.getAxis()) {
                case X: {
                    offsetX = face.getXOffset() > 0 ? 0.8125 : 0.1875;
                    offsetY = 0.5;
                    offsetZ = 0.5;
                    break;
                }
                case Y: {
                    offsetX = 0.5;
                    offsetY = face.getYOffset() > 0 ? 0.8125 : 0.1875;
                    offsetZ = 0.5;
                    break;
                }
                case Z: {
                    offsetX = 0.5;
                    offsetY = 0.5;
                    offsetZ = face.getZOffset() > 0 ? 0.8125 : 0.1875;
                    break;
                }
                default: break;
            }
            
            float mod = maxStabilityPerOperation / 10000.0F;
            int color = ((int) (0xFF * (mod / MAX_STABILITY)) << 16) | ((int) (0xBF * (mod / MAX_STABILITY)) << 8);
            beam = FXDispatcher.INSTANCE.beamBore(pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ,
                    dest.x, dest.y, dest.z, 1, color,
                    false, 0.05F, beam, 0);
            ((FXBeamBore) beam).setMaxAge(Integer.MAX_VALUE);
            updateBeamColor();
            actionTime.setValue(Animation.getWorldTime(world, Animation.getPartialTickTime()));
            asm.transition("opening");
        }
        else if ((rift == null || rift.isDead || !lastEnabledState) && beam != null) {
            if (((FXBeamBore) beam).isAlive()) {
                actionTime.setValue(Animation.getWorldTime(world, Animation.getPartialTickTime()));
                asm.transition("closing");
            }
            
            ((FXBeamBore) beam).setExpired();
        }
    }
    
    protected void updateBeamColor() {
        if (beam != null && ((FXBeamBore) beam).isAlive())
            ((FXBeamBore) beam).setRBGColorF(maxStabilityPerOperation / MAX_STABILITY, 0.75F * (maxStabilityPerOperation / MAX_STABILITY), 0);
    }
    
    @Override
    public void onBlockBroken() {
        targetedRift.clear();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != world.getBlockState(pos).getValue(IDirectionalBlock.DIRECTION))
            return true;
        else if (capability == CapabilityAnimation.ANIMATION_CAPABILITY)
            return true;
        else
            return super.hasCapability(capability, facing);
    }
    
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != world.getBlockState(pos).getValue(IDirectionalBlock.DIRECTION))
            return CapabilityEnergy.ENERGY.cast(energy);
        else if (capability == CapabilityAnimation.ANIMATION_CAPABILITY)
            return CapabilityAnimation.ANIMATION_CAPABILITY.cast(asm);
        else
            return super.getCapability(capability, facing);
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        Entity e = targetedRift.get();
        tag.setInteger("riftID", e != null ? e.getEntityId() : -1);
        tag.setInteger("energy", energy.getEnergyStored());
        tag.setFloat("stabRegen", maxStabilityPerOperation);
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        maxStabilityPerOperation = tag.getFloat("stabRegen");
        clientLoadedID = tag.getInteger("riftID");
        if (clientLoadedID != -1)
            loadTargetFromID();
        else {
            targetedRift.clear();
            updateBeam();
        }
        
        energy.setEnergy(tag.getInteger("energy"));
    }
    
    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        Entity e = targetedRift.get();
        compound.setInteger("riftID", e != null ? e.getEntityId() : -1);
        compound.setInteger("energy", energy.getEnergyStored());
        compound.setFloat("stabRegen", maxStabilityPerOperation);
        return new SPacketUpdateTileEntity(pos, 1, compound);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        clientLoadedID = pkt.getNbtCompound().getInteger("riftID");
        if (clientLoadedID != -1)
            loadTargetFromID();
        else {
            targetedRift.clear();
            updateBeam();
        }
        
        energy.setEnergy(pkt.getNbtCompound().getInteger("energy"));
        maxStabilityPerOperation = pkt.getNbtCompound().getFloat("stabRegen");
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        EntityFluxRift rift = targetedRift.get();
        if (rift != null && !rift.isDead)
            tag.setUniqueId("rift", rift.getUniqueID());
        
        tag.setInteger("energy", energy.getEnergyStored());
        tag.setFloat("stabRegen", maxStabilityPerOperation);
        return super.writeToNBT(tag);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        serverLoadedID = nbt.getUniqueId("rift");
        energy.setEnergy(nbt.getInteger("energy"));
        maxStabilityPerOperation = nbt.getFloat("stabRegen");
    }
    
    @Override
    public void handleEvents(float time, Iterable<Event> pastEvents) {}

    @Override
    public boolean hasFastRenderer() {
        return true;
    }
    
}
