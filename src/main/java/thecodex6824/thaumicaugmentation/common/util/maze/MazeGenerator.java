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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.util.EnumFacing;

public class MazeGenerator {

    protected int width;
    protected int length;
    
    public MazeGenerator() {
        width = 3;
        length = 3;
    }
    
    public MazeGenerator withSize(int mazeWidth, int mazeLength) {
        width = mazeWidth;
        length = mazeLength;
        return this;
    }
    
    public Maze generate(Random rand) {
        MazeCell[] cells = new MazeCell[width * length];
        for (int i = 0; i < cells.length; ++i)
            cells[i] = new MazeCell();
        
        ImmutableList<Integer> directionOffsets = ImmutableList.of(-length, 1, length, -1);
        ArrayList<Integer> dirChoices = new ArrayList<>();
        
        IntOpenHashSet visited = new IntOpenHashSet();
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        int initial = rand.nextInt(cells.length);
        visited.add(initial);
        stack.push(initial);
        while (!stack.isEmpty()) {
            int current = stack.pop();
            int neighborDir = 0;
            dirChoices.clear();
            dirChoices.addAll(directionOffsets);
            while (!dirChoices.isEmpty()) {
                int i = rand.nextInt(dirChoices.size());
                int dir = dirChoices.get(i);
                int check = current + dir;
                if (dir == -1 || dir == 1) {
                    if (check > -1 && check < cells.length && check / length == current / length &&
                            !visited.contains(check)) {
                        
                        neighborDir = dir;
                        break;
                    }
                }
                else {
                    if (check > -1 && check < cells.length && check % length == current % length &&
                            !visited.contains(check)) {
                        
                        neighborDir = dir;
                        break;
                    }
                }
                
                dirChoices.remove(i);
            }
            
            if (neighborDir != 0) {
                int otherIndex = current + neighborDir;
                MazeCell cell = cells[current];
                MazeCell other = cells[otherIndex];
                if (neighborDir == -1) {
                    cell.setWall(EnumFacing.WEST, false);
                    other.setWall(EnumFacing.EAST, false);
                }
                else if (neighborDir == 1) {
                    cell.setWall(EnumFacing.EAST, false);
                    other.setWall(EnumFacing.WEST, false);
                }
                else if (neighborDir == -length) {
                    cell.setWall(EnumFacing.NORTH, false);
                    other.setWall(EnumFacing.SOUTH, false);
                }
                else {
                    cell.setWall(EnumFacing.SOUTH, false);
                    other.setWall(EnumFacing.NORTH, false);
                }
                
                stack.push(current);
                stack.push(otherIndex);
                visited.add(otherIndex);
            }
        }
        
        return new Maze(width, length, cells);
    }
    
}
