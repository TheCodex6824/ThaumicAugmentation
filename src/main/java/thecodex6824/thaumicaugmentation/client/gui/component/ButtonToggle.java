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

package thecodex6824.thaumicaugmentation.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.ResourceLocation;

public class ButtonToggle extends GuiButton {

    protected static final ResourceLocation TEXTURE = new ResourceLocation("thaumcraft", "textures/gui/gui_turret_advanced.png");
    
    protected boolean currentState;
    
    public ButtonToggle(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, "");
    }
    
    public ButtonToggle(int id, int x, int y, int width, int height, boolean initialState) {
        super(id, x, y, width, height, "");
        currentState = initialState;
    }
    
    public void onToggle(boolean newState) {}
    
    public String getLabel() {
        return "";
    }
    
    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (enabled && visible && mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height) {
            onToggle(!currentState);
            currentState = !currentState;
            return true;
        }
        
        return false;
    }
    
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            FontRenderer text = mc.fontRenderer;
            mc.renderEngine.bindTexture(TEXTURE);
            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            drawTexturedModalRect(x, y, 192, 16, 8, 8);
            if (currentState)
                drawTexturedModalRect(x, y, 192, 24, 8, 8);
            
            String label = getLabel();
            if (!label.isEmpty())
                text.drawStringWithShadow(label, x + 12, y, 0xFFFFFF);
        }
    }
    
}
