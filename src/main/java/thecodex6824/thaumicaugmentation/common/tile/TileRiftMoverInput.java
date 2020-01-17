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
import net.minecraft.client.Minecraft;
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
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.casters.IInteractWithCaster;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.tile.CapabilityRiftJar;
import thecodex6824.thaumicaugmentation.api.tile.IRiftJar;
import thecodex6824.thaumicaugmentation.api.util.FluxRiftReconstructor;
import thecodex6824.thaumicaugmentation.api.util.RiftHelper;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.tile.trait.IBreakCallback;

public class TileRiftMoverInput extends TileEntity implements ITickable, IInteractWithCaster, IBreakCallback {

    protected boolean operating;
    protected int oldSize;
    protected int oldSeed;
    protected float oldStability;
    protected EntityFluxRift rift;
    protected UUID loadedRiftUUID;
    protected int ticks;
    
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
            if (below != null) {
                IRiftJar jar = below.getCapability(CapabilityRiftJar.RIFT_JAR, null);
                if (jar != null) {
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
            }
            
            return true;
        }
        else if (world.isRemote && !operating)
            return true;
        else
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
            if (!world.isRemote && ticks++ % 10 == 0) {
                TileEntity below = world.getTileEntity(pos.down());
                if (rift == null || rift.isDead || rift.getRiftSize() < 1 || rift.getCollapse() ||
                        below == null || !below.hasCapability(CapabilityRiftJar.RIFT_JAR, null) ||
                        below.getCapability(CapabilityRiftJar.RIFT_JAR, null).hasRift()) {
                    
                    if (rift == null || rift.isDead)
                        AuraHelper.polluteAura(world, pos, oldSize, true);
                    else {
                        if (rift.getCollapse())
                            rift.setCollapse(false);
                        
                        rift.setRiftSize(oldSize);
                        rift.setRiftStability(-150.0F);
                        rift.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 0.75F);
                        Vec3d riftCenter = RiftHelper.getRiftCenter(rift);
                        TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.EXPLOSION,
                                riftCenter.x, riftCenter.y, riftCenter.z), rift);
                        TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.SPARK, riftCenter.x,
                                riftCenter.y, riftCenter.z, 5.0F, Aspect.ELDRITCH.getColor()),
                                new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64.0));
                        rift = null;
                    }
                    
                    operating = false;
                    markDirty();
                    world.playSound(null, pos, SoundsTC.craftfail, SoundCategory.BLOCKS, 0.5F, 1.0F);
                    world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
                }
                else if (AuraHelper.drainVis(world, pos, 0.25F, false) >= 0.25F - 0.0001){
                    rift.setRiftSize(rift.getRiftSize() - 1);
                    rift.setRiftStability(rift.getRiftStability() - 0.5F);
                    if (rift.getRiftSize() == 0) {
                        below.getCapability(CapabilityRiftJar.RIFT_JAR, null).setRift(new FluxRiftReconstructor(oldSeed, oldSize));
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
                if (rift != null && ticks % getParticleDelay(rift.getRiftSize()) == 0) {
                    Vec3d particlePos = RiftHelper.pickRandomPointOnRift(rift).add(rift.posX, rift.posY, rift.posZ);
                    Vec3d dir = particlePos.subtract(new Vec3d(pos).add(0.5, 0.5, 0.5)).normalize();
                    ThaumicAugmentation.proxy.getRenderHelper().renderRiftMoverParticle(world, particlePos.x,
                            particlePos.y, particlePos.z, -dir.x * 0.25, -dir.y * 0.25, -dir.z * 0.25);
                    if (!TAConfig.reducedEffects.getValue()) {
                        particlePos = RiftHelper.pickRandomPointOnRift(rift).add(rift.posX, rift.posY, rift.posZ);
                        ThaumicAugmentation.proxy.getRenderHelper().renderSpark(world, particlePos.x, particlePos.y, particlePos.z, 5.0F + world.rand.nextFloat() * 7.5F,
                                Aspect.ELDRITCH.getColor(), false);
                        if (world.rand.nextBoolean()) {
                            particlePos = RiftHelper.pickRandomPointOnRiftWithInstability(rift, Minecraft.getMinecraft().player.ticksExisted,
                                    Minecraft.getMinecraft().getRenderPartialTicks()).add(rift.posX, rift.posY, rift.posZ);
                            ThaumicAugmentation.proxy.getRenderHelper().renderArc(world, particlePos.x,
                                    particlePos.y, particlePos.z, pos.getX() + 0.5, pos.getY() + 0.75,
                                    pos.getZ() + 0.5, Aspect.ELDRITCH.getColor(), rift.height / 2);
                        }
                    }
                }
                
                ++ticks;
            }
        }
    }
    
    @Override
    public void onBlockBroken() {
        if (!world.isRemote && operating) {
            if (rift == null || rift.isDead)
                AuraHelper.polluteAura(world, pos, oldSize, true);
            else {
                if (rift.getCollapse())
                    rift.setCollapse(false);
                
                rift.setRiftSize(oldSize);
                rift.setRiftStability(-150.0F);
                rift.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 0.75F);
                Vec3d riftCenter = RiftHelper.getRiftCenter(rift);
                TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.EXPLOSION,
                        riftCenter.x, riftCenter.y, riftCenter.z), rift);
                TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.SPARK, riftCenter.x,
                        riftCenter.y, riftCenter.z, 5.0F, Aspect.ELDRITCH.getColor()),
                        new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64.0));
                rift = null;
            }
            
            operating = false;
            world.playSound(null, pos, SoundsTC.craftfail, SoundCategory.BLOCKS, 0.5F, 1.0F);
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
