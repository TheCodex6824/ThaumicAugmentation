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

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.client.gui.component.ButtonToggle;
import thecodex6824.thaumicaugmentation.common.container.ContainerAutocaster;
import thecodex6824.thaumicaugmentation.common.entity.EntityAutocaster;
import thecodex6824.thaumicaugmentation.common.network.PacketInteractGUI;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class GUIAutocaster extends GuiContainer {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/gui/autocaster.png");
    
    public GUIAutocaster(ContainerAutocaster c) {
        super(c);
        xSize = 175;
        ySize = 232;
    }
    
    @Override
    public void initGui() {
        super.initGui();
        EntityAutocaster e = ((ContainerAutocaster) inventorySlots).getEntity();
        buttonList.add(new ButtonToggle(0, guiLeft + 80, guiTop, 8, 8, e.getTargetAnimals()) {
            @Override
            public String getLabel() {
                return new TextComponentTranslation("button.turretfocus.1").getFormattedText();
            }
            
            @Override
            public void onToggle(boolean newState) {
                TANetwork.INSTANCE.sendToServer(new PacketInteractGUI(id, newState ? 1 : 0));
            }
        });
        buttonList.add(new ButtonToggle(1, guiLeft + 80, guiTop + 14, 8, 8, e.getTargetMobs()) {
            @Override
            public String getLabel() {
                return new TextComponentTranslation("button.turretfocus.2").getFormattedText();
            }
            
            @Override
            public void onToggle(boolean newState) {
                TANetwork.INSTANCE.sendToServer(new PacketInteractGUI(id, newState ? 1 : 0));
            }
        });
        buttonList.add(new ButtonToggle(2, guiLeft + 80, guiTop + 28, 8, 8, e.getTargetPlayers()) {
            @Override
            public String getLabel() {
                return new TextComponentTranslation("button.turretfocus.3").getFormattedText();
            }
            
            @Override
            public void onToggle(boolean newState) {
                TANetwork.INSTANCE.sendToServer(new PacketInteractGUI(id, newState ? 1 : 0));
            }
        });
        buttonList.add(new ButtonToggle(3, guiLeft + 80, guiTop + 42, 8, 8, e.getTargetFriendly()) {
            @Override
            public String getLabel() {
                return new TextComponentTranslation("button.turretfocus.4").getFormattedText();
            }
            
            @Override
            public void onToggle(boolean newState) {
                TANetwork.INSTANCE.sendToServer(new PacketInteractGUI(id, newState ? 1 : 0));
            }
        });
        buttonList.add(new ButtonToggle(4, guiLeft + 80, guiTop + 56, 8, 8, e.getRedstoneControl()) {
            @Override
            public String getLabel() {
                return new TextComponentTranslation(ThaumicAugmentationAPI.MODID + ".gui.redstone").getFormattedText();
            }
            
            @Override
            public void onToggle(boolean newState) {
                TANetwork.INSTANCE.sendToServer(new PacketInteractGUI(id, newState ? 1 : 0));
            }
        });
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
        ContainerAutocaster container = (ContainerAutocaster) inventorySlots;
        int health = (int) (39.0F * container.getEntity().getHealth() / container.getEntity().getMaxHealth());
        drawTexturedModalRect(x + 24, y + 59, 192, 48, health, 6);
    }
    
}
