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

package thecodex6824.thaumicaugmentation.api.world.capability;

import java.lang.ref.WeakReference;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;

public class FractureLocation implements IFractureLocation {

    private boolean hasFracture;
    private BlockPos position;
    private WeakReference<Chunk> chunk;
    
    public FractureLocation() {
        hasFracture = false;
        position = new BlockPos(0, 0, 0);
    }
    
    public FractureLocation(Chunk c) {
        this();
        chunk = new WeakReference<>(c);
    }
    
    @Override
    public void setChunk(Chunk c) {
        chunk = new WeakReference<>(c);
    }
    
    @Override
    public boolean hasFracture() {
        return hasFracture;
    }
    
    @Override
    public void setHasFracture(boolean fracture) {
        hasFracture = fracture;
        if (chunk != null && chunk.get() != null)
            chunk.get().markDirty();
    }
    
    @Override
    public BlockPos getFractureLocation() {
        return position;
    }
    
    @Override
    public void setFractureLocation(BlockPos pos) {
        position = pos.toImmutable();
        if (chunk != null && chunk.get() != null)
            chunk.get().markDirty();
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        hasFracture = nbt.getBoolean("hasFracture");
        if (hasFracture) {
            int[] coord = nbt.getIntArray("fracturePos");
            if (coord.length == 3)
                position = new BlockPos(coord[0], coord[1], coord[2]);
            else
                ThaumicAugmentation.getLogger().warn("A CapabilityFractureLocation instance contained invalid position data!");
        }
        
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("hasFracture", hasFracture);
        if (hasFracture)
            tag.setIntArray("fracturePos", new int[] {position.getX(), position.getY(), position.getZ()});
        
        return tag;
    }
    
}
