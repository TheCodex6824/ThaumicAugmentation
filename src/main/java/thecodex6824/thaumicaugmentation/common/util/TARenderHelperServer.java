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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.api.util.FluxRiftReconstructor;
import thecodex6824.thaumicaugmentation.common.tile.TileObelisk;
import thecodex6824.thaumicaugmentation.common.tile.TileRiftBarrier;
import thecodex6824.thaumicaugmentation.common.tile.TileStarfieldGlass;

public class TARenderHelperServer implements ITARenderHelper {

    @Override
    public void renderGlowingSphere(World world, double x, double y, double z, int color) {}
    
    @Override
    public void renderBurst(World world, double x, double y, double z, float size, int color) {}
    
    @Override
    public void renderSpark(World world, double x, double y, double z, float size, int color, boolean colorAlpha) {}
    
    @Override
    public void renderArc(World world, double x, double y, double z, double dx, double dy, double dz, int color, double height) {}
    
    @Override
    public void renderFluxRift(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel,
            boolean ignoreGoggles) {}
    
    @Override
    public void renderFluxRiftOutline(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, float r, float g, float b, float a, boolean bindTexture, int joinType) {}

    @Override
    public void renderFluxRiftSolidLayer(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, float r, float g, float b, float a, boolean bindTexture, int joinType) {}
    
    @Override
    public void renderDimensionalFracture(boolean open, long worldTime, long timeOpened, long openingDuration,
            float partialTicks, int tessLevel, boolean ignoreGoggles, float r, float g, float b, float a) {}
    
    @Override
    public void renderDimensionalFractureSolidLayer(boolean open, long worldTime, long timeOpened, long openingDuration,
            float partialTicks, int tessLevel, float r, float g, float b, float a,
            boolean bindTexture, int joinType) {}
    
    @Override
    public void renderWispyMotes(World world, double x, double y, double z, double dx, double dy, double dz, int age,
            float r, float g, float b, float gravity) {}
    
    @Override
    public void renderFireMote(World world, float x, float y, float z, float vx, float vy, float vz, float r, float g,
            float b, float a, float scale) {}
    
    @Override
    public void renderSmokeSpiral(World world, double x, double y, double z, float rad, int start, int minY,
            int color) {}
    
    @Override
    public void renderTerraformerParticle(World world, double x, double y, double z, double vx, double vy, double vz,
            int color) {}
    
    @Override
    public void renderRiftMoverParticle(World world, double x, double y, double z, double vx, double vy,
            double vz) {}
    
    @Override
    public void renderObeliskParticles(World world, double x, double y, double z) {}
    
    @Override
    public void renderParticleTrail(World world, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b) {}
    
    @Override
    public void renderStarfieldGlass(ShaderType type, TileStarfieldGlass tile, double pX, double pY, double pZ) {}
    
    @Override
    public void renderObelisk(ShaderType type, TileObelisk tile, double pX, double pY, double pZ) {}
    
    @Override
    public void renderRiftBarrier(ShaderType type, TileRiftBarrier tile, double pX, double pY, double pZ) {}
    
    @Override
    public void renderWisp(double x, double y, double z, Entity target) {}
    
    @Override
    public void renderVent(double x, double y, double z, double vx, double vy, double vz, int color, float scale) {}
    
    @Override
    public void renderWispParticles(double x, double y, double z, double vx, double vy, double vz, int color,
            int delay) {}
    
    @Override
    public void renderFollowingParticles(World world, double x, double y, double z, Entity toFollow, float r, float g,
            float b) {}
    
    @Override
    public void drawCube() {}
    
    @Override
    public void drawCube(double min, double max) {}
    
    @Override
    public Vec3d estimateImpulseCannonFiringPoint(EntityLivingBase entity, float partialTicks) {
        return Vec3d.ZERO;
    }
    
    @Override
    public boolean shadersAvailable() {
        return false;
    }
    
    @Override
    public boolean stencilAvailable() {
        return false;
    }
    
    @Override
    public boolean framebuffersAvailable() {
        return false;
    }
    
}
