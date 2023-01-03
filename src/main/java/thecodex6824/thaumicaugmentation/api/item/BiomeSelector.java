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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

public class BiomeSelector implements IBiomeSelector, INBTSerializable<NBTTagCompound> {

    protected ResourceLocation id;
    
    public BiomeSelector() {
        id = IBiomeSelector.EMPTY;
    }
    
    public BiomeSelector(ResourceLocation loc) {
        id = loc;
    }
    
    @Override
    public ResourceLocation getBiomeID() {
        return id;
    }
    
    @Override
    public void setBiomeID(ResourceLocation loc) {
        id = loc;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        id = new ResourceLocation(nbt.getString("biome"));
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("biome", id.toString());
        return tag;
    }
    
}
