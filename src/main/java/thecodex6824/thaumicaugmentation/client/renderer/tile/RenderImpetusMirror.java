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

import java.nio.FloatBuffer;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.GlStateManager.TexGen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.tile.TileImpetusMirror;

public class RenderImpetusMirror extends TileEntitySpecialRenderer<TileImpetusMirror> {

    protected static final ResourceLocation TUNNEL = new ResourceLocation("thaumcraft", "textures/misc/tunnel.png");
    protected static final ResourceLocation PARTICLES = new ResourceLocation("thaumcraft", "textures/misc/particlefield.png");
    
    protected static final double LAYER_OFFSET = 0.01;
    
    protected static final FloatBuffer BUFFER_X = (FloatBuffer) GLAllocation.createDirectFloatBuffer(16).put(1.0F).put(0.0F).put(0.0F).put(0.0F).flip();
    protected static final FloatBuffer BUFFER_Y = (FloatBuffer) GLAllocation.createDirectFloatBuffer(16).put(0.0F).put(1.0F).put(0.0F).put(0.0F).flip();
    protected static final FloatBuffer BUFFER_Z = (FloatBuffer) GLAllocation.createDirectFloatBuffer(16).put(0.0F).put(0.0F).put(1.0F).put(0.0F).flip();
    protected static final FloatBuffer BUFFER_W = (FloatBuffer) GLAllocation.createDirectFloatBuffer(16).put(0.0F).put(0.0F).put(0.0F).put(1.0F).flip();
    
    protected void setupLayer(double x, double y, double z, EnumFacing facing, int index) {
        double px = (float) TileEntityRendererDispatcher.staticPlayerX;
        double py = (float) TileEntityRendererDispatcher.staticPlayerY;
        double pz = (float) TileEntityRendererDispatcher.staticPlayerZ;
        Vec3d cam = ActiveRenderInfo.getCameraPosition();
        
        GlStateManager.pushMatrix();
        float layer = (16 - index);
        float scaleFactor = 0.0625F;
        if (index == 0) {
            Minecraft.getMinecraft().renderEngine.bindTexture(TUNNEL);
            layer = 65.0F;
            scaleFactor = 0.125F;
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        }
        else if (index == 1) {
            Minecraft.getMinecraft().renderEngine.bindTexture(PARTICLES);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ONE);
            scaleFactor = 0.5F;
        }
        
        switch (facing) {
            case UP:
                GlStateManager.translate(px, y + LAYER_OFFSET + (-(y + LAYER_OFFSET) + cam.y) / (-(y + LAYER_OFFSET) + layer + cam.y), pz);
                break;
            case WEST:
                GlStateManager.translate(x + (1.0 - LAYER_OFFSET) + ((x + (1.0 - LAYER_OFFSET)) - cam.x) / (x + (1.0 - LAYER_OFFSET) + layer - cam.x), py, pz);
                break;
            case EAST:
                GlStateManager.translate(x + LAYER_OFFSET + (-(x + LAYER_OFFSET) + cam.x) / (-(x + LAYER_OFFSET) + layer + cam.x), py, pz);
                break;
            case NORTH:
                GlStateManager.translate(px, py, z + (1.0 - LAYER_OFFSET) + ((z + (1.0 - LAYER_OFFSET)) - cam.z) / (z + (1.0 - LAYER_OFFSET) + layer - cam.z));
                break;
            case SOUTH:
                GlStateManager.translate(px, py, z + LAYER_OFFSET + (-(z + LAYER_OFFSET) + cam.z) / (-(z + LAYER_OFFSET) + layer + cam.z));
                break;
            case DOWN:
            default:
                GlStateManager.translate(px, y + (1.0 - LAYER_OFFSET) + ((y + (1.0 - LAYER_OFFSET)) - cam.y) / ((y + (1.0 - LAYER_OFFSET)) + layer - cam.y), pz);
                break;
        }
        GlStateManager.texGen(TexGen.S, GL11.GL_OBJECT_LINEAR);
        GlStateManager.texGen(TexGen.T, GL11.GL_OBJECT_LINEAR);
        GlStateManager.texGen(TexGen.R, GL11.GL_OBJECT_LINEAR);
        GlStateManager.texGen(TexGen.Q, GL11.GL_EYE_LINEAR);
        switch (facing) {
            case WEST:
            case EAST:
                GlStateManager.texGen(TexGen.S, GL11.GL_OBJECT_PLANE, BUFFER_Z);
                GlStateManager.texGen(TexGen.T, GL11.GL_OBJECT_PLANE, BUFFER_Y);
                GlStateManager.texGen(TexGen.R, GL11.GL_OBJECT_PLANE, BUFFER_W);
                GlStateManager.texGen(TexGen.Q, GL11.GL_EYE_PLANE, BUFFER_X);
                break;
            case NORTH:
            case SOUTH:
                GlStateManager.texGen(TexGen.S, GL11.GL_OBJECT_PLANE, BUFFER_X);
                GlStateManager.texGen(TexGen.T, GL11.GL_OBJECT_PLANE, BUFFER_Y);
                GlStateManager.texGen(TexGen.R, GL11.GL_OBJECT_PLANE, BUFFER_W);
                GlStateManager.texGen(TexGen.Q, GL11.GL_EYE_PLANE, BUFFER_Z);
                break;
            case DOWN:
            case UP:
            default:
                GlStateManager.texGen(TexGen.S, GL11.GL_OBJECT_PLANE, BUFFER_X);
                GlStateManager.texGen(TexGen.T, GL11.GL_OBJECT_PLANE, BUFFER_Z);
                GlStateManager.texGen(TexGen.R, GL11.GL_OBJECT_PLANE, BUFFER_W);
                GlStateManager.texGen(TexGen.Q, GL11.GL_EYE_PLANE, BUFFER_Y);
                break;
        }
        GlStateManager.enableTexGenCoord(TexGen.S);
        GlStateManager.enableTexGenCoord(TexGen.T);
        GlStateManager.enableTexGenCoord(TexGen.R);
        GlStateManager.enableTexGenCoord(TexGen.Q);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0, System.currentTimeMillis() % 70000 / 250000.0, 0.0);
        GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
        GlStateManager.translate(0.5, 0.5, 0.0);
        GlStateManager.rotate((index * index * 4321 + index * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(-0.5, -0.5, 0.0);
        switch (facing) {
            case UP:
                GlStateManager.translate(-px, -pz, -py);
                GlStateManager.translate(cam.x * layer / ((y + LAYER_OFFSET) - cam.y), cam.z * layer / ((y + LAYER_OFFSET) - cam.y), -py);
                break;
            case WEST:
                GlStateManager.translate(-pz, -py, -px);
                GlStateManager.translate(cam.z * layer / ((x + (1.0 - LAYER_OFFSET)) - cam.x), cam.y * layer / ((x + (1.0 - LAYER_OFFSET)) - cam.x), -px);
                break;
            case EAST:
                GlStateManager.translate(-pz, -py, -px);
                GlStateManager.translate(cam.z * layer / (-(x + LAYER_OFFSET) + cam.x), cam.y * layer / (-(x + LAYER_OFFSET) + cam.x), -px);
                break;
            case NORTH:
                GlStateManager.translate(-px, -py, -pz);
                GlStateManager.translate(cam.x * layer / ((z + (1.0 - LAYER_OFFSET)) - cam.z), cam.y * layer / ((z + (1.0 - LAYER_OFFSET)) - cam.z), -pz);
                break;
            case SOUTH:
                GlStateManager.translate(-px, -py, -pz);
                GlStateManager.translate(cam.x * layer / (-(z + LAYER_OFFSET) + cam.z), cam.y * layer / (-(z + LAYER_OFFSET) + cam.z), -pz);
                break;
            case DOWN:
            default:
                GlStateManager.translate(-px, -pz, -py);
                GlStateManager.translate(cam.x * layer / (-(y + (1.0 - LAYER_OFFSET)) + cam.y), cam.z * layer / (-(y + (1.0 - LAYER_OFFSET)) + cam.y), -py);
                break;
        }
        
    }
    
    @Override
    public void render(TileImpetusMirror te, double x, double y, double z, float partialTicks, int destroyStage,
            float alpha) {
        
        EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(IDirectionalBlock.DIRECTION);
        if (facing != null && te.getLink() != DimensionalBlockPos.INVALID) {
            Random random = new Random(31100);
            Tessellator t = Tessellator.getInstance();
            BufferBuilder buffer = t.getBuffer();
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            for (int i = 0; i < 16; ++i) {
                setupLayer(x, y, z, facing, i);
                float colorMod, red, green, blue;
                if (i == 0) {
                    colorMod = 0.1F;
                    red = 1.0F;
                    green = 1.0F;
                    blue = 1.0F;
                }
                else {
                    colorMod = 1.0F / (16 - i + 1.0F);
                    red = random.nextFloat() * 0.5F + 0.1F;
                    green = random.nextFloat() * 0.5F + 0.4F;
                    blue = random.nextFloat() * 0.5F + 0.5F;
                }
                buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                switch (facing) {
                    case UP:
                        buffer.pos(x + 0.1875, y + LAYER_OFFSET, z + 0.1875).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + 0.1875, y + LAYER_OFFSET, z + 0.8125).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + 0.8125, y + LAYER_OFFSET, z + 0.8125).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + 0.8125, y + LAYER_OFFSET, z + 0.1875).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        break;
                    case WEST:
                        buffer.pos(x + (1.0 - LAYER_OFFSET), y + 0.1875, z + 0.1875).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + (1.0 - LAYER_OFFSET), y + 0.1875, z + 0.8125).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + (1.0 - LAYER_OFFSET), y + 0.8125, z + 0.8125).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + (1.0 - LAYER_OFFSET), y + 0.8125, z + 0.1875).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        break;
                    case EAST:
                        buffer.pos(x + LAYER_OFFSET, y + 0.8125, z + 0.1875).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + LAYER_OFFSET, y + 0.8125, z + 0.8125).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + LAYER_OFFSET, y + 0.1875, z + 0.8125).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + LAYER_OFFSET, y + 0.1875, z + 0.1875).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        break;
                    case NORTH:
                        buffer.pos(x + 0.1875, y + 0.1875, z + (1.0 - LAYER_OFFSET)).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + 0.1875, y + 0.8125, z + (1.0 - LAYER_OFFSET)).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + 0.8125, y + 0.8125, z + (1.0 - LAYER_OFFSET)).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + 0.8125, y + 0.1875, z + (1.0 - LAYER_OFFSET)).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        break;
                    case SOUTH:
                        buffer.pos(x + 0.1875, y + 0.8125, z + LAYER_OFFSET).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + 0.1875, y + 0.1875, z + LAYER_OFFSET).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + 0.8125, y + 0.1875, z + LAYER_OFFSET).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + 0.8125, y + 0.8125, z + LAYER_OFFSET).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        break;
                    case DOWN:
                    default:
                        buffer.pos(x + 0.1875, y + (1.0 - LAYER_OFFSET), z + 0.8125).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + 0.1875, y + (1.0 - LAYER_OFFSET), z + 0.1875).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + 0.8125, y + (1.0 - LAYER_OFFSET), z + 0.1875).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        buffer.pos(x + 0.8125, y + (1.0 - LAYER_OFFSET), z + 0.8125).color(red * colorMod, green * colorMod, blue * colorMod, 1.0F).endVertex();
                        break;
                }
                t.draw();
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            }
            
            GlStateManager.disableTexGenCoord(TexGen.S);
            GlStateManager.disableTexGenCoord(TexGen.T);
            GlStateManager.disableTexGenCoord(TexGen.R);
            GlStateManager.disableTexGenCoord(TexGen.Q);
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }
    
}
