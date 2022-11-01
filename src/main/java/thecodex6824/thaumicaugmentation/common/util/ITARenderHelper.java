/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

public interface ITARenderHelper {

    void renderGlowingSphere(World world, double x, double y, double z, int color);
    
    void renderBurst(World world, double x, double y, double z, float size, int color);
    
    void renderSpark(World world, double x, double y, double z, float size, int color, boolean colorAlpha);
    
    void renderArc(World world, double x, double y, double z, double dx, double dy, double dz, int color, double height);
    
    void renderFluxRift(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, boolean ignoreGoggles);

    void renderFluxRiftOutline(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, float r, float g, float b, float a, boolean bindTexture, int joinType);
    
    void renderFluxRiftSolidLayer(FluxRiftReconstructor rift, int stability, float partialTicks, int tessLevel, float r, float g, float b, float a, boolean bindTexture, int joinType);
    
    void renderDimensionalFracture(boolean open, long worldTime, long timeOpened, long openingDuration, float partialTicks, int tessLevel, boolean ignoreGoggles, float r, float g, float b, float a);
    
    void renderDimensionalFractureSolidLayer(boolean open, long worldTime, long timeOpened, long openingDuration, float partialTicks, int tessLevel, float r, float g, float b, float a, boolean bindTexture, int joinType);
    
    void renderWispyMotes(World world, double x, double y, double z, double dx, double dy, double dz, int age, float r, float g, float b, float gravity);

    void renderFireMote(World world, float x, float y, float z, float vx, float vy, float vz, float r, float g, float b, float a, float scale);
    
    void renderSmokeSpiral(World world, double x, double y, double z, float rad, int start, int minY, int color);

    void renderTerraformerParticle(World world, double x, double y, double z, double vx, double vy, double vz, int color);
    
    void renderRiftMoverParticle(World world, double x, double y, double z, double vx, double vy, double vz);
    
    void renderObeliskParticles(World world, double x, double y, double z);
    
    void renderParticleTrail(World world, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b);
    
    void renderStarfieldGlass(ShaderType type, TileStarfieldGlass tile, double pX, double pY, double pZ);
    
    void renderObelisk(ShaderType type, TileObelisk tile, double pX, double pY, double pZ);
    
    void renderRiftBarrier(ShaderType type, TileRiftBarrier tile, double pX, double pY, double pZ);
    
    void renderWisp(double x, double y, double z, Entity target);
    
    void renderVent(double x, double y, double z, double vx, double vy, double vz, int color, float scale);
    
    void renderWispParticles(double x, double y, double z, double vx, double vy, double vz, int color, int delay);
    
    void renderFollowingParticles(World world, double x, double y, double z, Entity toFollow, float r, float g, float b);
    
    void drawCube();
    
    void drawCube(double min, double max);
    
    Vec3d estimateImpulseCannonFiringPoint(EntityLivingBase entity, float partialTicks);
    
    boolean shadersAvailable();
    
    boolean stencilAvailable();
    
    boolean framebuffersAvailable();
    
}
