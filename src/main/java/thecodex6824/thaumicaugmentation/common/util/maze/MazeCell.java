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

package thecodex6824.thaumicaugmentation.common.util.maze;

import java.util.Arrays;

import net.minecraft.util.EnumFacing;

public class MazeCell {

    protected boolean[] walls;
    protected int wallCount;
    
    public MazeCell() {
        walls = new boolean[4];
        Arrays.fill(walls, true);
        wallCount = 4;
    }
    
    public void setWall(EnumFacing dir, boolean hasWall) {
        if (hasWall != hasWall(dir)) {
            walls[dir.getHorizontalIndex()] = hasWall;
            wallCount += hasWall ? 1 : -1;
        }
    }
    
    public boolean hasWall(EnumFacing dir) {
        return walls[dir.getHorizontalIndex()];
    }
    
    public int getNumWalls() {
        return wallCount;
    }
    
}
