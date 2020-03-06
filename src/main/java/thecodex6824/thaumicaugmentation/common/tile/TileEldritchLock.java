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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import thaumcraft.api.casters.IInteractWithCaster;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.block.property.IEldritchLockType;
import thecodex6824.thaumicaugmentation.api.block.property.IEldritchLockType.LockType;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class TileEldritchLock extends TileEntity implements ITickable, IInteractWithCaster {

    protected int ticks;
    protected int openTicks;
    
    public TileEldritchLock() {
        super();
        openTicks = Integer.MIN_VALUE;
    }
    
    public boolean isClosed() {
        return openTicks == Integer.MIN_VALUE;
    }
    
    public int getOpenTicks() {
        return openTicks;
    }
    
    public void open() {
        openTicks = 140;
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }
    
    protected void checkAndPlaceBarriers(EnumFacing face) {
        boolean xAxis = face.getAxis() == Axis.X;
        MutableBlockPos check = new MutableBlockPos();
        for (int c = -2; c < 3; ++c) {
            check.setPos(pos.getX() + (xAxis ? 0 : c), 0, pos.getZ() + (xAxis ? c : 0));
            for (int y = -2; y < 3; ++y) {
                check.setY(pos.getY() + y);
                IBlockState state = world.getBlockState(check);
                if (state.getBlock().isAir(state, world, check) && world.isBlockLoaded(check)) {
                    world.setBlockState(check, TABlocks.RIFT_BARRIER.getDefaultState().withProperty(
                            IHorizontallyDirectionalBlock.DIRECTION, face), 3);
                    TileEntity placed = world.getTileEntity(check);
                    if (placed instanceof TileRiftBarrier)
                        ((TileRiftBarrier) placed).setLock(pos);
                }
            }
        }
    }
    
    protected void destroyBarriers(EnumFacing face) {
        boolean xAxis = face.getAxis() == Axis.X;
        MutableBlockPos check = new MutableBlockPos();
        ArrayList<Vec3d> particles = new ArrayList<>();
        for (int c = -2; c < 3; ++c) {
            check.setPos(pos.getX() + (xAxis ? 0 : c), 0, pos.getZ() + (xAxis ? c : 0));
            for (int y = -2; y < 3; ++y) {
                check.setY(pos.getY() + y);
                IBlockState state = world.getBlockState(check);
                if (state.getBlock() == TABlocks.RIFT_BARRIER && world.isBlockLoaded(check)) {
                    world.setBlockToAir(check);
                    particles.add(new Vec3d(check.getX(), check.getY(), check.getZ()));
                }
            }
        }
        
        List<Vec3d> list = particles;
        if (list.size() * 3 > PacketParticleEffect.maxPacketData)
            list = list.subList(0, PacketParticleEffect.maxPacketData / 3);
            
        double[] coords = new double[list.size() * 3];
        for (int i = 0; i < list.size(); ++i) {
            Vec3d vec = list.get(i);
            coords[i * 3] = vec.x;
            coords[i * 3 + 1] = vec.y;
            coords[i * 3 + 2] = vec.z;
        }
        
        TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.FLUX_BATCH, coords), new TargetPoint(world.provider.getDimension(),
                pos.getX(), pos.getY(), pos.getZ(), 64.0));
    }
    
    protected boolean shouldPlayRetractSound() {
        IBlockState state = world.getBlockState(pos);
        LockType type = state.getPropertyKeys().contains(IEldritchLockType.LOCK_TYPE) ?
                state.getValue(IEldritchLockType.LOCK_TYPE) : LockType.BOSS;
        switch (type) {
            case LABYRINTH: return openTicks > 20 && openTicks % 20 == 0;
            case PRISON: return openTicks > 15 && (openTicks + 5) % 10 == 0;
            case LIBRARY: return openTicks > 15 && (openTicks + 5) % 10 == 0;
            case BOSS: return openTicks > 20 && openTicks % 5 == 0;
            default: return false;
        }
    }
        
    @Override
    public void update() {
        if (!world.isRemote && ticks++ % 20 == 0 && (openTicks == Integer.MIN_VALUE || openTicks > 20)) {
            EnumFacing dir = world.getBlockState(pos).getValue(IHorizontallyDirectionalBlock.DIRECTION);
            checkAndPlaceBarriers(dir);
        }
        
        if (openTicks != Integer.MIN_VALUE) {
            --openTicks;
            if (!world.isRemote) {
                if (openTicks < 101 && shouldPlayRetractSound())
                    world.playSound(null, pos, SoundsTC.grind, SoundCategory.BLOCKS, 1.0F, 0.9F);
                else if (openTicks <= 0) {
                    EnumFacing dir = world.getBlockState(pos).getValue(IHorizontallyDirectionalBlock.DIRECTION);
                    destroyBarriers(dir);
                    world.playSound(null, pos, SoundsTC.ice, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.FLUX, pos.getX(),
                            pos.getY(), pos.getZ()), new TargetPoint(world.provider.getDimension(),
                            pos.getX(), pos.getY(), pos.getZ(), 64.0));
                    world.setBlockToAir(pos);
                }
            }
        }
    }
    
    @Override
    public boolean onCasterRightClick(World paramWorld, ItemStack paramItemStack, EntityPlayer paramEntityPlayer,
            BlockPos paramBlockPos, EnumFacing paramEnumFacing, EnumHand paramEnumHand) {
  
        // so people don't cloud themselves when opening the lock
        return true;
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
        IBlockState state = world.getBlockState(pos);
        if (state.getPropertyKeys().contains(IHorizontallyDirectionalBlock.DIRECTION)) {
            EnumFacing face = state.getValue(IHorizontallyDirectionalBlock.DIRECTION);
            return new AxisAlignedBB(pos).grow(Math.abs(face.getXOffset()) * 2, 2, face.getZOffset() * 2);
        }
        else
            return super.getRenderBoundingBox();
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
