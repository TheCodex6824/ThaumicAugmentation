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

package thecodex6824.thaumicaugmentation.api.item;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

public class ImpetusLinker implements IImpetusLinker, INBTSerializable<NBTTagCompound> {

    protected DimensionalBlockPos origin;
    
    public ImpetusLinker() {
        origin = DimensionalBlockPos.INVALID;
    }
    
    public ImpetusLinker(DimensionalBlockPos startOrigin) {
        origin = startOrigin;
    }
    
    @Override
    public DimensionalBlockPos getOrigin() {
        return origin;
    }
    
    @Override
    public void setOrigin(DimensionalBlockPos newOrigin) {
        origin = newOrigin;
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (!origin.isInvalid())
            tag.setIntArray("pos", origin.toArray());
        
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("pos", NBT.TAG_INT_ARRAY))
            origin = new DimensionalBlockPos(nbt.getIntArray("pos"));
        else
            origin = DimensionalBlockPos.INVALID;
    }
    
}
