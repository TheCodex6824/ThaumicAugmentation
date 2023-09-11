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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.client.gui.component.IHoverTextLocationAware;
import thecodex6824.thaumicaugmentation.common.container.ContainerAugmentPlanner;
import thecodex6824.thaumicaugmentation.common.container.ContainerAugmentationStation;
import thecodex6824.thaumicaugmentation.common.network.PacketInteractGUI;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

@SuppressWarnings("deprecation")
public class GUIAugmentPlanner extends GuiContainer {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/gui/augmentation_station.png");
    
    protected static class ButtonConfigSelect extends GuiButton implements IHoverTextLocationAware {
        
        protected int value;
        
        public ButtonConfigSelect(int id, int x, int y) {
            super(id, x, y, 48, 10, "");
            height = 8;
            enabled = false;
        }
        
        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                drawTexturedModalRect(x, y, 17, 239 + (!enabled || value == 0 ? height : 0), width / 2, height);
                drawTexturedModalRect(x + width / 2, y, 17 + width / 2, 239 + (!enabled || value == 7 ? height : 0), width / 2, height);
                mouseDragged(mc, mouseX, mouseY);
            }
        }
        
        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (super.mousePressed(mc, mouseX, mouseY)) {
                if (value > 0 && mouseX >= x && mouseY >= y && mouseX <= x + width / 2 && mouseY < y + height) {
                    --value;
                    sync();
                    return true;
                }
                else if (value < 7 && mouseX >= x + width / 2 && mouseY >= y && mouseX < x + width && mouseY < y + height) {
                    ++value;
                    sync();
                    return true;
                }
            }
            
            return false;
        }
        
        protected void sync() {
            TANetwork.INSTANCE.sendToServer(new PacketInteractGUI(id, value));
        }
        
        @Override
        public String getHoverText(int mouseX, int mouseY) {
            if (mouseX <= x + width / 2)
                return I18n.translateToLocal("thaumicaugmentation.gui.config_prev");
            else
                return I18n.translateToLocal("thaumicaugmentation.gui.config_next");
        }
        
    }
    
    public GUIAugmentPlanner(ContainerAugmentPlanner c) {
        super(c);
        xSize = 176;
        ySize = 214;
    }
    
    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new ButtonConfigSelect(0, guiLeft + 14, guiTop + 6));
    }
    
    @Override
    public void updateScreen() {
        super.updateScreen();
        ContainerAugmentationStation station = (ContainerAugmentationStation) inventorySlots;
        for (GuiButton b : buttonList)
            b.enabled = !station.getAugmentableItem().isEmpty();
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        for (GuiButton b : buttonList) {
            if (b.isMouseOver()) {
                String s = b.displayString;
                if (b instanceof IHoverTextLocationAware)
                    s = ((IHoverTextLocationAware) b).getHoverText(mouseX, mouseY);
                
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
