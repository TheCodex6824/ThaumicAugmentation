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

package thecodex6824.thaumicaugmentation.api.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.Event.HasResult;

/**
 * Event fired whenever a player attempts to interact with a "complex" warded block. For performance reasons, this event 
 * will NOT be fired for blocks affected by the warding focus. This is intended for mods that have "teams" or other 
 * situtations where players should be able to access each other's things.
 * @author TheCodex6824
 */
@HasResult
@Cancelable
public class WardedTilePermissionEvent extends Event {

    protected Result result;
    protected World world;
    protected EntityLivingBase entity;
    protected BlockPos position;
    protected IBlockState state;
    protected boolean allowed;

    public WardedTilePermissionEvent(World w, BlockPos pos, IBlockState s, EntityLivingBase e, boolean info) {
        result = Result.DEFAULT;
        world = w;
        entity = e;
        position = pos;
        state = s;
        allowed = info;
    }

    /**
     * Returns the world the warded block is in.
     * @return The world the warded block is in
     */
    public World getWorld() {
        return world;
    }

    /**
     * Returns the entity interacting with the warded block.
     * @return The entity interacting with the warded block
     */
    public EntityLivingBase getEntity() {
        return entity;
    }
    
    /**
     * Returns the entity interacting with the warded block.
     * @return The entity interacting with the warded block
     */
    public EntityLivingBase getEntityLiving() {
        return entity;
    }

    /**
     * Returns the position of the warded block.
     * @return The position of the warded block
     */
    public BlockPos getPos() {
        return position;
    }

    /**
     * Returns the blockstate of the warded block.
     * @return The blockstate of the warded block
     */
    public IBlockState getState() {
        return state;
    }

    /**
     * Returns if the player would normally be allowed to access this warded tile.
     * @return If the player would normally be allowed
     */
    public boolean isAllowed() {
        return allowed;
    }

    /**
     * Sets the result of this event. A result of ALLOW will let
     * the player interact with the warded block, even if they would normally
     * not have permission to do so. A result of DENY will always disallow players
     * from interacting with the warded block, even if they normally would be able to.
     * A result of DEFAULT will fall back to the normal permission behavior.
     * @param value The result this event should use
     */
    @Override
    public void setResult(Result value) {
        result = value;
    }

    /**
     * Returns the result of this event (DEFAULT by default).
     * @return The result of this event
     * @see WardedTilePermissionEvent#setResult(Result) setResult
     */
    @Override
    public Result getResult() {
        return result;
    }

}
