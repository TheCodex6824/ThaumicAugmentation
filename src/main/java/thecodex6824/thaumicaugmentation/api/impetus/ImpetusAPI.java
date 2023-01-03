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

package thecodex6824.thaumicaugmentation.api.impetus;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import thecodex6824.thaumicaugmentation.api.entity.DamageSourceImpetus;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

import javax.annotation.Nullable;

public final class ImpetusAPI {

    private ImpetusAPI() {}
    
    public static final String ENERGY_NONE = "thaumicaugmentation.text.energy_none";
    public static final String ENERGY_MINIMAL = "thaumicaugmentation.text.energy_minimal";
    public static final String ENERGY_VERY_WEAK = "thaumicaugmentation.text.energy_very_weak";
    public static final String ENERGY_WEAK = "thaumicaugmentation.text.energy_weak";
    public static final String ENERGY_MEDIUM = "thaumicaugmentation.text.energy_medium";
    public static final String ENERGY_STRONG = "thaumicaugmentation.text.energy_strong";
    public static final String ENERGY_MAX = "thaumicaugmentation.text.energy_max";
    
    /*
     * Returns an unlocalized description string for the proportion of energy in the object.
     * @param storage The object to get the descriptor of
     * @return An unlocalized String describing the ratio of stored energy to max energy
     */
    public static String getEnergyAmountDescriptor(IImpetusStorage storage) {
        if (storage.getEnergyStored() <= 0)
            return ENERGY_NONE;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.1)
            return ENERGY_MINIMAL;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.25)
            return ENERGY_VERY_WEAK;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.5)
            return ENERGY_WEAK;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.75)
            return ENERGY_MEDIUM;
        else if (storage.getEnergyStored() < storage.getMaxEnergyStored())
            return ENERGY_STRONG;
        else
            return ENERGY_MAX;
    }
    
    public static TextFormatting getSuggestedChatColorForDescriptor(IImpetusStorage storage) {
        if (storage.getEnergyStored() <= 0)
            return TextFormatting.DARK_RED;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.1)
            return TextFormatting.RED;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.25)
            return TextFormatting.GOLD;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.5)
            return TextFormatting.YELLOW;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.75)
            return TextFormatting.GREEN;
        else if (storage.getEnergyStored() < storage.getMaxEnergyStored())
            return TextFormatting.GREEN;
        else
            return TextFormatting.DARK_GREEN;
    }
    
    public static int getSuggestedColorForDescriptor(IImpetusStorage storage) {
        if (storage.getEnergyStored() <= 0)
            return 0xAA0000;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.1)
            return 0xFF5555;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.25)
            return 0xFFAA00;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.5)
            return 0xFFFF55;
        else if (storage.getEnergyStored() / (double) storage.getMaxEnergyStored() <= 0.75)
            return 0x55FF55;
        else if (storage.getEnergyStored() < storage.getMaxEnergyStored())
            return 0x55FF55;
        else
            return 0x00AA00;
    }
    
    public static void createImpetusParticles(World world, Vec3d origin, Vec3d dest) {
        TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.VOID_STREAKS, 
                origin.x, origin.y, origin.z, dest.x, dest.y, dest.z, 0.04F), 
                new TargetPoint(world.provider.getDimension(), dest.x, dest.y, dest.z, 64.0F));
    }
    
    public static boolean drainEnergyIntoStorage(World world, IImpetusStorage src, IImpetusStorage dest) {
        return drainEnergyIntoStorage(world, src, dest, null, null);
    }
    
    public static boolean drainEnergyIntoStorage(World world, IImpetusStorage src, IImpetusStorage dest, @Nullable Vec3d effectOrigin, @Nullable Vec3d effectDest) {
        long maxToExtract = dest.getMaxEnergyStored() - dest.getEnergyStored();
        long extracted = src.extractEnergy(maxToExtract, false);
        if (extracted > 0) {
            dest.receiveEnergy(extracted, false);
            if (effectOrigin != null && effectDest != null) {
                TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.VOID_STREAKS, 
                        effectOrigin.x, effectOrigin.y, effectOrigin.z, effectDest.x, effectDest.y, effectDest.z, 0.04F), 
                        new TargetPoint(world.provider.getDimension(), effectDest.x, effectDest.y, effectDest.z, 64.0F));
            }
            return true;
        }
        
        return false;
    }
    
    /*
     * Searches for IRiftEnergyStorage instances in the world, and drains energy from them into the passed storage.
     * @param world The world to look in
     * @param dest The IRiftEnergyStorage to put the energy in
     * @param range The bounding box to check for energy storage in
     * @return If any energy was received at all
     */
    public static boolean drainNearbyEnergyIntoStorage(World world, IImpetusStorage dest, AxisAlignedBB range) {
        return drainNearbyEnergyIntoStorage(world, dest, range, null);
    }
    
    /*
     * Searches for IRiftEnergyStorage instances in the world, and drains energy from them into the passed storage. This will
     * also create energy transfer particles on the client, if effectOrigin is not null.
     * @param world The world to look in
     * @param dest The IRiftEnergyStorage to put the energy in
     * @param range The bounding box to check for energy storage in
     * @param effectDest The position that the energy particles should go to, or null to not send particles
     * @return If any energy was received at all
     */
    public static boolean drainNearbyEnergyIntoStorage(World world, IImpetusStorage dest, AxisAlignedBB range, @Nullable Vec3d effectDest) {
        boolean receivedEnergy = false;
        MutableBlockPos pos = new MutableBlockPos(0, 0, 0);
        for (int x = (int) Math.floor(range.minX); x < Math.ceil(range.maxX); ++x) {
            for (int z = (int) Math.floor(range.minZ); z < Math.ceil(range.maxZ); ++z) {
                pos.setPos(x, 0, z);
                for (int y = (int) Math.floor(range.minY); y < Math.ceil(range.maxY); ++y) {  
                    if (world.isBlockLoaded(pos) && world.getChunk(pos).getTileEntity(pos, EnumCreateEntityType.CHECK) != null) {
                        TileEntity tile = world.getTileEntity(pos);
                        if (tile != null) {
                            IImpetusStorage storage = tile.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                            if (storage != null) {
                                long maxToExtract = dest.getMaxEnergyStored() - dest.getEnergyStored();
                                long extracted = storage.extractEnergy(maxToExtract, false);
                                if (extracted > 0) {
                                    dest.receiveEnergy(extracted, false);
                                    if (effectDest != null) {
                                        TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.VOID_STREAKS, 
                                                x + 0.5, y + 0.5, z + 0.5, effectDest.x, effectDest.y, effectDest.z, 0.04F), 
                                                new TargetPoint(world.provider.getDimension(), effectDest.x, effectDest.y, effectDest.z, 64.0F));
                                    }
                                    receivedEnergy = true;
                                }
                            }
                        }
                    }
                    
                    if (dest.getEnergyStored() == dest.getMaxEnergyStored())
                        return receivedEnergy;
                }
            }
        }
        
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, range)) {
            IImpetusStorage storage = entity.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
            if (storage != null) {
                long maxToExtract = dest.getMaxEnergyStored() - dest.getEnergyStored();
                long extracted = storage.extractEnergy(maxToExtract, false);
                if (extracted > 0) {
                    dest.receiveEnergy(extracted, false);
                    if (effectDest != null) {
                        TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.VOID_STREAKS, 
                                entity.posX, entity.posY, entity.posZ, effectDest.x, effectDest.y, effectDest.z, 0.04F), 
                                new TargetPoint(world.provider.getDimension(), effectDest.x, effectDest.y, effectDest.z, 64.0F));
                    }
                    receivedEnergy = true;
                }
            }
            
            if (dest.getEnergyStored() == dest.getMaxEnergyStored())
                return receivedEnergy;
        }
        
        return receivedEnergy;
    }
    
    public static boolean tryExtractFully(IImpetusStorage storage, long amount) {
        if (!storage.canExtract())
            return false;
        
        long total = amount;
        long oldAmount = amount;
        do {
            oldAmount = amount;
            amount -= storage.extractEnergy(amount, true);
        } while (amount > 0 && oldAmount != amount);
        
        if (amount <= 0) {
            oldAmount = total;
            amount = total;
            do {
                oldAmount = amount;
                amount -= storage.extractEnergy(amount, false);
            } while (amount > 0 && oldAmount != amount);
            
            return amount <= 0;
        }
        else
            return false;
    }
    
    public static boolean tryExtractFully(IImpetusStorage storage, long amount, Entity user) {
        if (user instanceof EntityPlayer && ((EntityPlayer) user).isCreative())
            return true;
        else
            return tryExtractFully(storage, amount);
    }
    
    private static boolean doDamage(DamageSource source1, DamageSource source2, Entity target, float damage) {
        boolean result = target.attackEntityFrom(source1, damage / 2.0F);
        if (result) {
            if (target instanceof EntityLivingBase) {
                EntityLivingBase base = (EntityLivingBase) target;
                base.hurtResistantTime = 0;
                base.lastDamage = 0;
            }
            
            target.attackEntityFrom(source2, damage / 2.0F);
        }
        
        return result;
    }
    
    public static boolean causeImpetusDamage(Entity target, float totalDamage) {
        DamageSource magic = new DamageSourceImpetus(null).setDamageBypassesArmor().setMagicDamage();
        DamageSource normal = new DamageSourceImpetus(null);
        return doDamage(magic, normal, target, totalDamage);
    }
    
    public static boolean causeImpetusDamage(Vec3d source, Entity target, float totalDamage) {
        DamageSource magic = new DamageSourceImpetus(source).setDamageBypassesArmor().setMagicDamage();
        DamageSource normal = new DamageSourceImpetus(source);
        return doDamage(magic, normal, target, totalDamage);
    }
    
    public static boolean causeImpetusDamage(Entity source, Entity target, float totalDamage) {
        DamageSource magic = new DamageSourceImpetus(source, source.getPositionVector()).setDamageBypassesArmor().setMagicDamage();
        DamageSource normal = new DamageSourceImpetus(source, source.getPositionVector());
        return doDamage(magic, normal, target, totalDamage);
    }
    
}
