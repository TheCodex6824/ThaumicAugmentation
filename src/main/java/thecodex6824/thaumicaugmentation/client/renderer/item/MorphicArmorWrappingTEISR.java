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

package thecodex6824.thaumicaugmentation.client.renderer.item;

import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import thecodex6824.thaumicaugmentation.common.util.MorphicArmorHelper;

public class MorphicArmorWrappingTEISR extends MorphicWrappingTEISR {
    
    protected TileEntityItemStackRenderer wrapped;
    
    public MorphicArmorWrappingTEISR(TileEntityItemStackRenderer toWrap) {
        wrapped = toWrap;
    }
    
    @Override
    public void renderByItemWrapped(ItemStack stack) {
        wrapped.renderByItem(stack);
    }
    
    @Override
    public void renderByItem(ItemStack stack) {
        if (MorphicArmorHelper.hasMorphicArmor(stack)) {
            ItemStack item = MorphicArmorHelper.getMorphicArmor(stack);
            TileEntityItemStackRenderer r = item.getItem().getTileEntityItemStackRenderer();
            if (r instanceof MorphicWrappingTEISR)
                ((MorphicWrappingTEISR) r).renderByItemWrapped(item);
            else
                r.renderByItem(item);
        }
        else
            renderByItemWrapped(stack);
    }
    
}
