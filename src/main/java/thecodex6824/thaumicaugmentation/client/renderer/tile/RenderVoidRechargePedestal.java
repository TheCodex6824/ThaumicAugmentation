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

package thecodex6824.thaumicaugmentation.client.renderer.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import thecodex6824.thaumicaugmentation.common.tile.TileVoidRechargePedestal;

public class RenderVoidRechargePedestal extends TileEntitySpecialRenderer<TileVoidRechargePedestal> {

    @Override
    public void render(TileVoidRechargePedestal te, double x, double y, double z, float partialTicks, int destroyStage,
            float alpha) {
        
        IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (inv != null) {
            ItemStack stack = inv.getStackInSlot(0);
            if (!stack.isEmpty()) {
                Entity rv = Minecraft.getMinecraft().getRenderViewEntity();
                if (rv == null)
                    rv = Minecraft.getMinecraft().player;
                
                RenderHelper.enableStandardItemLighting();
                GlStateManager.pushMatrix();
                GlStateManager.enableLighting();
                GlStateManager.translate(x + 0.5, y + 0.95, z + 0.5);
                GlStateManager.scale(1.25F, 1.25F, 1.25F);
                GlStateManager.rotate(rv.ticksExisted + partialTicks % 360, 0.0F, 1.0F, 0.0F);
                Minecraft.getMinecraft().getRenderItem().renderItem(stack, TransformType.GROUND);
                GlStateManager.popMatrix();
            }
        }
    }
    
}
