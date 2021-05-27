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

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.client.gui.component.ButtonToggle;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.common.container.ContainerCelestialObserver;
import thecodex6824.thaumicaugmentation.common.entity.EntityCelestialObserver;
import thecodex6824.thaumicaugmentation.common.network.PacketInteractGUI;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class GUICelestialObserver extends GuiContainer {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/gui/celestial_observer.png");
    protected static final ResourceLocation SUN = new ResourceLocation("minecraft", "textures/environment/sun.png");
    protected static final ResourceLocation MOON = new ResourceLocation("minecraft", "textures/environment/moon_phases.png");
    
    // can't use TC's field as it is in the AuraThread itself server side
    // some of the lower values are fudged a bit to make them easier to see
    protected static final float[] AURA_STRENGTH = new float[] {32.0F, 19.2F, 13.3F, 7.4F, 2.0F, 7.4F, 13.3F, 19.2F};
    
    protected static Framebuffer fb;
    
    protected static final int FB_WIDTH = 256;
    protected static final int FB_HEIGHT = 256;
    
    public GUICelestialObserver(ContainerCelestialObserver c) {
        super(c);
        xSize = 176;
        ySize = 217;
        if (Minecraft.getMinecraft().gameSettings.fboEnable && !TAConfig.disableFramebuffers.getValue() && fb == null) {
            fb = new Framebuffer(FB_WIDTH, FB_HEIGHT, true);
            fb.setFramebufferColor(0.0F, 0.0F, 0.0F, 1.0F);
            fb.framebufferClear();
        }
    }
    
    @Override
    public void initGui() {
        super.initGui();
        EntityCelestialObserver e = ((ContainerCelestialObserver) inventorySlots).getEntity();
        buttonList.add(new ButtonToggle(0, guiLeft + 8, guiTop + 22, 12, 8, e.getScanSun()) {
            @Override
            public String getLabel() {
                return new TextComponentTranslation("thaumicaugmentation.gui.scan_sun").getFormattedText();
            }
            
            @Override
            public void onToggle(boolean newState) {
                TANetwork.INSTANCE.sendToServer(new PacketInteractGUI(id, newState ? 1 : 0));
            }
        });
        buttonList.add(new ButtonToggle(1, guiLeft + 8, guiTop + 36, 8, 8, e.getScanMoon()) {
            @Override
            public String getLabel() {
                return new TextComponentTranslation("thaumicaugmentation.gui.scan_moon").getFormattedText();
            }
            
            @Override
            public void onToggle(boolean newState) {
                TANetwork.INSTANCE.sendToServer(new PacketInteractGUI(id, newState ? 1 : 0));
            }
        });
        buttonList.add(new ButtonToggle(2, guiLeft + 8, guiTop + 50, 8, 8, e.getScanStars()) {
            @Override
            public String getLabel() {
                return new TextComponentTranslation("thaumicaugmentation.gui.scan_stars").getFormattedText();
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
        EntityCelestialObserver e = ((ContainerCelestialObserver) inventorySlots).getEntity();
        BlockPos base = new BlockPos(new BlockPos(e.getLookVec().add(e.posX, e.posY + 1.0, e.posZ)));
        boolean skyVisible = e.getEntityWorld().provider.isSurfaceWorld() && e.getEntityWorld().canSeeSky(base);
        Minecraft mc = Minecraft.getMinecraft();
        if (fb != null && !mc.skipRenderWorld) {
            int w = mc.displayWidth;
            int h = mc.displayHeight;
            int tp = mc.gameSettings.thirdPersonView;
            boolean hg = mc.gameSettings.hideGUI;
            // does this even still work in 1.12?
            boolean a = mc.gameSettings.anaglyph;
            float fov = mc.gameSettings.fovSetting;
            Entity rv = mc.getRenderViewEntity();
            try {
                mc.displayWidth = FB_WIDTH;
                mc.displayHeight = FB_HEIGHT;
                mc.gameSettings.thirdPersonView = 0;
                mc.gameSettings.hideGUI = true;
                mc.gameSettings.anaglyph = false;
                mc.gameSettings.fovSetting = 15.0F;
                mc.setRenderViewEntity(e);
                mc.profiler.startSection("ta_world_fbo");
                fb.bindFramebuffer(false);
                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.pushMatrix();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                GlStateManager.pushMatrix();
                int fps = Math.max(Math.min(Minecraft.getDebugFPS(), mc.gameSettings.limitFramerate), 60);
                mc.entityRenderer.renderWorld(partialTicks, System.nanoTime() + Math.max(1000000000 / fps / 4, 0));
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                mc.profiler.endSection();
            }
            catch (Exception ex) {
                // do thing like with tc blueprint render and flush buffer
                // but this time it *should* be done but might not if exceptions happened
                try {
                    Tessellator.getInstance().draw();
                }
                catch (Exception ex2) {}
            }
            finally {
                fb.unbindFramebuffer();
                mc.getFramebuffer().bindFramebuffer(true);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend();
                mc.displayWidth = w;
                mc.displayHeight = h;
                mc.gameSettings.thirdPersonView = tp;
                mc.gameSettings.hideGUI = hg;
                mc.gameSettings.anaglyph = a;
                mc.gameSettings.fovSetting = fov;
                mc.setRenderViewEntity(rv);
            }
            
            int xMin = (this.width - this.xSize) / 2 + 62;
            int xMax = (this.width - this.xSize) / 2 + 114;
            int yMin = (this.height - this.ySize) / 2 + 14;
            int yMax = (this.height - this.ySize) / 2 + 66;
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            Tessellator t = Tessellator.getInstance();
            BufferBuilder buffer = t.getBuffer();
            fb.bindFramebufferTexture();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(xMin, yMax, zLevel).tex(0.0F, 0.0F).endVertex();
            buffer.pos(xMax, yMax, zLevel).tex(1.0F, 0.0F).endVertex();
            buffer.pos(xMax, yMin, zLevel).tex(1.0F, 1.0F).endVertex();
            buffer.pos(xMin, yMin, zLevel).tex(0.0F, 1.0F).endVertex();
            t.draw();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
        }
        else {
            int color = 0xFF111111;
            if (skyVisible) {
                Vec3d sky = e.getEntityWorld().provider.getSkyColor(e, partialTicks);
                color = 0xFF << 24 | (int) (sky.x * 255) << 16 | (int) (sky.y * 255) << 8 | (int) (sky.z * 255);
            }
            else {
                IBlockState blocking = null;
                MutableBlockPos finder = new MutableBlockPos(base);
                while (finder.getY() < 256) {
                    blocking = e.getEntityWorld().getBlockState(finder);
                    if (!blocking.getBlock().isAir(blocking, e.getEntityWorld(), finder))
                        break;
                    
                    finder.setY(finder.getY() + 1);
                }
                
                if (blocking != null) {
                    int baseColor = blocking.getMapColor(e.getEntityWorld(), finder).colorValue;
                    float brightness = e.getEntityWorld().getLight(finder.down()) / 15.0F;
                    int r = (int) (((baseColor & 0xFF0000) >>> 16) * brightness);
                    int g = (int) (((baseColor & 0x00FF00) >>> 8) * brightness);
                    int b = (int) ((baseColor & 0x0000FF) * brightness);
                    color = 0xFF << 24 | r << 16 | g << 8 | b;
                }
            }
            
            drawRect((this.width - this.xSize) / 2 + 62, (this.height - this.ySize) / 2 + 14, (this.width - this.xSize) / 2 + 114, (this.height - this.ySize) / 2 + 66, color);
            if (skyVisible && e.getEntityWorld().provider.isSurfaceWorld()) {
                GlStateManager.enableBlend();
                int xMin = (this.width - this.xSize) / 2 + 76;
                int xMax = (this.width - this.xSize) / 2 + 100;
                int yMin = (this.height - this.ySize) / 2 + 28;
                int yMax = (this.height - this.ySize) / 2 + 52;
                long time = e.getEntityWorld().getWorldTime() % 24000;
                float sun = e.getEntityWorld().provider.getSunBrightness(partialTicks);
                mc.renderEngine.bindTexture(SUN);
                GlStateManager.color(sun, sun, sun, time >= 12000 && time <= 23000  ? 0.0F : 1.0F);
                Tessellator t = Tessellator.getInstance();
                BufferBuilder buffer = t.getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(xMin, yMax, zLevel).tex(0.375F, 0.625F).endVertex();
                buffer.pos(xMax, yMax, zLevel).tex(0.625F, 0.625F).endVertex();
                buffer.pos(xMax, yMin, zLevel).tex(0.625F, 0.375F).endVertex();
                buffer.pos(xMin, yMin, zLevel).tex(0.375F, 0.375F).endVertex();
                t.draw();
                xMin = (this.width - this.xSize) / 2 + 60;
                xMax = (this.width - this.xSize) / 2 + 116;
                yMin = (this.height - this.ySize) / 2 + 12;
                yMax = (this.height - this.ySize) / 2 + 68;
                int phase = e.getEntityWorld().provider.getMoonPhase(e.getEntityWorld().getWorldTime());
                int x = phase % 4;
                int y = phase / 4 % 2;
                mc.renderEngine.bindTexture(MOON);
                GlStateManager.color(1.0F, 1.0F, 1.0F, time >= 12000 && time <= 23000 ? 1.0F : 0.0F);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(xMin, yMax, zLevel).tex(x / 4.0F, (y + 1) / 2.0F).endVertex();
                buffer.pos(xMax, yMax, zLevel).tex((x + 1.0F) / 4.0F, (y + 1) / 2.0F).endVertex();
                buffer.pos(xMax, yMin, zLevel).tex((x + 1.0F) / 4.0F, y / 2.0F).endVertex();
                buffer.pos(xMin, yMin, zLevel).tex(x / 4.0F, y / 2.0F).endVertex();
                t.draw();
            }
            
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        
        mc.renderEngine.bindTexture(TEXTURE);
        if (!skyVisible)
            drawTexturedModalRect((this.width - this.xSize) / 2 + 75, (this.height - this.ySize) / 2 + 27, 229, 0, 32, 32);
            
        drawTexturedModalRect((this.width - this.xSize) / 2, (this.height - this.ySize) / 2, 0, 0, xSize, ySize);
    }
    
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format(ThaumicAugmentationAPI.MODID + ".gui.scans"), 8, 6, 0xFFFFFF);
        World world = ((ContainerCelestialObserver) inventorySlots).getEntity().getEntityWorld();
        float aura = AURA_STRENGTH[world.provider.getMoonPhase(world.getWorldInfo().getWorldTime())];
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        int auraColor = Aspect.ENERGY.getColor();
        GlStateManager.color(((auraColor >> 16) & 0xFF) / 255.0F, ((auraColor >> 8) & 0xFF) / 255.0F, (auraColor & 0xFF) / 255.0F, 1.0F);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        Minecraft.getMinecraft().renderEngine.bindTexture(TATextures.TC_HUD);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(132, 64, 0.0).tex(0.40625, 0.1171875).endVertex();
        buffer.pos(132 + aura, 64, 0.0).tex(0.40625, 0.0).endVertex();
        buffer.pos(132 + aura, 58, 0.0).tex(0.4375, 0.0).endVertex();
        buffer.pos(132, 58, 0.0).tex(0.4375, 0.1171875).endVertex();
        t.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(128, 68, 0.0).tex(0.34375, 0.0078125).endVertex();
        buffer.pos(168, 68, 0.0).tex(0.34375, 0.16796875).endVertex();
        buffer.pos(168, 54, 0.0).tex(0.28125, 0.16796875).endVertex();
        buffer.pos(128, 54, 0.0).tex(0.28125, 0.0078125).endVertex();
        t.draw();
    }
    
}
