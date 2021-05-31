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
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.casters.IInteractWithCaster;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.EntityUtils;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.tile.CapabilityRiftJar;
import thecodex6824.thaumicaugmentation.api.tile.IRiftJar;
import thecodex6824.thaumicaugmentation.api.util.FluxRiftReconstructor;
import thecodex6824.thaumicaugmentation.api.util.RiftHelper;
import thecodex6824.thaumicaugmentation.common.tile.trait.IBreakCallback;
import thecodex6824.thaumicaugmentation.common.util.ISoundHandle;

public class TileRiftMoverOutput extends TileEntity implements ITickable, IInteractWithCaster, IBreakCallback {

    protected boolean operating;
    protected EntityFluxRift rift;
    protected int targetSize;
    protected int lastSize;
    protected UUID loadedRiftUUID;
    protected int ticks;
    protected ISoundHandle loop;
    
    public TileRiftMoverOutput() {
        ticks = ThreadLocalRandom.current().nextInt(20);
    }
    
    @Nullable
    protected Vec3d findRiftPos() {
        Vec3d position = new Vec3d(pos.up());
        for (int offset = 5; offset > 0; --offset) {
            Vec3d test = new Vec3d(pos.up(offset));
            RayTraceResult trace = world.rayTraceBlocks(position, test);
            if (trace == null || trace.hitVec == null)
                return test.add(0.5, 0.5, 0.5);
        }
        
        return null;
    }
    
    @Nullable
    public Vec3d findLocalRiftPos() {
        Vec3d vec = findRiftPos();
        return vec != null ? vec.subtract(pos.getX(), pos.getY(), pos.getZ()) : null;
    }
    
    @Override
    public boolean onCasterRightClick(World w, ItemStack stack, EntityPlayer player, BlockPos position,
            EnumFacing face, EnumHand hand) {
        
        if (!world.isRemote && !operating && !ModConfig.CONFIG_MISC.wussMode) {
            TileEntity below = world.getTileEntity(pos.down());
            if (below != null) {
                IRiftJar jar = below.getCapability(CapabilityRiftJar.RIFT_JAR, null);
                if (jar != null && jar.hasRift()) {
                    Vec3d riftPos = findRiftPos();
                    if (riftPos != null) {
                        List<EntityFluxRift> rifts = world.getEntitiesWithinAABB(EntityFluxRift.class,
                                new AxisAlignedBB(riftPos.x, riftPos.y, riftPos.z, riftPos.x, riftPos.y, riftPos.z).grow(32.0F));
                        if (rifts.isEmpty()) {
                            rift = new EntityFluxRift(world);
                            EnumFacing facing = world.getBlockState(pos.down()).getValue(IHorizontallyDirectionalBlock.DIRECTION);
                            rift.setPositionAndRotation(riftPos.x, riftPos.y, riftPos.z, facing != null ? facing.getHorizontalAngle() : 0.0F, 0.0F);
                            if (world.spawnEntity(rift)) {
                                rift.setRiftSize(1);
                                rift.setRiftSeed(jar.getRift().getRiftSeed());
                                operating = true;
                                targetSize = jar.getRift().getRiftSize();
                                lastSize = 1;
                                markDirty();
                                world.playSound(null, pos, SoundsTC.craftstart, SoundCategory.BLOCKS, 0.5F, 1.0F);
                                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
                            }
                            else {
                                rift = null;
                                AuraHelper.polluteAura(world, pos, jar.getRift().getRiftSize(), true);
                                world.playSound(null, pos, SoundsTC.craftfail, SoundCategory.BLOCKS, 0.5F, 1.0F);
                            }
                            
                            jar.setRift(new FluxRiftReconstructor(0, 0));
                        }
                        else {
                            player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.rift_too_close").setStyle(
                                    new Style().setColor(TextFormatting.DARK_PURPLE)), true);
                        }
                    }
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
    public void onBlockBroken() {
        if (!world.isRemote && operating) {
            if (rift != null) {
                if (rift.getCollapse())
                    rift.setCollapse(false);
                
                rift.setRiftSize(0);
            }
            
            AuraHelper.polluteAura(world, pos, targetSize, true);
            operating = false;
            world.playSound(null, pos, SoundsTC.craftfail, SoundCategory.BLOCKS, 0.5F, 1.0F);
        }
    }
    
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
    public void onLoad() {
        if (operating && loadedRiftUUID != null) {
            EntityFluxRift check = findRift();
            if (check != null && check.getUniqueID().equals(loadedRiftUUID)) {
                rift = check;
                if (loop != null)
                    loop.stop();
                
                loop = ThaumicAugmentation.proxy.playSpecialSound(TASounds.RIFT_MOVER_OUTPUT_LOOP, SoundCategory.BLOCKS,
                        old -> operating && rift != null ? old : null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                        4.0F, 1.0F, true, 0);
            }
            
            loadedRiftUUID = null;
        }
    }
    
    protected int getParticleDelay(int size) {
        if (size < targetSize / 3)
            return 5;
        else if (size < targetSize / 1.5)
            return 10;
        else
            return 20;
    }
    
    @Override
    public void update() {
        if (operating) {
            if (!world.isRemote && ticks++ % 10 == 0) {
                TileEntity below = world.getTileEntity(pos.down());
                if (rift == null || rift.isDead || rift.getRiftSize() < 1 || rift.getCollapse() ||
                        rift.getRiftSize() < lastSize || below == null || !below.hasCapability(CapabilityRiftJar.RIFT_JAR, null)) {
                    
                    if (rift != null) {
                        if (rift.getCollapse())
                            rift.setCollapse(false);
                        
                        rift.setRiftSize(0);
                    }
                    
                    AuraHelper.polluteAura(world, pos, targetSize, true);
                    operating = false;
                    markDirty();
                    world.playSound(null, pos, SoundsTC.craftfail, SoundCategory.BLOCKS, 0.5F, 1.0F);
                    world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
                }
                else if (AuraHelper.drainVis(world, pos, 0.25F, false) >= 0.25F - 0.0001) {
                    rift.setRiftSize(rift.getRiftSize() + 1);
                    lastSize = rift.getRiftSize();
                    if (rift.getRiftSize() >= targetSize) {
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
                    Vec3d particleDest = RiftHelper.pickRandomPointOnRift(rift).add(rift.posX, rift.posY, rift.posZ);
                    Vec3d dir = particleDest.subtract(new Vec3d(pos).add(0.5, 0.5, 0.5)).normalize();
                    ThaumicAugmentation.proxy.getRenderHelper().renderRiftMoverParticle(world,
                            pos.getX() + 0.5, pos.getY() + 0.75, pos.getZ() + 0.5, dir.x * 0.25, dir.y * 0.25, dir.z * 0.25);
                    if (!TAConfig.reducedEffects.getValue()) {
                        particleDest = RiftHelper.pickRandomPointOnRift(rift).add(rift.posX, rift.posY, rift.posZ);
                        ThaumicAugmentation.proxy.getRenderHelper().renderSpark(world, particleDest.x, particleDest.y, particleDest.z, 5.0F + world.rand.nextFloat() * 7.5F,
                                Aspect.ELDRITCH.getColor(), false);
                        if (world.rand.nextBoolean()) {
                            particleDest = RiftHelper.pickRandomPointOnRiftWithInstability(rift, ticks).add(rift.posX, rift.posY, rift.posZ);
                            ThaumicAugmentation.proxy.getRenderHelper().renderArc(world, particleDest.x,
                                    particleDest.y, particleDest.z, pos.getX() + 0.5, pos.getY() + 0.75,
                                    pos.getZ() + 0.5, Aspect.ELDRITCH.getColor(), rift.height / 2);
                        }
                    }
                }
                
                ++ticks;
            }
        }
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    @Override
    public void invalidate() {
        super.invalidate();
        if (loop != null)
            loop.stop();
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
            if (check != null && check.getUniqueID().equals(pkt.getNbtCompound().getUniqueId("rift"))) {
                rift = check;
                if (loop != null)
                    loop.stop();
                
                loop = ThaumicAugmentation.proxy.playSpecialSound(TASounds.RIFT_MOVER_OUTPUT_LOOP, SoundCategory.BLOCKS,
                        old -> operating && rift != null ? old : null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                        4.0F, 1.0F, true, 0);
            }
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
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("operating", operating);
        if (operating) {
            compound.setInteger("size", targetSize);
            compound.setInteger("lastSize", lastSize);
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
            targetSize = compound.getInteger("size");
            // will be ok if not present as no size would be less than 0
            // so no rifts going boom on update
            lastSize = compound.getInteger("lastSize");
            loadedRiftUUID = compound.getUniqueId("rift");
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        AxisAlignedBB normal = super.getRenderBoundingBox();
        if (EntityUtils.hasGoggles(Minecraft.getMinecraft().player)) {
            Vec3d origin = findLocalRiftPos();
            if (origin != null) {
                TileEntity below = world.getTileEntity(pos.down());
                if (below != null) {
                    IRiftJar jar = below.getCapability(CapabilityRiftJar.RIFT_JAR, null);
                    if (jar != null && jar.hasRift()) {
                        return normal.union(jar.getRift().getBoundingBox().grow(0.5).offset(pos.getX(),
                                pos.getY() + origin.y, pos.getZ()));
                    }
                }
            }
        }
        
        return normal;
    }
    
}
