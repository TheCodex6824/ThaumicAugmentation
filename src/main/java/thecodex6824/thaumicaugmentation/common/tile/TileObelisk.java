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

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import thaumcraft.api.entities.IEldritchMob;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ward.storage.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorage;
import thecodex6824.thaumicaugmentation.api.ward.storage.IWardStorageServer;
import thecodex6824.thaumicaugmentation.common.util.IShaderRenderingCallback;
import thecodex6824.thaumicaugmentation.common.util.ShaderType;

public class TileObelisk extends TileEntity implements ITickable, IShaderRenderingCallback {
    
    protected int ticks;
    protected UUID wardOwner;
    
    public TileObelisk() {
        ticks = ThreadLocalRandom.current().nextInt(20);
        wardOwner = IWardStorageServer.NIL_UUID;
    }
    
    protected int getHealCycleLength() {
        switch (world.getDifficulty()) {
            case NORMAL: return 30;
            case HARD: return 20;
            default: return 40;
        }
    }
    
    @Override
    public void update() {
        if (!world.isRemote) {
            if (ticks % 40 == 0 && !wardOwner.equals(IWardStorageServer.NIL_UUID)) {
                IWardStorage s = world.getChunk(pos).getCapability(CapabilityWardStorage.WARD_STORAGE, null);
                if (s instanceof IWardStorageServer && !((IWardStorageServer) s).isWardOwner(wardOwner)) {
                    if (world.getBlockState(pos.down(4)).getBlock() == TABlocks.CAPSTONE)
                        world.setBlockToAir(pos.down(4));
                    
                    world.setBlockToAir(pos);
                    return;
                }
            }
            if (++ticks % getHealCycleLength() == 0) {
                boolean hard = world.getDifficulty() == EnumDifficulty.HARD;
                for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos).grow(6.0))) {
                    if (!entity.isDead && entity.getHealth() > 0.0F && entity instanceof IEldritchMob) {
                        if (entity.isPotionApplicable(new PotionEffect(MobEffects.REGENERATION, 1, 0))) {
                            entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, hard ? 1 : 0, true, true));
                            if (entity.isPotionApplicable(new PotionEffect(MobEffects.STRENGTH, 1, 0)))
                                entity.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 100, hard ? 1 : 0, true, true));
                        }
                        else
                            entity.setHealth(entity.getHealth() + 1);
                    }
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
    
    public void setBoundWard(UUID ward) {
        wardOwner = ward;
    }
    
    @Nullable
    public UUID getBoundWard() {
        return wardOwner;
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    public double getMaxRenderDistanceSquared() {
        int dist = TAConfig.bulkRenderDistance.getValue();
        return dist * dist;
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
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setUniqueId("wardOwner", wardOwner);
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        wardOwner = compound.getUniqueId("wardOwner");
    }
    
}
