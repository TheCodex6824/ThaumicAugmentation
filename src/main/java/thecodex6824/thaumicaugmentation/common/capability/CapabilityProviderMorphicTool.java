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

package thecodex6824.thaumicaugmentation.common.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import thecodex6824.thaumicaugmentation.api.item.CapabilityMorphicTool;

public class CapabilityProviderMorphicTool implements ICapabilitySerializable<NBTTagCompound> {

    private MorphicTool morphic;
    
    public CapabilityProviderMorphicTool(MorphicTool tool) {
        morphic = tool;
    }
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityMorphicTool.MORPHIC_TOOL)
            return true;
        else
            return morphic.getFunctionalStack().hasCapability(capability, facing);
    }
    
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityMorphicTool.MORPHIC_TOOL)
            return CapabilityMorphicTool.MORPHIC_TOOL.cast(morphic);
        else
            return morphic.getFunctionalStack().getCapability(capability, facing);
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        morphic.deserializeNBT(nbt);
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        return morphic.serializeNBT();
    }
    
}
