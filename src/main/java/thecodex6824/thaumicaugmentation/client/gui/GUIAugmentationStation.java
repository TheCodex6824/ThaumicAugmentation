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

package thecodex6824.thaumicaugmentation.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.client.gui.component.IHoverTextLocationAware;
import thecodex6824.thaumicaugmentation.common.container.ContainerAugmentationStation;

public class GUIAugmentationStation extends GuiContainer {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/gui/augmentation_station.png");
    
    public GUIAugmentationStation(ContainerAugmentationStation c) {
        super(c);
        xSize = 256;
        ySize = 204;
    }
    
    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new ButtonAugmentDrawerPage(0, guiLeft + 193, guiTop + 189));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        for (GuiButton b : buttonList) {
            if (b.isMouseOver()) {
                String s = b.displayString;
                if (b instanceof IHoverTextLocationAware) {
                    s = ((IHoverTextLocationAware) b).getHoverText(mouseX, mouseY);
                }
                
                drawHoveringText(s, mouseX, mouseY);
                break;
            }
        }
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        int width = (this.width - this.xSize) / 2;
        int height = (this.height - this.ySize) / 2;
        drawTexturedModalRect(width, height, 0, 0, xSize, ySize);
        for (int i : ((ContainerAugmentationStation) inventorySlots).getAugmentSlotIndices()) {
            Slot s = inventorySlots.inventorySlots.get(i);
            drawTexturedModalRect(guiLeft + s.xPos - 7, guiTop + s.yPos - 7, 81, 214, 32, 32);
        }
    }
    
}
