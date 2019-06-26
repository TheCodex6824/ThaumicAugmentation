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

package thecodex6824.thaumicaugmentation.common.research;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumcraft.api.research.IScanThing;

public class ScanTool implements IScanThing {

    protected String research;
    protected String type;
    protected int min;
    
    public ScanTool(String researchKey, String toolType, int minLevel) {
        research = researchKey;
        type = toolType;
        min = minLevel;
    }
    
    @Override
    public boolean checkThing(EntityPlayer player, Object thing) {
        if (thing instanceof EntityItem) {
            ItemStack stack = ((EntityItem) thing).getItem();
            return stack.getItem().getHarvestLevel(stack, type, player, Blocks.STONE.getDefaultState()) >= min;
        }
        else if (thing instanceof ItemStack) {
            ItemStack stack = (ItemStack) thing;
            return stack.getItem().getHarvestLevel(stack, type, player, Blocks.STONE.getDefaultState()) >= min;
        }
        else if (thing instanceof Item) {
            Item item = (Item) thing;
            return item.getHarvestLevel(new ItemStack(item), type, player, Blocks.STONE.getDefaultState()) >= min;
        }
        else
            return false;
    }
    
    @Override
    public String getResearchKey(EntityPlayer player, Object thing) {
        return research;
    }
    
    @Override
    public void onSuccess(EntityPlayer player, Object object) {}
    
}
