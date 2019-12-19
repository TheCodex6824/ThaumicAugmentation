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
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.tile.CapabilityRiftJar;
import thecodex6824.thaumicaugmentation.api.tile.IRiftJar;
import thecodex6824.thaumicaugmentation.api.util.FluxRiftReconstructor;
import thecodex6824.thaumicaugmentation.common.tile.TileRiftJar;

public class RenderRiftJar extends TileEntitySpecialRenderer<TileRiftJar> {

    @Override
    public void render(TileRiftJar te, double x, double y, double z, float partialTicks, int destroyStage,
            float alpha) {
        
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        IRiftJar jar = te.getCapability(CapabilityRiftJar.RIFT_JAR, null);
        if (jar != null) {
            int tess = 6;
            Entity rv = Minecraft.getMinecraft().getRenderViewEntity() != null ? Minecraft.getMinecraft().getRenderViewEntity() :
                Minecraft.getMinecraft().player;
            double dist = rv.getPositionEyes(partialTicks).squareDistanceTo(te.getPos().getX() + 0.5,
                    te.getPos().getY() + 0.25, te.getPos().getZ() + 0.5);
            if (dist <= 64 * 64) {
                if (dist > 16 * 16)
                    tess = 2;
                else if (dist > 8 * 8)
                    tess = 3;
                
                IBlockState state = te.getWorld().getBlockState(te.getPos());
                FluxRiftReconstructor rift = jar.getRift();
                
                GlStateManager.pushMatrix();
                GlStateManager.translate(x + 0.5, y + 0.375, z + 0.5);
                EnumFacing dir = state.getValue(IHorizontallyDirectionalBlock.DIRECTION);
                if (dir != null)
                    GlStateManager.rotate(dir.getHorizontalAngle(), 0F, 1.0F, 0F);
                
                AxisAlignedBB box = rift.getBoundingBox();
                GlStateManager.scale(0.3125 / (Math.max(Math.abs(box.minX), Math.abs(box.maxX)) + 0.075), 0.375 / (Math.max(Math.abs(box.minY), Math.abs(box.maxY)) + 0.075),
                        0.3125 / (Math.max(Math.abs(box.minZ), Math.abs(box.maxZ)) + 0.075));
                ThaumicAugmentation.proxy.getRenderHelper().renderFluxRift(rift, 0, partialTicks, tess, true);
                GlStateManager.popMatrix();
            }
        }
    }
    
}
