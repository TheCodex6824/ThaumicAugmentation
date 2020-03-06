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

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.block.property.IEldritchLockType;
import thecodex6824.thaumicaugmentation.api.block.property.IEldritchLockType.LockType;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.client.renderer.texture.TATextures;
import thecodex6824.thaumicaugmentation.common.tile.TileEldritchLock;
import thecodex6824.thaumicaugmentation.common.util.ITARenderHelper;

public class RenderEldritchLock extends TileEntitySpecialRenderer<TileEldritchLock> {
    
    @Override
    public void render(TileEldritchLock te, double x, double y, double z, float partialTicks, int destroyStage,
            float alpha) {
        
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        Entity rv = Minecraft.getMinecraft().getRenderViewEntity();
        if (rv == null)
            rv = Minecraft.getMinecraft().player;
        
        IBlockState state = te.getWorld().getBlockState(te.getPos());
        EnumFacing face = state.getValue(IHorizontallyDirectionalBlock.DIRECTION);
        LockType type = state.getPropertyKeys().contains(IEldritchLockType.LOCK_TYPE) ?
                state.getValue(IEldritchLockType.LOCK_TYPE) : LockType.BOSS;
        ITARenderHelper renderer = ThaumicAugmentation.proxy.getRenderHelper();
        bindTexture(TATextures.ELDRITCH_CUBE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        RenderHelper.disableStandardItemLighting();
        for (int angle = 0; angle < 4; ++angle) {
            EnumFacing lightFace;
            switch (angle) {
                case 0: {
                    lightFace = EnumFacing.UP;
                    break;
                }
                case 1: {
                    lightFace = face.rotateYCCW();
                    break;
                }
                case 2: {
                    lightFace = EnumFacing.DOWN;
                    break;
                }
                case 3: {
                    lightFace = face.rotateY();
                    break;
                }
                default: {
                    lightFace = face;
                    break;
                }
            }
            GlStateManager.pushMatrix();
            GlStateManager.rotate(angle * 90, face.getXOffset(), face.getYOffset(), face.getZOffset());
            int light = te.getWorld().getCombinedLight(te.getPos().offset(lightFace), 0);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
            int limit = 5;
            if (te.getOpenTicks() != Integer.MIN_VALUE) {
                switch (type) {
                    case LABYRINTH: {
                        limit = Math.min(te.getOpenTicks() / 20, 5);
                        break;
                    }
                    case PRISON: {
                        limit = Math.min((te.getOpenTicks() + (angle == 0 || angle == 2 ? 5 : 15)) / 20, 5);
                        break;
                    }
                    case LIBRARY: {
                        limit = Math.min((te.getOpenTicks() + (angle == 0 || angle == 2 ? 15 : 5)) / 20, 5);
                        break;
                    }
                    case BOSS: {
                        limit = Math.min((te.getOpenTicks() + angle * 5) / 20, 5);
                        break;
                    }
                    default: break;
                }
            }
            for (int i = 1; i < limit; ++i) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0, 0.25 + 0.5 * i, 0.0);
                double wave = Math.sin((rv.ticksExisted + partialTicks + i * 10.0 + angle * 20.0) / 20.0) * 0.1;
                if (i == 1 || i == 4)
                    wave = wave / 2.0 + 0.2;
                
                GlStateManager.scale(0.5 + wave, 0.5, 0.5 + wave);
                renderer.drawCube(-0.5, 0.5);
                GlStateManager.popMatrix();
            }
            
            GlStateManager.popMatrix();
        }
        
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }
    
}
