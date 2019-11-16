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
import java.util.Collection;
import java.util.HashSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Constants.NBT;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;

/**
 * Default implementation for {@link IFractureLocations}.
 * @author TheCodex6824
 */
public class FractureLocations implements IFractureLocations, INBTSerializable<NBTTagCompound> {

    private HashSet<BlockPos> positions;
    private WeakReference<Chunk> chunk;
    
    public FractureLocations() {
        positions = new HashSet<>();
    }
    
    public FractureLocations(Chunk c) {
        this();
        chunk = new WeakReference<>(c);
    }
    
    @Override
    public void setChunk(Chunk c) {
        chunk = new WeakReference<>(c);
    }
    
    @Override
    public boolean hasFracture() {
        return !positions.isEmpty();
    }
    
    @Override
    public Collection<BlockPos> getFractureLocations() {
        return positions;
    }
    
    @Override
    public void addFractureLocation(BlockPos pos) {
        positions.add(pos.toImmutable());
        if (chunk != null && chunk.get() != null)
            chunk.get().markDirty();
    }
    
    @Override
    public void removeFractureLocation(BlockPos pos) {
        positions.remove(pos.toImmutable());
        if (chunk != null && chunk.get() != null)
            chunk.get().markDirty();
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        for (int i = 0; i < Integer.MAX_VALUE; ++i) {
            String str = Integer.toString(i);
            if (nbt.hasKey(str, NBT.TAG_COMPOUND)) {
                int[] coord = nbt.getCompoundTag(str).getIntArray("pos");
                if (coord.length == 3)
                    positions.add(new BlockPos(coord[0], coord[1], coord[2]));
                else {
                    ThaumicAugmentation.getLogger().warn("A CapabilityFractureLocation instance contained invalid position data!");
                    break;
                }
            }
            else
                break;
        }
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        BlockPos[] arr = positions.toArray(new BlockPos[positions.size()]);
        for (int i = 0; i < arr.length; ++i) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setIntArray("pos", new int[] {arr[i].getX(), arr[i].getY(), arr[i].getZ()});
            tag.setTag(Integer.toString(i), nbt);
        }
        
        return tag;
    }
    
}
