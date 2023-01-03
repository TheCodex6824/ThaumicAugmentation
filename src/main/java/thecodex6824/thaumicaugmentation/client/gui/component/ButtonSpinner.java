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

package thecodex6824.thaumicaugmentation.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.ResourceLocation;

public class ButtonSpinner extends GuiButton {

    protected static final ResourceLocation TEXTURE = new ResourceLocation("thaumcraft", "textures/gui/gui_base.png");
    
    public ButtonSpinner(int id, int x, int y, int width) {
        super(id, x, y, width, 10, "");
    }
    
    public void onDecrement() {}
    
    public void onIncrement() {}
    
    public String getLabel() {
        return "";
    }
    
    public String getDisplayedValue() {
        return "";
    }
    
    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (enabled && visible) {
            if (mouseX >= x && mouseY >= y && mouseX < x + 10 && mouseY < y + height) {
                onDecrement();
                return true;
            }
            else if (mouseX >= x + width && mouseY >= y && mouseX < x + width + 10 && mouseY < y + height) {
                onIncrement();
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            FontRenderer text = mc.fontRenderer;
            mc.renderEngine.bindTexture(TEXTURE);
            hovered = mouseX >= x && mouseY >= y && mouseX < x + width + 10 && mouseY < y + height;
            if (getHoverState(hovered) == 2)
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            else
                GlStateManager.color(0.9F, 0.9F, 0.9F, 0.9F);
            
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            drawTexturedModalRect(x, y, 20, 0, 10, 10);
            drawTexturedModalRect(x + width, y, 30, 0, 10, 10);
            
            String label = getLabel();
            if (!label.isEmpty())
                text.drawStringWithShadow(label, x + (width + 10) / 2.0F - text.getStringWidth(label) / 2.0F, y - text.FONT_HEIGHT - 1, 0xFFAA00);
            
            String display = getDisplayedValue();
            if (!display.isEmpty())
                text.drawStringWithShadow(display, x + (width + 10) / 2.0F - text.getStringWidth(display) / 2.0F, y + 1, 0xFFFFFF);
        }
    }
    
}
