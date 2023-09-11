package thecodex6824.thaumicaugmentation.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.translation.I18n;
import thecodex6824.thaumicaugmentation.client.gui.component.IHoverTextLocationAware;
import thecodex6824.thaumicaugmentation.common.network.PacketInteractGUI;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

@SuppressWarnings("deprecation")
public class ButtonAugmentDrawerPage extends GuiButton implements IHoverTextLocationAware {

	protected int value;
    
    public ButtonAugmentDrawerPage(int id, int x, int y) {
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
            return I18n.translateToLocal("thaumicaugmentation.gui.augment_drawer_prev");
        else
            return I18n.translateToLocal("thaumicaugmentation.gui.augment_drawer_next");
    }
	
}
