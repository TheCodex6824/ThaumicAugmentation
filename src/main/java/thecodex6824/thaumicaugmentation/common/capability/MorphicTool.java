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

package thecodex6824.thaumicaugmentation.common.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import thecodex6824.thaumicaugmentation.api.item.IMorphicTool;

public class MorphicTool implements IMorphicTool, INBTSerializable<NBTTagCompound>{

    protected ItemStack functional;
    protected ItemStack display;
    
    public MorphicTool() {
        functional = ItemStack.EMPTY;
        display = ItemStack.EMPTY;
    }
    
    @Override
    public void setFunctionalStack(ItemStack stack) {
        functional = stack.copy();
    }
    
    @Override
    public ItemStack getFunctionalStack() {
        return functional;
    }
    
    @Override
    public void setDisplayStack(ItemStack stack) {
        display = stack.copy();
    }
    
    @Override
    public ItemStack getDisplayStack() {
        return display;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("functional", NBT.TAG_COMPOUND))
            functional = new ItemStack(nbt.getCompoundTag("functional"));
        if (nbt.hasKey("display", NBT.TAG_COMPOUND))
            display = new ItemStack(nbt.getCompoundTag("display"));
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (!functional.isEmpty())
            tag.setTag("functional", functional.serializeNBT());
        if (!display.isEmpty())
            tag.setTag("display", display.serializeNBT());
        
        return tag;
    }
    
}
