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
import java.util.UUID;

import javax.annotation.Nullable;

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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.IInteractWithCaster;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.tile.IRiftJar;
import thecodex6824.thaumicaugmentation.api.util.FluxRiftReconstructor;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class TileRiftMoverInput extends TileEntity implements ITickable, IInteractWithCaster {

    protected boolean operating;
    protected int oldSize;
    protected int oldSeed;
    protected float oldStability;
    protected EntityFluxRift rift;
    protected UUID loadedRiftUUID;
    
    protected EntityFluxRift findRift() {
        BlockPos pos1 = pos.add(-1, 1, -1);
        BlockPos pos2 = pos.add(1, 6, 1);
        List<EntityFluxRift> rifts = world.getEntitiesWithinAABB(EntityFluxRift.class, 
                new AxisAlignedBB(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX() + 1, pos2.getY() + 1, pos2.getZ() + 1));
        if (!rifts.isEmpty()) {
            rifts.sort((rift1, rift2) -> (int) (rift1.getPosition().distanceSq(pos) - rift2.getPosition().distanceSq(pos)));
            for (EntityFluxRift maybe : rifts) {
                RayTraceResult trace = world.rayTraceBlocks(new Vec3d(pos.add(0, 1, 0)), new Vec3d(maybe.getPosition()));
                if (trace == null || trace.hitVec == null)
                    return maybe;
            }
        }
        
        return null;
    }
    
    @Override
    public boolean onCasterRightClick(World w, ItemStack stack, EntityPlayer player, BlockPos position,
            EnumFacing face, EnumHand hand) {
        
        if (!world.isRemote && !operating) {
            TileEntity below = world.getTileEntity(pos.down());
            if (below instanceof IRiftJar && !((IRiftJar) below).hasRift()) {
                rift = findRift();
                if (rift != null && !rift.isDead && rift.getRiftSize() > 0 && !rift.getCollapse()) {
                    operating = true;
                    oldSize = rift.getRiftSize();
                    oldSeed = rift.getRiftSeed();
                    oldStability = rift.getRiftStability();
                    markDirty();
                    world.playSound(null, pos, SoundsTC.craftstart, SoundCategory.BLOCKS, 0.5F, 1.0F);
                    world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
                }
                else
                    rift = null;
            }
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public void onLoad() {
        if (operating && loadedRiftUUID != null) {
            EntityFluxRift check = findRift();
            if (check != null && check.getUniqueID().equals(loadedRiftUUID))
                rift = check;
            
            loadedRiftUUID = null;
        }
    }
    
    protected int getParticleDelay(int size) {
        if (size < 11)
            return 20;
        else if (size < 30)
            return 10;
        else
            return 5;
    }
    
    @Override
    public void update() {
        if (operating) {
            if (!world.isRemote && world.getTotalWorldTime() % 10 == 0) {
                TileEntity below = world.getTileEntity(pos.down());
                if (rift == null || rift.isDead || rift.getRiftSize() < 1 || rift.getCollapse() ||
                        !(below instanceof IRiftJar) || ((IRiftJar) below).hasRift()) {
                    
                    if (rift == null || rift.isDead) {
                        rift = new EntityFluxRift(world);
                        rift.setPositionAndRotation(pos.getX() + 0.5, pos.getY() + 2.5, pos.getZ() + 0.5,
                                world.rand.nextInt(360), 0);
                        if (world.spawnEntity(rift))
                            rift.setRiftSeed(oldSeed);
                        else
                            rift = null;
                    }
                    else if (rift.getCollapse())
                        rift.setCollapse(false);
                    
                    if (rift != null) {
                        rift.setRiftSize(oldSize);
                        rift.setRiftStability(-150.0F);
                        rift.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 0.75F);
                        TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.EXPLOSION,
                                rift.posX, rift.posY, rift.posZ), rift);
                        TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.SPARK, rift.posX,
                                rift.posY, rift.posZ, 5.0F, Aspect.ELDRITCH.getColor()),
                                new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64.0));
                    }
                    
                    rift = null;
                    operating = false;
                    markDirty();
                    world.playSound(null, pos, SoundsTC.craftfail, SoundCategory.BLOCKS, 0.5F, 1.0F);
                    world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
                }
                else {
                    rift.setRiftSize(rift.getRiftSize() - 1);
                    rift.setRiftStability(rift.getRiftStability() - 0.5F);
                    if (rift.getRiftSize() == 0) {
                        ((IRiftJar) world.getTileEntity(pos.down())).setRift(new FluxRiftReconstructor(oldSeed, oldSize));
                        rift = null;
                        operating = false;
                        markDirty();
                        world.playSound(null, pos, SoundsTC.wand, SoundCategory.BLOCKS, 0.5F, 1.0F);
                        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
                    }
                }
            }
            else if (world.isRemote) {
                if (loadedRiftUUID != null) {
                    rift = findRift();
                    if (!rift.getUniqueID().equals(loadedRiftUUID))
                        rift = null;
                    
                    loadedRiftUUID = null;
                }
                if (rift != null && world.getTotalWorldTime() % getParticleDelay(rift.getRiftSize()) == 0) {
                    Vec3d riftCenter = rift.getEntityBoundingBox().getCenter();
                    Vec3d particlePos = new Vec3d(riftCenter.x + -rift.width / 2.0F + world.rand.nextFloat() * (rift.width / 2.0F + rift.width / 2.0F),
                            riftCenter.y + world.rand.nextFloat() * rift.height,
                            riftCenter.z + -rift.width / 2.0F + world.rand.nextFloat() * (rift.width / 2.0F + rift.width / 2.0F));
                    Vec3d dir = particlePos.subtract(new Vec3d(pos).add(0.5, 0.5, 0.5)).normalize();
                    FXGeneric fx = new FXGeneric(world, particlePos.x, particlePos.y, particlePos.z, -dir.x * 0.25, -dir.y * 0.25, -dir.z * 0.25);
                    fx.setMaxAge(20 + world.rand.nextInt(6));
                    fx.setRBGColorF(0.044F, 0.036F, 0.063F);
                    fx.setAlphaF(0.75F);
                    fx.setGridSize(64);
                    fx.setParticles(264, 8, 1);
                    fx.setScale(2.0F);
                    fx.setLayer(1);
                    fx.setLoop(true);
                    fx.setNoClip(false); // this is REALLY poorly named, it actually should be "setCollides", as that's what it does
                    fx.setRotationSpeed(world.rand.nextFloat(), world.rand.nextBoolean() ? 1.0F : -1.0F);
                    ParticleEngine.addEffect(world, fx);
                    
                    if (!TAConfig.reducedEffects.getValue()) {
                        ThaumicAugmentation.proxy.getRenderHelper().renderSpark(world, pos.getX() + world.rand.nextDouble(),
                                pos.getY() + 0.5, pos.getZ() + world.rand.nextDouble(), 2.0F, Aspect.ELDRITCH.getColor(), false);
                        particlePos = new Vec3d(riftCenter.x + -rift.width / 2.0F + world.rand.nextFloat() * (rift.width / 2.0F + rift.width / 2.0F),
                                riftCenter.y + world.rand.nextFloat() * rift.height,
                                riftCenter.z + -rift.width / 2.0F + world.rand.nextFloat() * (rift.width / 2.0F + rift.width / 2.0F));
                        ThaumicAugmentation.proxy.getRenderHelper().renderSpark(world, particlePos.x, particlePos.y, particlePos.z, 5.0F,
                                Aspect.ELDRITCH.getColor(), false);
                    }
                }
            }
        }
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("operating", operating);
        if (operating && rift != null)
            tag.setUniqueId("rift", rift.getUniqueID());
        
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        operating = pkt.getNbtCompound().getBoolean("operating");
        if (operating) {
            EntityFluxRift check = findRift();
            if (check != null && check.getUniqueID().equals(pkt.getNbtCompound().getUniqueId("rift")))
                rift = check;
        }
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setBoolean("operating", operating);
        if (operating && (rift != null || loadedRiftUUID != null))
            tag.setUniqueId("rift", rift != null ? rift.getUniqueID() : loadedRiftUUID);
        
        return tag;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        operating = tag.getBoolean("operating");
        if (operating)
            loadedRiftUUID = tag.getUniqueId("rift");
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("operating", operating);
        if (operating) {
            compound.setInteger("size", oldSize);
            compound.setInteger("seed", oldSeed);
            compound.setFloat("stab", oldStability);
            if (rift != null)
                compound.setUniqueId("rift", rift.getUniqueID());
        }
        
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        operating = compound.getBoolean("operating");
        if (operating) {
            oldSize = compound.getInteger("size");
            oldSeed = compound.getInteger("seed");
            oldStability = compound.getFloat("stab");
            loadedRiftUUID = compound.getUniqueId("rift");
        }
    }
    
}
