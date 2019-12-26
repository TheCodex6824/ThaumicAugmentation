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

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.client.fx.beams.FXBeamBore;
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.api.util.RiftHelper;

public class TileStabilityFieldGenerator extends TileEntity implements ITickable {

    protected static class CustomEnergyStorage extends EnergyStorage {
        
        public CustomEnergyStorage(int capacity, int in, int out, int initial) {
            super(capacity, in, out, initial);
        }
        
        public void setEnergy(int newEnergy) {
            energy = newEnergy;
        }
        
    }
    
    protected UUID serverLoadedID;
    protected WeakReference<EntityFluxRift> targetedRift;
    protected CustomEnergyStorage energy;
    protected int ticks;
    
    protected int clientLoadedID;
    protected boolean lastEnabledState;
    protected Object beam;
    
    public TileStabilityFieldGenerator() {
        targetedRift = new WeakReference<>(null);
        energy = new CustomEnergyStorage(1000, 1000, 1000, 0);
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
                new AxisAlignedBB(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX() + 1, pos2.getY() + 1, pos2.getZ() + 1),
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
            List<EntityFluxRift> test = world.getEntities(EntityFluxRift.class, e -> e.getUniqueID().equals(serverLoadedID));
            if (!test.isEmpty()) {
                targetedRift = new WeakReference<>(test.get(0));
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 1);
            }
            
            serverLoadedID = null;
        }
        else {
            List<EntityFluxRift> test = world.getEntities(EntityFluxRift.class, e -> e.getEntityId() == clientLoadedID);
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
                int result = energy.extractEnergy(30, true);
                if (result == 30) {
                    energy.extractEnergy(30, false);
                    EntityFluxRift rift = targetedRift.get();
                    if (rift == null || rift.isDead) {
                        if (serverLoadedID != null)
                            loadTargetFromID();
                        
                        EnumFacing face = state.getValue(IDirectionalBlock.DIRECTION);
                        rift = findClosestRift(face);
                        if (rift != null) {
                            targetedRift = new WeakReference<>(rift);
                            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 1);
                            markDirty();
                        }
                        else {
                            targetedRift.clear();
                            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 1);
                            markDirty();
                        }
                    }
                    
                    if (rift != null && !rift.isDead) {
                        if (rift.getCollapse()) {
                            BlockPos boom = pos.offset(state.getValue(IDirectionalBlock.DIRECTION));
                            world.createExplosion(null, boom.getX(), boom.getY(), boom.getZ(), 2.0F, true);
                            targetedRift.clear();
                            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 1);
                            markDirty();
                        }
                        else if (rift.getRiftStability() < 100.0F) {
                            float stab = Math.min(100.0F - rift.getRiftStability(), Math.min(energy.getEnergyStored() / 100.0F, 200));
                            int cost = (int) (stab * 100);
                            if (cost > 0 && energy.extractEnergy(cost, true) == cost) {
                                energy.extractEnergy(cost, false);
                                rift.setRiftStability(rift.getRiftStability() + stab);
                                world.addBlockEvent(pos, getBlockType(), 1, 0);
                            }
                        }
                    }
                }
                else if (targetedRift.get() != null) {
                    targetedRift.clear();
                    world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 1);
                    markDirty();
                }
            }
        }
        else if (world.isRemote && ticks++ % 5 == 0) {
            boolean on = world.getBlockState(pos).getValue(IEnabledBlock.ENABLED);
            if (on != lastEnabledState) {
                lastEnabledState = on;
                // TODO put fancy latch animation here
                
                updateBeam();
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
        if (rift != null && lastEnabledState) {
            Vec3d dest = RiftHelper.getRiftCenter(rift).add(rift.posX, rift.posY, rift.posZ);
            EnumFacing face = world.getBlockState(pos).getValue(IDirectionalBlock.DIRECTION);
            double offsetX = 0.0, offsetY = 0.0, offsetZ = 0.0;
            switch (face.getAxis()) {
                case X: {
                    offsetX = face.getXOffset() * 0.5;
                    offsetY = 0.5;
                    offsetZ = 0.5;
                    break;
                }
                case Y: {
                    offsetX = 0.5;
                    offsetY = face.getYOffset() * 0.5;
                    offsetZ = 0.5;
                    break;
                }
                case Z: {
                    offsetX = 0.5;
                    offsetY = 0.5;
                    offsetZ = face.getZOffset() * 0.5;
                    break;
                }
                default: break;
            }
            
            if (beam == null || !((FXBeamBore) beam).isAlive()) {
                beam = FXDispatcher.INSTANCE.beamBore(pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ,
                        dest.x, dest.y, dest.z, 1, 0xFFBF00,
                        false, 0.05F, beam, 0);
                ((FXBeamBore) beam).setMaxAge(Integer.MAX_VALUE);
            }
        }
        else if ((rift == null || !lastEnabledState) && beam != null)
            ((FXBeamBore) beam).setExpired();
    }
    
    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (targetedRift.get() != null) {
            updateBeam();
            return true;
        }
        else
            return false;
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != world.getBlockState(pos).getValue(IDirectionalBlock.DIRECTION))
            return true;
        else
            return super.hasCapability(capability, facing);
    }
    
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != world.getBlockState(pos).getValue(IDirectionalBlock.DIRECTION))
            return CapabilityEnergy.ENERGY.cast(energy);
        else
            return super.getCapability(capability, facing);
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        Entity e = targetedRift.get();
        tag.setInteger("riftID", e != null ? e.getEntityId() : -1);
        tag.setInteger("energy", energy.getEnergyStored());
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
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
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        EntityFluxRift rift = targetedRift.get();
        if (rift != null && !rift.isDead)
            tag.setUniqueId("rift", rift.getUniqueID());
        
        tag.setInteger("energy", energy.getEnergyStored());
        return super.writeToNBT(tag);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        serverLoadedID = nbt.getUniqueId("rift");
        energy.setEnergy(nbt.getInteger("energy"));
    }
}
