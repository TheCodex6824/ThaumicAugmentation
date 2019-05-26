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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.Event.HasResult;

/**
 * Event fired whenever a player attempts to interact with a warded block. Intended for mods that
 * have "teams" or other situtations where players should be able to access each other's things.
 * @author TheCodex6824
 *
 */
@HasResult
@Cancelable
public class WardedBlockPermissionEvent extends Event {

    protected Result result;
    protected World world;
    protected EntityPlayer player;
    protected BlockPos position;
    protected IBlockState state;
    protected boolean informative;

    public WardedBlockPermissionEvent(World w, BlockPos pos, IBlockState s, EntityPlayer p, boolean info) {
        result = Result.DEFAULT;
        world = w;
        player = p;
        position = pos;
        state = s;
        informative = info;
    }

    /**
     * Returns the world the warded block is in.
     * @return The world the warded block is in
     */
    public World getWorld() {
        return world;
    }

    /**
     * Returns the player interacting with the warded block.
     * @return The player interacting with the warded block
     */
    public EntityPlayer getPlayer() {
        return player;
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
     * Returns if this event is informative, or otherwise is solely for other mods
     * to know when a warded block is used. If this is true, then any permission
     * returned in this event is ignored, and the interaction will still take place
     * if the event is cancelled.
     * @return If this event is informative
     */
    public boolean isInformative() {
        return informative;
    }

    /**
     * Sets the result of this event. A result of ALLOW will let
     * the player interact with the warded block, even if they would normally
     * not have permission to do so. A result of DENY will always disallow players
     * from interacting with the warded block, even if they normally would be able to.
     * A result of DEFAULT will fall back to the normal permission behavior. All results
     * will be ignored if {@link WardedBlockPermissionEvent#isInformative isInformative} is true.
     * @param value The result this event should use
     */
    @Override
    public void setResult(Result value) {
        result = value;
    }

    /**
     * Returns the result of this event (DEFAULT by default).
     * @return The result of this event
     * @see WardedBlockPermissionEvent#setResult(Result) setResult
     */
    @Override
    public Result getResult() {
        return result;
    }

}
