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

package thecodex6824.thaumicaugmentation.api.warded;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * Inventory manager for warded inventories. The API is very similar to IItemHandler,
 * but it's a separate interface so that other things that are accessing IItemHandler capabilities
 * don't access warded inventories.
 * @author TheCodex6824
 */
public interface IWardedInventory {
    
    public ItemStack extractItem(int slot, int amount, boolean simulate);
    
    public int getSlotLimit(int slot);
    
    public int getSlots();
    
    public ItemStack getStackInSlot(int slot);
    
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
    
    public IItemHandler getItemHandler();
    
}
