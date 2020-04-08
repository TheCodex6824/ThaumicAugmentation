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
    protected int timer;
    protected int lastResult;
    protected WeakReference<Entity> target;
    
    protected UUID serverTargetID;
    protected int clientTargetID;
    
    public TileRiftMonitor() {
        super();
        lastResult = -1;
        target = new WeakReference<>(null);
        clientTargetID = -1;
    }
    
    @Nullable
    public Entity getTarget() {
        return target.get();
    }
    
    public void cycleTarget() {
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos).grow(12), entity -> entity instanceof EntityFluxRift || entity instanceof IDimensionalFracture);
        entities.sort((e1, e2) -> Double.compare(e1.getDistanceSq(pos), e2.getDistanceSq(pos)));
        if (!entities.isEmpty()) {
            if (target.get() == null || target.get().isDead) {
                target = new WeakReference<>(entities.get(0));
                serverTargetID = entities.get(0).getPersistentID();
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                markDirty();
            }
            else {
                boolean found = false;
                for (Entity e : entities) {
                    if (e.equals(target.get())) {
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    target = new WeakReference<>(entities.get(0));
                    serverTargetID = entities.get(0).getPersistentID();
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
    
    protected void loadTargetFromID() {
        if (!world.isRemote) {
            List<Entity> list = world.getEntities(Entity.class, e -> e != null && e.getPersistentID().equals(serverTargetID));
            if (!list.isEmpty()) {
                target = new WeakReference<>(list.get(0));
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
            }
        }
        else {
            Entity e = world.getEntityByID(clientTargetID);
            if (e != null) {
                target = new WeakReference<>(e);
                clientTargetID = -1;
            }
        }
    }
    
    @Override
    public void update() {
        if (!world.isRemote) {
            if (timer++ % 20 == 0 && (target.get() == null || target.get().isDead)) {
                if (serverTargetID != null)
                    loadTargetFromID();
                
                if (target.get() == null || target.get().isDead)
                    cycleTarget();
            }
            
            if (target.get() != null) {
                if (target.get().isDead) {
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
        else if (world.isRemote && clientTargetID != -1)
            loadTargetFromID();
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
        clientTargetID = pkt.getNbtCompound().getInteger("targetID");
        if (clientTargetID != -1)
            loadTargetFromID();
        else
            target.clear();
        
        world.markBlockRangeForRenderUpdate(pos, pos.up());
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = super.getUpdateTag();
        compound.setBoolean("mode", mode);
        Entity e = target.get();
        compound.setInteger("targetID", e != null ? e.getEntityId() : -1);
        
        return compound;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        clientTargetID = tag.getInteger("targetID");
        if (clientTargetID != -1)
            loadTargetFromID();
        else
            target.clear();
        
        world.markBlockRangeForRenderUpdate(pos, pos.up());
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("mode", mode);
        if (serverTargetID != null)
            compound.setUniqueId("target", serverTargetID);
        
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        mode = compound.getBoolean("mode");
        serverTargetID = compound.getUniqueId("target");
    }
    
}
