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

package thecodex6824.thaumicaugmentation.api.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import thecodex6824.thaumicaugmentation.api.util.FluxRiftReconstructor;

public class RiftJar implements IRiftJar, INBTSerializable<NBTTagCompound> {

    protected FluxRiftReconstructor rift;
    
    public RiftJar() {
        rift = new FluxRiftReconstructor(0, 0);
    }
    
    public RiftJar(int seed, int size) {
        rift = new FluxRiftReconstructor(seed, size);
    }
    
    @Override
    public boolean hasRift() {
        return rift.getRiftSize() > 0;
    }
    
    @Override
    public FluxRiftReconstructor getRift() {
        return rift;
    }
    
    @Override
    public void setRift(FluxRiftReconstructor newRift) {
        rift = newRift;
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("seed", rift.getRiftSeed());
        tag.setInteger("size", rift.getRiftSize());
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        rift = new FluxRiftReconstructor(nbt.getInteger("seed"), nbt.getInteger("size"));
    }
    
}
