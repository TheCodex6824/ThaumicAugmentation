/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.api.util;

import com.google.common.base.MoreObjects;
import net.minecraft.util.math.BlockPos;

public class DimensionalBlockPos {

    public static final DimensionalBlockPos INVALID = new DimensionalBlockPos(0, 0, 0, 0) {
        
        @Override
        public BlockPos getPos() {
            throw new UnsupportedOperationException("Attempted to get position of invalid DimensionalBlockPos");
        }
        
        @Override
        public int getDimension() {
            throw new UnsupportedOperationException("Attempted to get dimension of invalid DimensionalBlockPos");
        }
        
        @Override
        public int[] toArray() {
            throw new UnsupportedOperationException("Attempted to convert invalid DimensionalBlockPos to an array");
        }
        
        @Override
        public boolean isInvalid() {
            return true;
        }
        
        @Override
        public String toString() {
            return "InvalidDimensionalBlockPos{}";
        }
        
        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }
        
        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
        
    };
    
    protected BlockPos pos;
    protected int dim;
    
    public DimensionalBlockPos(int x, int y, int z, int dimension) {
        this(new BlockPos(x, y, z), dimension);
    }
    
    public DimensionalBlockPos(BlockPos position, int dimension) {
        pos = position;
        dim = dimension;
    }
    
    public DimensionalBlockPos(DimensionalBlockPos toCopy) {
        pos = toCopy.getPos().toImmutable();
        dim = toCopy.getDimension();
    }
    
    public DimensionalBlockPos(int[] components) {
        if (components.length != 4)
            throw new ArrayIndexOutOfBoundsException("DimensionalBlockPos component array has wrong size");
        
        pos = new BlockPos(components[0], components[1], components[2]);
        dim = components[3];
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    public int getDimension() {
        return dim;
    }
    
    public int[] toArray() {
        return new int[] {pos.getX(), pos.getY(), pos.getZ(), dim};
    }
    
    public boolean isInvalid() {
        return false;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ()).add("dim", dim).toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == DimensionalBlockPos.class) {
            return ((DimensionalBlockPos) obj).getPos().equals(pos) && 
                    ((DimensionalBlockPos) obj).getDimension() == dim;
        }
        else
            return false;
    }
    
    @Override
    public int hashCode() {
        return pos.hashCode() * 31 + dim;
    }
    
}
