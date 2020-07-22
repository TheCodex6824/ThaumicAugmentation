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

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.api.util.RiftHelper;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class TileRiftFeeder extends TileEntity implements ITickable, IEssentiaTransport {

    protected static final int MAX_ESSENTIA = 200;
    
    protected int storedEssentia;
    
    protected double getDistForFace(EnumFacing face, Entity entity) {
        return getDistForFace(face, entity.getPositionVector());
    }
    
    protected double getDistForFace(EnumFacing face, Vec3d vec) {
        if (face.getAxis() == Axis.X)
            return Math.abs(pos.getX() - vec.x);
        else if (face.getAxis() == Axis.Y)
            return Math.abs(pos.getY() - vec.y);
        else
            return Math.abs(pos.getZ() - vec.z);
    }
    
    @Nullable
    protected EntityFluxRift findClosestRift(EnumFacing face) {
        BlockPos pos1 = pos.offset(face).add(1.0 - face.getXOffset(), 1.0 - face.getYOffset(), 1.0 - face.getZOffset());
        BlockPos pos2 = pos.offset(face, 12).add(1.0 + face.getXOffset(), 1.0 + face.getYOffset(), 1.0 + face.getZOffset());
        List<EntityFluxRift> rifts = world.getEntitiesWithinAABB(EntityFluxRift.class, 
                new AxisAlignedBB(pos1.getX() - 1, pos1.getY() - 1, pos1.getZ() - 1, pos2.getX() + 2, pos2.getY() + 2, pos2.getZ() + 2));
        if (!rifts.isEmpty()) {
            rifts.sort((rift1, rift2) -> Double.compare(getDistForFace(face, rift1), getDistForFace(face, rift2)));
            RayTraceResult trace = world.rayTraceBlocks(new Vec3d(pos.offset(face)), new Vec3d(pos.offset(face, 10)));
            EntityFluxRift chosenOne = rifts.get(0);
            if (trace == null || trace.hitVec == null || getDistForFace(face, chosenOne) < getDistForFace(face, trace.hitVec))
                return chosenOne;
        }
        
        return null;
    }
    
    @Override
    public void update() {
        if (!world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (facing != state.getValue(IDirectionalBlock.DIRECTION)) {
                    TileEntity tile = ThaumcraftApiHelper.getConnectableTile(world, pos, facing);
                    if (tile != null) {
                        IEssentiaTransport t = (IEssentiaTransport) tile;
                        if (t.canOutputTo(facing.getOpposite()) && t.getEssentiaType(facing) == Aspect.FLUX) {
                            if (t.getEssentiaAmount(facing.getOpposite()) > 0 && t.getSuctionAmount(facing.getOpposite()) < getSuctionAmount(facing) &&
                                    getSuctionAmount(facing) >= t.getMinimumSuction()) {
                                
                                addEssentiaDirect(t.takeEssentia(Aspect.FLUX, 1, facing.getOpposite()));
                            }
                        }
                    }
                }
            }
            
            if (world.getTotalWorldTime() % 5 == 0) {
                if (storedEssentia > 0 && state.getValue(IEnabledBlock.ENABLED)) {
                    EntityFluxRift rift = findClosestRift(state.getValue(IDirectionalBlock.DIRECTION));
                    if (rift != null && rift.getRiftSize() < 200 && !rift.getCollapse()) {
                        int required = (int) Math.sqrt(rift.getRiftSize());
                        if (storedEssentia >= required) {
                            storedEssentia -= required;
                            rift.setRiftSize(rift.getRiftSize() + 1);
                            Vec3d particlePos = RiftHelper.getRiftCenter(rift).add(rift.posX, rift.posY, rift.posZ);
                            TANetwork.INSTANCE.sendToAllAround(new PacketParticleEffect(ParticleEffect.ESSENTIA_TRAIL, 
                                    pos.getX(), pos.getY(), pos.getZ(), particlePos.x, particlePos.y, particlePos.z, Aspect.FLUX.getColor()),
                                    new TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 48));
                            world.notifyBlockUpdate(pos, state, state, 2);
                            markDirty();
                        }
                    }
                }
            }
        }
    }
    
    protected void addEssentiaDirect(int amount) {
        if (amount > 0) {
            storedEssentia += amount;
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 2);
            markDirty();
        }
    }
    
    @Override
    public int addEssentia(Aspect aspect, int amount, EnumFacing face) {
        int taken = canInputFrom(face) ? Math.min(amount, MAX_ESSENTIA - storedEssentia) : 0;
        addEssentiaDirect(taken);
        return taken;
    }
    
    @Override
    public boolean canInputFrom(EnumFacing face) {
        return isConnectable(face);
    }
    
    @Override
    public boolean canOutputTo(EnumFacing face) {
        return false;
    }
    
    @Override
    public int getEssentiaAmount(EnumFacing face) {
        return isConnectable(face) ? storedEssentia : 0;
    }
    
    @Override
    public Aspect getEssentiaType(EnumFacing face) {
        return isConnectable(face) ? Aspect.FLUX : null;
    }
    
    @Override
    public int getMinimumSuction() {
        return 0;
    }
    
    @Override
    public int getSuctionAmount(EnumFacing face) {
        if (isConnectable(face))
            return storedEssentia < MAX_ESSENTIA ? 128 : 0;
        else
            return 0;
    }
    
    @Override
    public Aspect getSuctionType(EnumFacing face) {
        return Aspect.FLUX;
    }
    
    @Override
    public boolean isConnectable(EnumFacing face) {
        return world.getBlockState(pos).getValue(IDirectionalBlock.DIRECTION) != face;
    }
    
    @Override
    public void setSuction(Aspect aspect, int suction) {}
    
    @Override
    public int takeEssentia(Aspect aspect, int amount, EnumFacing face) {
        return 0;
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("essentia", storedEssentia);
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        storedEssentia = pkt.getNbtCompound().getInteger("essentia");
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("essentia", storedEssentia);
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        storedEssentia = compound.getInteger("essentia");
    }
    
}
