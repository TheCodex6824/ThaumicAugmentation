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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import thaumcraft.common.lib.utils.EntityUtils;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.tile.CapabilityRiftJar;
import thecodex6824.thaumicaugmentation.api.tile.IRiftJar;
import thecodex6824.thaumicaugmentation.common.tile.TileRiftMoverOutput;

public class RenderRiftMoverOutput extends TileEntitySpecialRenderer<TileRiftMoverOutput> {

    @Override
    public void render(TileRiftMoverOutput te, double x, double y, double z, float partialTicks, int destroyStage,
            float alpha) {
        
        if (EntityUtils.hasGoggles(Minecraft.getMinecraft().player)) {
            Vec3d origin = te.findLocalRiftPos();
            if (origin != null) {
                TileEntity below = te.getWorld().getTileEntity(te.getPos().down());
                if (below != null) {
                    IRiftJar jar = below.getCapability(CapabilityRiftJar.RIFT_JAR, null);
                    if (jar != null) {
                        //EnumFacing dir = te.getWorld().getBlockState(te.getPos().down()).getValue(IHorizontallyDirectionalBlock.DIRECTION);
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(x + origin.x, y + origin.y, z + origin.z);
                        //if (dir != null)
                        //    GlStateManager.rotate(dir.getHorizontalAngle(), 0F, 1.0F, 0F);
                        
                        ThaumicAugmentation.proxy.getRenderHelper().renderFluxRiftSolidLayer(jar.getRift(), 0, partialTicks, 6, 0.55F, 0.55F, 0.55F, 0.6F);
                        GlStateManager.popMatrix();
                    }
                }
            }
        }
    }
    
}
