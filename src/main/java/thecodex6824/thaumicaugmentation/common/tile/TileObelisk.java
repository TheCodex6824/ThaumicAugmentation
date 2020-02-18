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

package thecodex6824.thaumicaugmentation.common.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thaumcraft.api.entities.IEldritchMob;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.common.util.IShaderRenderingCallback;
import thecodex6824.thaumicaugmentation.common.util.ShaderType;

public class TileObelisk extends TileEntity implements ITickable, IShaderRenderingCallback {
    
    protected int ticks;
    
    protected int getHealCycleLength() {
        switch (world.getDifficulty()) {
            case NORMAL: return 20;
            case HARD: return 10;
            default: return 30;
        }
    }
    
    @Override
    public void update() {
        if (++ticks % getHealCycleLength() == 0) {
            if (!world.isRemote) {
                for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos).grow(5.0))) {
                    if (entity instanceof IEldritchMob)
                        entity.heal(1.0F);
                }
            }
            else {
                boolean particles = false;
                for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos).grow(5.0))) {
                    if (entity instanceof IEldritchMob) {
                        Vec3d dir = entity.getPositionVector().subtract(new Vec3d(pos)).normalize();
                        ThaumicAugmentation.proxy.getRenderHelper().renderObeliskConnection(world, pos.getX(), pos.getY(), pos.getZ(),
                                dir.x * 0.1F, dir.y * 0.1F, dir.z * 0.1F);
                        particles = true;
                    }
                }
                
                if (particles) {
                    ThaumicAugmentation.proxy.getRenderHelper().renderObeliskParticles(world, pos.getX() + world.rand.nextFloat() * 1.5F,
                            pos.getY() + world.rand.nextFloat() * 1.5F, pos.getZ() + world.rand.nextFloat() * 1.5F);
                }
            }
        }
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    public double getMaxRenderDistanceSquared() {
        return 16384.0;
    }
    
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos).grow(0.0, 1.0, 0.0);
    }
    
    @Override
    public void render(ShaderType type, double pX, double pY, double pZ) {
        ThaumicAugmentation.proxy.getRenderHelper().renderObelisk(type, this, pX, pY, pZ);
    }
    
}
