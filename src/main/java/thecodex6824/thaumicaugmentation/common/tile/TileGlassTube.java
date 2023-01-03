/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.casters.IInteractWithCaster;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.tiles.essentia.TileTube;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.aspect.AspectUtil;
import thecodex6824.thaumicaugmentation.api.tile.IEssentiaTube;
import thecodex6824.thaumicaugmentation.common.network.PacketEssentiaUpdate;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

public class TileGlassTube extends TileEntity implements IEssentiaTube, IInteractWithCaster,
    ITickable {

    protected byte sides = 0b00111111;
    protected Aspect containedAspect;
    protected int amount;
    protected int suction;
    protected Aspect suctionAspect;
    protected int ventingTicks;
    protected int ticks = ThreadLocalRandom.current().nextInt(20);
    
    // client vars
    protected int ventingColor;
    protected float ventX = -1.0F;
    protected float ventY = -1.0F;
    protected int fluidStartTicks = 20;
    protected boolean fluidStartTicksUp = true;
    protected Aspect lastFluid;
    
    protected void syncEssentia() {
        PacketEssentiaUpdate update = new PacketEssentiaUpdate(pos, AspectUtil.getAspectID(containedAspect), amount);
        TANetwork.INSTANCE.sendToAllTracking(update, new TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 64.0));
    }
    
    @Override
    public void update() {
        if (ventingTicks > 0)
            --ventingTicks;
        
        if (!world.isRemote && ventingTicks == 0) {
            if (++ticks % 2 == 0) {
                suction = 0;
                suctionAspect = null;
                for (EnumFacing facing : EnumFacing.VALUES) {
                    if (isConnectable(facing)) {
                        TileEntity te = ThaumcraftApiHelper.getConnectableTile(world, pos, facing);
                        if (te != null) {
                            IEssentiaTransport t = (IEssentiaTransport) te;
                            if (getEssentiaAmount(facing) == 0 || t.getSuctionType(facing.getOpposite()) == null ||
                                    getEssentiaType(facing) == t.getSuctionType(facing.getOpposite())) {
                              
                                int suck = t.getSuctionAmount(facing.getOpposite());
                                if (suck > 0 && suck > getSuctionAmount(facing) + 1) {
                                    Aspect a = t.getSuctionType(facing.getOpposite());
                                    setSuction(a, suck - 1);
                                } 
                            }
                            
                            int suck = t.getSuctionAmount(facing.getOpposite());
                            int ourSuck = getSuctionAmount(facing);
                            if (ourSuck > 0 && (ourSuck == suck || ourSuck == suck - 1) && getSuctionType(facing) != t.getSuctionType(facing.getOpposite())) {
                                world.addBlockEvent(pos, getBlockType(), 1, suctionAspect != null ?
                                        suctionAspect.getColor() : 0xAAAAAA);
                                ventingTicks = 40;
                                world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS,
                                        0.1F, 1.0F + world.rand.nextFloat() * 0.1F);
                                markDirty();
                            }
                        } 
                    }
                }
                
                if (containedAspect != null && amount == 0)
                    containedAspect = null;
            }
            else if (ticks % 5 == 0 && suction != 0 && amount == 0) {
                for (EnumFacing facing : EnumFacing.VALUES) {
                    if (isConnectable(facing)) {
                        TileEntity te = ThaumcraftApiHelper.getConnectableTile(world, pos, facing);
                        if (te != null) {
                            IEssentiaTransport t = (IEssentiaTransport) te;
                            if (t.canOutputTo(facing.getOpposite())) {
                                Aspect suckType = getSuctionType(facing);
                                int suck = getSuctionAmount(facing);
                                if ((suckType == null || suckType == t.getEssentiaType(facing.getOpposite()) ||
                                        t.getEssentiaType(facing.getOpposite()) == null) && suck > t.getSuctionAmount(facing.getOpposite()) && suck > t.getMinimumSuction()) {
                                    
                                    if (suckType == null) {
                                        suckType = t.getEssentiaType(facing.getOpposite());
                                        if (suckType == null)
                                            suckType = t.getEssentiaType(null);
                                    }
                                    
                                    int added = addEssentia(suckType, t.takeEssentia(suckType, 1, facing.getOpposite()), facing);
                                    if (added > 0) {
                                        if (world.rand.nextInt(100) == 0) {
                                            world.playSound(null, pos, SoundsTC.creak, SoundCategory.AMBIENT,
                                                    1.0F, 1.3F + world.rand.nextFloat() * 0.2F);
                                        }
                                        
                                        break;
                                    }
                                }
                            }  
                        } 
                    }
                }
            }
        }
        else if (world.isRemote && ThaumicAugmentation.proxy.isInGame()) {
            if (fluidStartTicksUp) {
                if (fluidStartTicks < 20)
                    ++fluidStartTicks;
            }
            else {
                if (fluidStartTicks > 0)
                    --fluidStartTicks;
            }
            
            if (ventingTicks > 0) {
                if (ventX < 0.0F)
                    ventX = world.rand.nextFloat() * (float) Math.PI * 2.0F;
                if (ventY < 0.0F)
                    ventY = world.rand.nextFloat() * (float) Math.PI * 2.0F;
                
                double fX = (-MathHelper.sin(ventX) * MathHelper.cos(ventY));
                double fZ = (MathHelper.cos(ventX) * MathHelper.cos(ventY));
                double fY = -MathHelper.sin(ventY);
                FXDispatcher.INSTANCE.drawVentParticles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        fX / 5.0, fY / 5.0, fZ / 5.0, ventingColor);
            }
        }
    }
    
    @Override
    public int addEssentia(Aspect aspect, int addAmount, @Nullable EnumFacing from) {
        if (amount == 0 && addAmount != 0 && canInputFrom(from)) {
            containedAspect = aspect;
            ++amount;
            if (!world.isRemote) {
                markDirty();
                syncEssentia();
            }
            return 1;
        }
        else
            return 0;
    }
    
    @Override
    public boolean canInputFrom(@Nullable EnumFacing face) {
        return face != null && isSideOpen(face);
    }
    
    @Override
    public boolean canOutputTo(@Nullable EnumFacing face) {
        return face != null && isSideOpen(face);
    }
    
    @Override
    public int getEssentiaAmount(@Nullable EnumFacing face) {
        if (face != null)
            return amount;
        else
            return 0;
    }
    
    @Override
    @Nullable
    public Aspect getEssentiaType(@Nullable EnumFacing face) {
        if (face != null)
            return containedAspect;
        else
            return null;
    }
    
    @Override
    public int getMinimumSuction() {
        return 0;
    }
    
    @Override
    public int getSuctionAmount(EnumFacing face) {
        if (face != null)
            return suction;
        else
            return 0;
    }
    
    @Override
    @Nullable
    public Aspect getSuctionType(EnumFacing face) {
        if (face != null)
            return suctionAspect;
        else
            return null;
    }
    
    @Override
    public boolean isConnectable(@Nullable EnumFacing face) {
        return face != null && isSideOpen(face);
    }
    
    @Override
    public boolean isSideOpen(EnumFacing side) {
        return ((sides & (1 << side.getIndex())) >> side.getIndex()) == 1;
    }
    
    @Override
    public boolean onCasterRightClick(World world, ItemStack stack, EntityPlayer player, BlockPos pos, EnumFacing face,
            EnumHand hand) {
        
        Vec3d start = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec();
        double reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        RayTraceResult result = world.getBlockState(pos).collisionRayTrace(world, pos, start,
                start.add(look.x * reach, look.y * reach, look.z * reach));
        if (result == null || result.getBlockPos() == null)
            return false;
        
        if (result.subHit >= 0 && result.subHit < 6) {
            world.playSound(null, pos, SoundsTC.tool, SoundCategory.BLOCKS, 0.5F, 0.9F + world.rand.nextFloat() * 0.2F);
            player.swingArm(hand);
            markDirty();
            EnumFacing dir = EnumFacing.byIndex(result.subHit);
            setSideOpen(dir, !isSideOpen(dir));
            TileEntity tile = world.getTileEntity(pos.offset(dir));
            if (tile != null) {
                // really tc, no tube interface? Come on now...
                if (tile instanceof TileTube) {
                    ((TileTube) tile).openSides[dir.getOpposite().ordinal()] = isSideOpen(dir);
                    ((TileTube) tile).syncTile(true);
                    tile.markDirty();
                }
                else if (tile instanceof IEssentiaTube)
                    ((IEssentiaTube) tile).setSideOpen(dir.getOpposite(), isSideOpen(dir));
            } 
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public void setEssentiaDirect(@Nullable Aspect aspect, int amount) {
        fluidStartTicksUp = aspect != null && amount != 0;
        
        lastFluid = containedAspect;
        containedAspect = aspect;
        this.amount = amount;
        if (!world.isRemote) {
            markDirty();
            syncEssentia();
        }
    }
    
    @Override
    public void setSideOpen(EnumFacing side, boolean open) {
        if (open)
            sides |= (1 << side.getIndex());
        else
            sides &= ~(1 << side.getIndex());
        
        markDirty();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }
    
    @Override
    public void setSuction(Aspect aspect, int amount) {
        suctionAspect = aspect;
        suction = amount;
        markDirty();
    }
    
    @Override
    public int takeEssentia(Aspect aspect, int takeAmount, EnumFacing face) {
        if (containedAspect == aspect && amount == 1 && takeAmount > 0 && canOutputTo(face)) {
            --amount;
            containedAspect = null;
            if (!world.isRemote) {
                markDirty();
                syncEssentia();
            }
            return 1;
        }
        else
            return 0;
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    public int getFluidStartTicks() {
        return fluidStartTicks;
    }
    
    public void setFluidStartTicks(int newTicks) {
        fluidStartTicks = newTicks;
    }
    
    public Aspect getLastFluid() {
        return lastFluid;
    }
    
    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            ventingTicks = 40;
            ventingColor = type;
            return true;
        }
        
        return false;
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setByte("sides", sides);
        tag.setString("containedAspect", containedAspect != null ? containedAspect.getTag() : "");
        tag.setInteger("amount", amount);
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        sides = tag.getByte("sides");
        containedAspect = Aspect.getAspect(tag.getString("containedAspect"));
        amount = tag.getInteger("amount");
        lastFluid = containedAspect;
    }
    
    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("sides", sides);
        tag.setString("containedAspect", containedAspect != null ? containedAspect.getTag() : "");
        tag.setInteger("amount", amount);
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        sides = pkt.getNbtCompound().getByte("sides");
        containedAspect = Aspect.getAspect(pkt.getNbtCompound().getString("containedAspect"));
        amount = pkt.getNbtCompound().getInteger("amount");
        world.markBlockRangeForRenderUpdate(pos, pos);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setByte("sides", sides);
        compound.setString("containedAspect", containedAspect != null ? containedAspect.getTag() : "");
        compound.setInteger("amount", amount);
        compound.setString("suctionAspect", suctionAspect != null ? suctionAspect.getTag() : "");
        compound.setInteger("suction", suction);
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        sides = compound.getByte("sides");
        containedAspect = Aspect.getAspect(compound.getString("containedAspect"));
        amount = compound.getInteger("amount");
        suctionAspect = Aspect.getAspect(compound.getString("suctionAspect"));
        suction = compound.getInteger("suction");
    }
    
    @Override
    public boolean hasFastRenderer() {
        return true;
    }
    
}
