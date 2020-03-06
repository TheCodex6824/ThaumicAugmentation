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

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.api.casters.IInteractWithCaster;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.block.property.IAltarBlock;
import thecodex6824.thaumicaugmentation.common.entity.EntityTAEldritchWarden;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class TileAltar extends TileEntity implements ITickable, IInteractWithCaster {

    protected static final BlockPos[] CAP_OFFSETS = new BlockPos[] {
            new BlockPos(-1, 0, -3),
            new BlockPos(-3, 0, -1),
            new BlockPos(-3, 0, 1),
            new BlockPos(-1, 0, 3),
            new BlockPos(1, 0, 3),
            new BlockPos(3, 0, 1),
            new BlockPos(3, 0, -1),
            new BlockPos(1, 0, -3)
    };
    
    protected int ticks;
    protected int openTicks;
    
    public TileAltar() {
        super();
        openTicks = Integer.MIN_VALUE;
    }
    
    public void open() {
        openTicks = 280;
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }
    
    public boolean isOpen() {
        return openTicks != Integer.MIN_VALUE;
    }
    
    public int getOpenTicks() {
        return openTicks;
    }
    
    protected EntityLiving createBoss() {
        return new EntityTAEldritchWarden(world);
    }
    
    @Override
    public void update() {
        if (openTicks > 0)
            --openTicks;
        
        if (!world.isRemote && openTicks >= 0) {
            if (openTicks == 0) {
                BlockPos check = pos.up(2);
                IBlockState state = world.getBlockState(check);
                if (state.getBlock() == TABlocks.OBELISK)
                    world.destroyBlock(check, false);
                
                EntityLiving boss = createBoss();
                boss.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                        MathHelper.wrapDegrees(world.rand.nextFloat() * 360.0F), 0.0F);
                boss.rotationYawHead = boss.rotationYaw;
                boss.renderYawOffset = boss.rotationYaw;
                boss.onInitialSpawn(world.getDifficultyForLocation(pos), null);
                if (world.spawnEntity(boss)) {
                    world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, 1.0F);
                    TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.EXPLOSION, pos.getX() + 0.5,
                            pos.getY(), pos.getZ() + 0.5), boss);
                }
                
                world.destroyBlock(pos, false);
            }
            else if (openTicks <= 140 && openTicks > 60 && openTicks % 10 == 0) {
                BlockPos check = pos.add(CAP_OFFSETS[openTicks / 10 - 7]);
                IBlockState state = world.getBlockState(check);
                if (state.getBlock() == TABlocks.CAPSTONE && state.getValue(IAltarBlock.ALTAR) == false)
                    world.destroyBlock(check, false);
            }
            else if (openTicks == 175)
                world.playSound(null, pos, SoundsTC.shock, SoundCategory.BLOCKS, 1.0F, 0.35F);
            else if (openTicks == 250)
                world.playSound(null, pos, SoundsTC.evilportal, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }
    
    @Override
    public boolean onCasterRightClick(World world, ItemStack stack, EntityPlayer player,
            BlockPos pos, EnumFacing facing, EnumHand hand) {
        
        if (!world.isRemote) {
            open();
            world.playSound(null, pos, SoundsTC.wand, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        
        return true;
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock() || !newState.getValue(IAltarBlock.ALTAR);
    }
    
    @Override
    public double getMaxRenderDistanceSquared() {
        return 16384.0;
    }
    
    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }
    
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos).grow(1.0, 0.0, 1.0).expand(0.0, 2.0, 0.0);
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setInteger("openTicks", openTicks);
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        openTicks = tag.getInteger("openTicks");
    }
    
    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("openTicks", openTicks);
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        openTicks = pkt.getNbtCompound().getInteger("openTicks");
        world.markBlockRangeForRenderUpdate(pos, pos);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("openTicks", openTicks);
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        openTicks = compound.getInteger("openTicks");
    }
    
}
