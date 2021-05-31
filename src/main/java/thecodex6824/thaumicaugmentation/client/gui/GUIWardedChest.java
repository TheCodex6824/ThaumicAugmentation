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

package thecodex6824.thaumicaugmentation.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.common.container.ContainerWardedChest;
import thecodex6824.thaumicaugmentation.common.tile.TileWardedChest;

public class GUIWardedChest extends GuiContainer {

    protected static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
    protected InventoryPlayer inv;

    public GUIWardedChest(ContainerWardedChest c, InventoryPlayer i) {
        super(c);
        inv = i;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        int width = (this.width - this.xSize) / 2;
        int height = (this.height - this.ySize) / 2;
        drawTexturedModalRect(width, height, 0, 0, xSize, 3 * 18 + 17);
        drawTexturedModalRect(width, height + 3 * 18 + 17, 0, 126, xSize, 96);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        TileWardedChest tile = ((ContainerWardedChest) inventorySlots).getTile();
        String name = "";
        if (tile.hasCustomName())
            name = tile.getCustomName();
        else
            name = I18n.format("tile." + TABlocks.WARDED_CHEST.getRegistryName().toString().replace(':', '.') + ".name");
        
        fontRenderer.drawString(name, 8, 6, 0x404040);
        fontRenderer.drawString(inv.getDisplayName().getUnformattedText(), 8, ySize - 96 + 4, 0x404040);
    }

}
