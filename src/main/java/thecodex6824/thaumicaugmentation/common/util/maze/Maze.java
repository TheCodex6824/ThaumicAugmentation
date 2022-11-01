/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

public class Maze {

    protected int width;
    protected int length;
    protected MazeCell[] cells;
    
    public Maze(int mazeWidth, int mazeLength, MazeCell[] mazeCells) {
        width = mazeWidth;
        length = mazeLength;
        cells = mazeCells;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getLength() {
        return length;
    }
    
    public MazeCell getCell(int x, int z) {
        return cells[z * width + x];
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int z = 0; z < length; ++z) {
            for (int x = 0; x < width; ++x) {
                MazeCell cell = cells[z * width + x];
                if (cell.getNumWalls() == 4)
                    result.append(' ');
                else if (cell.getNumWalls() == 3) {
                    EnumFacing open = null;
                    for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                        if (!cell.hasWall(dir)) {
                            open = dir;
                            break;
                        }
                    }
                    
                    if (open == EnumFacing.NORTH)
                        result.append('\u2575');
                    else if (open == EnumFacing.EAST)
                        result.append('\u2576');
                    else if (open == EnumFacing.SOUTH)
                        result.append('\u2577');
                    else
                        result.append('\u2574');
                }
                else if (cell.getNumWalls() == 2) {
                    EnumFacing open1 = null;
                    EnumFacing open2 = null;
                    for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                        if (!cell.hasWall(dir)) {
                            if (open1 == null)
                                open1 = dir;
                            else {
                                open2 = dir;
                                break;
                            }
                        }
                    }
                    
                    if (open1.getOpposite() == open2) {
                        if (open1.getAxis() == Axis.X)
                            result.append('\u2500');
                        else
                            result.append('\u2502');
                    }
                    else {
                        int angle1 = (int) open1.getHorizontalAngle();
                        int angle2 = (int) open2.getHorizontalAngle();
                        int temp = Math.min(angle1, angle2);
                        if (temp == angle2) {
                            angle2 = angle1;
                            angle1 = temp;
                        }
                        
                        if (angle1 == 0 && angle2 == 90)
                            result.append('\u2510');
                        else if (angle1 == 90 && angle2 == 180)
                            result.append('\u2518');
                        else if (angle1 == 180 && angle2 == 270)
                            result.append('\u2514');
                        else
                            result.append('\u250c');
                    }
                }
                else if (cell.getNumWalls() == 1) {
                    EnumFacing closed = null;
                    for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                        if (cell.hasWall(dir)) {
                            closed = dir;
                            break;
                        }
                    }
                    
                    if (closed == EnumFacing.NORTH)
                        result.append('\u252c');
                    else if (closed == EnumFacing.EAST)
                        result.append('\u2524');
                    else if (closed == EnumFacing.SOUTH)
                        result.append('\u2534');
                    else
                        result.append('\u251c');
                }
                else
                    result.append('\u253c');
            }
            
            if (z < length - 1)
                result.append('\n');
        }
        
        return result.toString();
    }
    
}
