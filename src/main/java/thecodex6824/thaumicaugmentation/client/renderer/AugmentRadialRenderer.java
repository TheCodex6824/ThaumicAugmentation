package thecodex6824.thaumicaugmentation.client.renderer;

import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import thaumcraft.client.lib.UtilsFX;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.AugmentConfiguration;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentConfigurationStorage;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentConfigurationStorage;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.common.network.PacketApplyAugmentConfiguration;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public final class AugmentRadialRenderer {

    private static final ResourceLocation RADIAL_TEXTURE_1 = new ResourceLocation(ThaumicAugmentationAPI.MODID,
            "textures/gui/augment_radial1.png");
    private static final ResourceLocation RADIAL_TEXTURE_2 = new ResourceLocation(ThaumicAugmentationAPI.MODID,
            "textures/gui/augment_radial2.png");
    
    private static float radialScale = 0.0F;
    private static long lastTime = 0;
    private static boolean enabledLastTick = false;
    private static int lastConfig = -1;
    
    private AugmentRadialRenderer() {}
    
    private static void renderRadialHud(IAugmentConfigurationStorage storage, double resX, double resY, long time, float partialTicks) {
        lastConfig = -1;
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack held = mc.player.getHeldItemMainhand();
        if (!held.hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null))
            held = mc.player.getHeldItemOffhand();
        
        IAugmentableItem augmentable = held.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        List<AugmentConfiguration> configs = storage.getAllConfigurationsForItem(held);
        if (augmentable != null && !configs.isEmpty()) {
            GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
            GlStateManager.pushMatrix();
            GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0, resX, resY, 0.0, 1000.0, 3000.0);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            
            GlStateManager.pushMatrix();
            
            GlStateManager.translate(Math.floor(resX / 2), Math.floor(resY / 2), 0.0);
            
            float width = 16.0F + configs.size() * 2.5F;
            
            mc.renderEngine.bindTexture(RADIAL_TEXTURE_1);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(partialTicks + (mc.player.ticksExisted % 720) / 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            UtilsFX.renderQuadCentered(1, 1, 0, width * 2.75F * radialScale, 0.5F, 0.5F, 0.5F, 200, 771, 0.5F);
            
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
            GlStateManager.popMatrix();
            
            mc.renderEngine.bindTexture(RADIAL_TEXTURE_2);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-(partialTicks + (mc.player.ticksExisted % 720) / 2.0F), 0.0F, 0.0F, 1.0F);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            UtilsFX.renderQuadCentered(1, 1, 0, width * 2.55F * radialScale, 0.5F, 0.5F, 0.5F, 200, 771, 0.5F);
            
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
            GlStateManager.popMatrix();
            
            GlStateManager.scale(radialScale, radialScale, radialScale);
            
            float currentRot = -90.0F * radialScale;
            float pieSlice = 360.0F / configs.size();
            
            // TODO this is ugly, move mouse logic elsewhere
            int mouseX = (int) (Mouse.getEventX() * resX / mc.displayWidth);
            int mouseY = (int) (resY - Mouse.getEventY() * resY / mc.displayHeight - 1.0D);
            int mouseButton = Mouse.getEventButton();
            for (int c = 0; c < configs.size(); ++c) {
                AugmentConfiguration config = configs.get(c);
                double xx = (MathHelper.cos((float) Math.toRadians(currentRot)) * width);
                double yy = (MathHelper.sin((float) Math.toRadians(currentRot)) * width);
                currentRot += pieSlice;
              
                GlStateManager.pushMatrix();
                GlStateManager.translate(Math.floor(xx), Math.floor(yy), 100.0);
                //GlStateManager.scale(((Float)this.fociScale.get(key)).floatValue(), ((Float)this.fociScale.get(key)).floatValue(), ((Float)this.fociScale.get(key)).floatValue());
                GlStateManager.enableRescaleNormal();
                // TODO exclude air
                for (int i = 0; i < config.getAugmentConfig().size(); ++i) {
                    ItemStack item = config.getAugment(i);
                    UtilsFX.renderItemInGUI(-8, -8 - i * 8, 100, item);
                }
                
                GlStateManager.disableRescaleNormal();
                GlStateManager.popMatrix();
                if (ThaumicAugmentation.proxy.isAugmentRadialKeyDown()) {
                    int mx = (int)(mouseX - resX / 2.0 - xx);
                    int my = (int)(mouseY - resY / 2.0 - yy);
                
                    if (mx >= -10 && mx <= 10 && my >= -10 && my <= 10) {
                        if (mouseButton == 0) {
                            TANetwork.INSTANCE.sendToServer(new PacketApplyAugmentConfiguration(c));
                            ThaumicAugmentation.proxy.setAugmentRadialKeyDown(false);
                            lastConfig = -1;
                        }
                        else
                            lastConfig = c;
                    }
                }
            } 
            
            GlStateManager.popMatrix();
            
            // TODO show name of config once it is implemented
            
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    public static void renderAugmentRadial(RenderGameOverlayEvent event) {
        if (ThaumicAugmentation.proxy.isAugmentRadialKeyDown() || radialScale > 0.0F) {
            long time = Minecraft.getSystemTime();
            Minecraft mc = Minecraft.getMinecraft();
            IAugmentConfigurationStorage storage = mc.player.getCapability(
                    CapabilityAugmentConfigurationStorage.AUGMENT_CONFIGURATION_STORAGE, null);
            if (storage != null) {
                if (ThaumicAugmentation.proxy.isAugmentRadialKeyDown()) {
                    if (mc.currentScreen != null) {
                        ThaumicAugmentation.proxy.setAugmentRadialKeyDown(false);
                        mc.setIngameFocus();
                        mc.setIngameNotInFocus();
                        return;
                    }
                    
                    if (radialScale == 0.0F) {
                        mc.inGameHasFocus = false;
                        mc.mouseHelper.ungrabMouseCursor();
                    }
                }
                else {
                    if (radialScale >= 1.0F && lastConfig != -1) {
                        TANetwork.INSTANCE.sendToServer(new PacketApplyAugmentConfiguration(lastConfig));
                        ThaumicAugmentation.proxy.setAugmentRadialKeyDown(false);
                        lastConfig = -1;
                        enabledLastTick = true;
                    }
                    
                    if (mc.currentScreen == null && enabledLastTick) {
                        if (Display.isActive()) {
                            if (!mc.inGameHasFocus) {
                                mc.inGameHasFocus = true;
                                mc.mouseHelper.grabMouseCursor();
                            } 
                        }
                        
                        enabledLastTick = false;
                    } 
                }
                
                renderRadialHud(storage, event.getResolution().getScaledWidth_double(),
                        event.getResolution().getScaledHeight_double(), time, event.getPartialTicks());
                if (time > lastTime) {
                    if (!ThaumicAugmentation.proxy.isAugmentRadialKeyDown())
                        radialScale = MathHelper.clamp(radialScale - (time - lastTime) / 150.0F, 0.0F, 1.0F);
                    else if (radialScale < 1.0F)
                        radialScale = MathHelper.clamp(radialScale + (time - lastTime) / 150.0F, 0.0F, 1.0F);
                    
                    enabledLastTick = ThaumicAugmentation.proxy.isAugmentRadialKeyDown();
                }
            } 
            
            lastTime = time;
        }
    }
    
}
