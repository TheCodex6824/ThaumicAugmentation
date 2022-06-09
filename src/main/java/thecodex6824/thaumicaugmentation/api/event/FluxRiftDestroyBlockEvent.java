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
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import thaumcraft.common.entities.EntityFluxRift;

@Cancelable
public class FluxRiftDestroyBlockEvent extends EntityEvent {

    protected final BlockPos pos;
    protected final IBlockState state;
    
    public FluxRiftDestroyBlockEvent(EntityFluxRift rift, BlockPos position, IBlockState destroyedState) {
        super(rift);
        pos = position;
        state = destroyedState;
    }
    
    public EntityFluxRift getRift() {
        return (EntityFluxRift) getEntity();
    }
    
    public BlockPos getPosition() {
        return pos;
    }
    
    public IBlockState getDestroyedBlock() {
        return state;
    }
    
}
