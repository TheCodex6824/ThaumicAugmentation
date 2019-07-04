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

import net.minecraft.util.math.BlockPos;

public interface IDimensionalFracture {

    public void setLinkedPosition(BlockPos pos);

    public BlockPos getLinkedPosition();

    public void setLinkedDimension(int dim);

    public int getLinkedDimension();
    
    public void setLinkLocated();
    
    public void setLinkLocated(boolean located);

    public boolean wasLinkLocated();

    public void setLinkInvalid();
    
    public void setLinkInvalid(boolean invalid);

    public boolean isLinkInvalid();

    public void open();
    
    public void open(boolean skipTransition);
    
    public int getOpeningDuration();
    
    public void close();

    public boolean isOpening();
    
    public boolean isOpen();
    
    public long getTimeOpened();
    
}
