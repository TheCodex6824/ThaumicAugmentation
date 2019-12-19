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

package thecodex6824.thaumicaugmentation.common.util;

import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.api.util.FluxRiftReconstructor;

public interface ITARenderHelper {

    public void renderGlowingSphere(World world, double x, double y, double z, int color);
    
    public void renderBurst(World world, double x, double y, double z, float size, int color);
    
    public void renderSpark(World world, double x, double y, double z, float size, int color, boolean colorAlpha);
    
    public void renderArc(World world, double x, double y, double z, double dx, double dy, double dz, int color, double height);
    
    public void renderFluxRift(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, boolean ignoreGoggles);

    public void renderFluxRiftOutline(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, float r, float g, float b, float a, boolean bindTexture, int joinType);
    
    public void renderFluxRiftSolidLayer(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, float r, float g, float b, float a, boolean bindTexture, int joinType);
    
    public void renderWispyMotes(World world, double x, double y, double z, double dx, double dy, double dz, int age, float r, float g, float b, float gravity);

    public void renderFireMote(World world, float x, float y, float z, float vx, float vy, float vz, float r, float g, float b, float a, float scale);
    
    public void renderSmokeSpiral(World world, double x, double y, double z, float rad, int start, int minY, int color);

    public boolean shadersAvailable();
    
    public boolean stencilAvailable();
    
}
