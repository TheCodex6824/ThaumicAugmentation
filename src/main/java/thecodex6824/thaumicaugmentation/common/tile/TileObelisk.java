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
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import thaumcraft.api.entities.IEldritchMob;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.common.util.IShaderRenderingCallback;
import thecodex6824.thaumicaugmentation.common.util.ShaderType;

public class TileObelisk extends TileEntity implements ITickable, IShaderRenderingCallback {
    
    protected int ticks;
    
    protected int getHealCycleLength() {
        switch (world.getDifficulty()) {
            case NORMAL: return 30;
            case HARD: return 20;
            default: return 40;
        }
    }
    
    @Override
    public void update() {
        if (!world.isRemote && ++ticks % getHealCycleLength() == 0) {
            boolean hard = world.getDifficulty() == EnumDifficulty.HARD;
            for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos).grow(6.0))) {
                if (entity instanceof IEldritchMob) {
                    if (entity.isPotionApplicable(new PotionEffect(MobEffects.REGENERATION, 1, 0))) {
                        entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, hard ? 1 : 0, true, true));
                        entity.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 100, hard ? 1 : 0, true, true));
                    }
                    else
                        entity.setHealth(entity.getHealth() + 1);
                }
            }
        }
        else if (world.isRemote && ++ticks % 5 == 0) {
            boolean particles = false;
            for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos).grow(6.0))) {
                if (entity instanceof IEldritchMob) {
                    ThaumicAugmentation.proxy.getRenderHelper().renderFollowingParticles(world, pos.getX(), pos.getY(), pos.getZ(),
                            entity, 0.05F, 0.05F, 0.05F);
                    ThaumicAugmentation.proxy.getRenderHelper().renderWisp(entity.posX, entity.posY + world.rand.nextFloat(), entity.posZ, entity);
                    particles = true;
                }
            }
            
            if (particles) {
                ThaumicAugmentation.proxy.getRenderHelper().renderObeliskParticles(world, pos.getX() + world.rand.nextFloat() * 1.5F,
                        pos.getY() + world.rand.nextFloat() * 1.5F, pos.getZ() + world.rand.nextFloat() * 1.5F);
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
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }
    
    @Override
    public boolean hasFastRenderer() {
        return true;
    }
    
    @Override
    public void renderWithShader(ShaderType type, double pX, double pY, double pZ) {
        ThaumicAugmentation.proxy.getRenderHelper().renderObelisk(type, this, pX, pY, pZ);
    }
    
}
