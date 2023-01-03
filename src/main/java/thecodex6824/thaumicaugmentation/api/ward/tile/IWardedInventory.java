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

package thecodex6824.thaumicaugmentation.api.ward.tile;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

/*
 * Inventory manager for warded inventories. The API is very similar to IItemHandler,
 * but it's a separate interface so that other things that are accessing IItemHandler capabilities
 * don't access warded inventories.
 * @author TheCodex6824
 */
public interface IWardedInventory {
    
    @Nonnull
    ItemStack extractItem(int slot, int amount, boolean simulate);
    
    int getSlotLimit(int slot);
    
    int getSlots();
    
    @Nonnull
    ItemStack getStackInSlot(int slot);
    
    @Nonnull
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
    
    IItemHandler getItemHandler();
    
}
