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

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.common.entities.monster.EntityEldritchCrab;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.EntityUtils;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;

public class TileCrabVent extends TileEntity implements ITickable {

    protected int ticks;
    protected int clientVenting;
    
    public TileCrabVent() {
        super();
        ticks = -1;
        clientVenting = -1;
    }
    
    protected boolean canSpawnCrab() {
        return world.getClosestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16.0, false) != null &&
                world.getEntitiesWithinAABB(EntityEldritchCrab.class, new AxisAlignedBB(pos).grow(16.0)).size() < 5;
    }
    
    protected void makeVentParticles() {
        IBlockState state = world.getBlockState(pos);
        EnumFacing face = state.getValue(IDirectionalBlock.DIRECTION);
        AxisAlignedBB box = state.getBoundingBox(world, pos);
        Random rand = world.rand;
        double x = face.getAxis() == Axis.X ? (face.getAxisDirection() == AxisDirection.POSITIVE ? box.maxX : box.minX) + 
                (rand.nextFloat() - rand.nextFloat()) / 4.0 : rand.nextFloat() / 2.0 + 0.25;
        double y = face.getAxis() == Axis.Y ? (face.getAxisDirection() == AxisDirection.POSITIVE ? box.maxY : box.minY) + 
                (rand.nextFloat() - rand.nextFloat()) / 4.0 : rand.nextFloat() / 2.0 + 0.25;
        double z = face.getAxis() == Axis.Z ? (face.getAxisDirection() == AxisDirection.POSITIVE ? box.maxZ : box.minZ) + 
                (rand.nextFloat() - rand.nextFloat()) / 4.0 : rand.nextFloat() / 2.0 + 0.25;
        ThaumicAugmentation.proxy.getRenderHelper().renderVent(pos.getX() + x, pos.getY() + y, pos.getZ() + z, face.getXOffset() * 0.25, face.getYOffset() * 0.25,
                face.getZOffset() * 0.25, 0x9988AA, 2.0F);
    }
    
    protected int getBaseSpawnDelay() {
        switch (world.getDifficulty()) {
            case HARD: return 120;
            case NORMAL: return 150;
            default: return 200;
        }
    }
    
    @Override
    public void update() {
        if (!world.isRemote) {
            if (ticks < 0)
                ticks = world.rand.nextInt(201);
            
            --ticks;
            boolean canSpawn = canSpawnCrab();
            if (ticks == 15 && canSpawn) {
                world.addBlockEvent(pos, getBlockType(), 1, 0);
                world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 1.1F);
            }
            else if (ticks == 0) {
                if (canSpawn) {
                    ticks = getBaseSpawnDelay() + world.rand.nextInt(61);
                    EnumFacing face = world.getBlockState(pos).getValue(IDirectionalBlock.DIRECTION);
                    EntityEldritchCrab crab = new EntityEldritchCrab(world);
                    double offsetX = face.getAxis() == Axis.X ? face.getXOffset() * 0.33 : 0.5;
                    double offsetY = face.getAxis() == Axis.Y ? face.getYOffset() * 0.33 : 0.5 - crab.height / 2.0;
                    double offsetZ = face.getAxis() == Axis.Z ? face.getZOffset() * 0.33 : 0.5;
                    crab.setLocationAndAngles(pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ,
                            face.getHorizontalAngle(), 0.0F);
                    crab.motionX = face.getXOffset() * 0.2F;
                    crab.motionY = face.getYOffset() * 0.2F;
                    crab.motionZ = face.getZOffset() * 0.2F;
                    crab.onInitialSpawn(world.getDifficultyForLocation(pos), null);
                    int difficulty = Math.max((int) (world.getDifficulty().getId() + 
                            world.getDifficultyForLocation(pos).getAdditionalDifficulty()), 1);
                    crab.setHelm(world.rand.nextInt(100 / difficulty) == 0);
                    if (world.rand.nextInt(1000 / difficulty) == 0)
                        EntityUtils.makeChampion(crab, false);
                    
                    if (world.spawnEntity(crab))
                        world.playSound(null, pos, SoundsTC.gore, SoundCategory.BLOCKS, 0.5F, 1.0F);
                }
                else
                    ticks = 60 + world.rand.nextInt(101);
            }
            else if (ticks % 5 == 0 && world.rand.nextInt(20) == 0)
                world.playSound(null, pos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.5F, 0.65F);
        }
        else {
            if (clientVenting > 0) {
                --clientVenting;
                for (int i = 0; i < 2 + world.rand.nextInt(4); ++i)
                    makeVentParticles();
            }
            else if (world.rand.nextInt(20) == 0)
                makeVentParticles();
        }
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            clientVenting = 20;
            return true;
        }
        else
            return false;
    }
    
}
