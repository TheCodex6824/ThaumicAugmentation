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

package thecodex6824.thaumicaugmentation.api.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import thecodex6824.thaumicaugmentation.api.block.property.IPortalBlock;

public final class PortalStateManager {

    private PortalStateManager() {}
    
    private static final Set<Entity> TRACKED = Collections.newSetFromMap(new WeakHashMap<>());
    
    public static void markEntityInPortal(Entity entity) {
        if (entity.hasCapability(CapabilityPortalState.PORTAL_STATE, null) && !TRACKED.contains(entity)) {
            entity.getCapability(CapabilityPortalState.PORTAL_STATE, null).setInPortal(true);
            TRACKED.add(entity);
        }
    }
    
    private static boolean findPortalBlock(Entity entity) {
        AxisAlignedBB box = entity.getEntityBoundingBox();
        if (box != null) {
            MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
            for (int x = (int) Math.floor(box.minX); x < Math.ceil(box.maxX); ++x) {
                for (int z = (int) Math.floor(box.minZ); z < Math.ceil(box.maxZ); ++z) {
                    pos.setPos(x, 0, z);
                    for (int y = (int) Math.floor(box.minY); y < Math.ceil(box.maxY); ++y) {
                        if (entity.getEntityWorld().isBlockLoaded(pos)) {
                            IBlockState state = entity.getEntityWorld().getBlockState(pos);
                            if ((state.getProperties().containsKey(IPortalBlock.PORTAL) && state.getValue(IPortalBlock.PORTAL)) ||
                                    (!state.getProperties().containsKey(IPortalBlock.PORTAL) && state.getBlock() instanceof IPortalBlock)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    private static boolean findPortalEntity(Entity entity) {
        AxisAlignedBB box = entity.getEntityBoundingBox();
        if (box != null) {
            for (Entity other : entity.getEntityWorld().getEntitiesWithinAABB(Entity.class, box)) {
                if (other != entity && other instanceof IPortalEntity)
                    return true;
            }
        }
        
        return false;
    }
    
    public static void tick() {
        ArrayList<Entity> toGetRidOf = new ArrayList<>();
        for (Entity entity : TRACKED) {
            if (!findPortalBlock(entity)) {
                if (!findPortalEntity(entity)) {
                    if (entity.hasCapability(CapabilityPortalState.PORTAL_STATE, null))
                        entity.getCapability(CapabilityPortalState.PORTAL_STATE, null).setInPortal(false);
                    
                    toGetRidOf.add(entity);
                }
            }
        }
        
        TRACKED.removeAll(toGetRidOf);
    }
    
}
