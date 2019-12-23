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

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.api.entity.IDimensionalFracture;

public class TileRiftMonitor extends TileEntity implements ITickable {

    // Rifts: 0 = size, 1 = stability
    // Fractures: 0 = biome, 1 = opened
    protected boolean mode;
    protected int targetID;
    protected int timer;
    protected int lastResult;
    protected WeakReference<Entity> target;
    
    public TileRiftMonitor() {
        super();
        targetID = -1;
        lastResult = -1;
        target = new WeakReference<>(null);
    }
    
    @Nullable
    public Entity getTarget() {
        return target.get();
    }
    
    public void cycleTarget() {
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos).grow(7), entity -> entity instanceof EntityFluxRift || entity instanceof IDimensionalFracture);
        if (!entities.isEmpty()) {
            if (target.get() == null) {
                target = new WeakReference<>(entities.get(0));
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                markDirty();
            }
            else {
                boolean found = false;
                boolean set = false;
                for (Entity e : entities) {
                    if (found) {
                        target = new WeakReference<>(e);
                        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                        markDirty();
                        set = true;
                        break;
                    }
                    else if (e.equals(target.get()))
                        found = true;
                }
                
                if (!found || (found && !set)) {
                    target = new WeakReference<>(entities.get(0));
                    world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                    markDirty();
                }
            }
        }
    }
    
    public void cycleMode() {
        mode = !mode;
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        markDirty();
    }
    
    public void setMode(boolean newMode) {
        mode = newMode;
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        markDirty();
    }
    
    public boolean getMode() {
        return mode;
    }
    
    public int getComparatorOutput() {
        Entity entity = target.get();
        if (entity instanceof EntityFluxRift) {
            double ratio = 0.0;
            if (mode)
                ratio = (((EntityFluxRift) entity).getRiftStability() + 100.0) / 200.0;
            else
                ratio = Math.min(((EntityFluxRift) entity).getRiftSize() / 200.0, 200.0);
            
            return (int) (Math.floor(ratio * 14) + Math.signum(ratio));
        }
        
        return 0;
    }
    
    @Override
    public void update() {
        if (target.get() == null && targetID != -1 && world.getTotalWorldTime() % 10 == 0) {
            Entity e = world.getEntityByID(targetID);
            if (e != null) {
                target = new WeakReference<>(e);
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                markDirty();
                targetID = -1;
            }
            else if (!world.isRemote)
                targetID = -1;
        }
        
        if (!world.isRemote) {
            if (++timer % 20 == 0 && target.get() == null)
                cycleTarget();
            
            if (target.get() != null) {
                if (!target.get().isEntityAlive()) {
                    target.clear();
                    world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                    markDirty();
                }
                
                int level = getComparatorOutput();
                if (level != lastResult) {
                    world.updateComparatorOutputLevel(pos, getBlockType());
                    lastResult = level;
                }
            }
            else if (lastResult != 0) {
                world.updateComparatorOutputLevel(pos, getBlockType());
                lastResult = 0;
            }
        }
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos).expand(0.0, 1.0, 0.0);
    }
    
    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("mode", mode);
        Entity e = target.get();
        compound.setInteger("targetID", e != null ? e.getEntityId() : -1);
        return new SPacketUpdateTileEntity(pos, 1, compound);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        mode = pkt.getNbtCompound().getBoolean("mode");
        targetID = pkt.getNbtCompound().getInteger("targetID");
        world.markBlockRangeForRenderUpdate(pos, pos);
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("mode", mode);
        Entity e = target.get();
        compound.setInteger("targetID", e != null ? e.getEntityId() : -1);
        return compound;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        mode = tag.getBoolean("mode");
        targetID = tag.getInteger("targetID");
        world.markBlockRangeForRenderUpdate(pos, pos);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("mode", mode);
        Entity e = target.get();
        compound.setInteger("targetID", e != null ? e.getEntityId() : -1);
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        mode = compound.getBoolean("mode");
        targetID = compound.getInteger("targetID");
    }
    
}
