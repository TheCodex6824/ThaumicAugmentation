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

package thecodex6824.thaumicaugmentation.common.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.ICaster;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.energy.CapabilityRiftEnergyStorage;
import thecodex6824.thaumicaugmentation.api.energy.IRiftEnergyStorage;
import thecodex6824.thaumicaugmentation.api.energy.RiftEnergyHelper;
import thecodex6824.thaumicaugmentation.api.entity.IDimensionalFracture;
import thecodex6824.thaumicaugmentation.api.util.AugmentUtils;
import thecodex6824.thaumicaugmentation.common.capability.SimpleCapabilityProvider;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class ItemRiftEnergyGauntletAugment extends ItemTABase implements IAugment {

    public ItemRiftEnergyGauntletAugment() {
        super();
        setMaxStackSize(1);
    }
    
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new SimpleCapabilityProvider<>(CapabilityRiftEnergyStorage.create(600, 10), 
                CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE);
    }
    
    @Override
    public boolean canBeAppliedToItem(ItemStack stack, ItemStack augmentable) {
        return augmentable.getItem() instanceof ICaster;
    }
    
    @Override
    public boolean isCompatible(ItemStack stack, ItemStack otherAugment) {
        return stack.getItem() != otherAugment.getItem();
    }
    
    @Override
    public void onCast(ItemStack stack, ItemStack caster, FocusPackage focusPackage, Entity user) {
        if (stack.hasCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null)) {
            IRiftEnergyStorage energy = stack.getCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null);
            if (energy.extractEnergy(10, true) == 10) {
                energy.extractEnergy(10, false);
                AugmentUtils.setPackagePower(focusPackage, focusPackage.getPower() * 1.1F);
            }
        }
    }
    
    @Override
    public void onTick(ItemStack stack, Entity user) {
        if (user.getEntityWorld().getTotalWorldTime() % 20 == 0) {
            IRiftEnergyStorage stackStorage = stack.getCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null);
            if (stackStorage.canReceive() && stackStorage.getEnergyStored() < stackStorage.getMaxEnergyStored()) {
                for (Entity entity : user.getEntityWorld().getEntitiesWithinAABBExcludingEntity(user, user.getEntityBoundingBox().grow(1.0))) {
                    if (entity.hasCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null)) {
                        IRiftEnergyStorage entityStorage = entity.getCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null);
                        long maxToExtract = stackStorage.getMaxEnergyStored() - stackStorage.getEnergyStored();
                        long extracted = entityStorage.extractEnergy(maxToExtract, false);
                        stackStorage.receiveEnergy(extracted, false);
                        TANetwork.INSTANCE.sendToAllAround(new PacketParticleEffect(ParticleEffect.VOID_STREAKS, 
                                entity.posX, entity.posY, entity.posZ, user.posX, user.posY + user.height / 2, user.posZ, 0.04F), 
                                new TargetPoint(user.getEntityWorld().provider.getDimension(), user.posX, user.posY, user.posZ, 64.0F));
                    }
                }
            }
        }
    }
    
    @Override
    public void onInteractEntity(ItemStack stack, Entity user, ItemStack used, Entity target, EnumHand hand) {
        if (target instanceof IDimensionalFracture) {
            IDimensionalFracture fracture = (IDimensionalFracture) target;
            if (!fracture.isOpening() && !fracture.isOpen()) {
                IRiftEnergyStorage stackStorage = stack.getCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null);
                if (stackStorage.canExtract() && stackStorage.extractEnergy(75, true) == 75) {
                    stackStorage.extractEnergy(75, false);
                    fracture.open();
                    for (int i = 0; i < 7; ++i) {
                        TANetwork.INSTANCE.sendToAllAround(new PacketParticleEffect(ParticleEffect.VOID_STREAKS, 
                                user.posX, user.posY + user.height / 2, user.posZ, target.posX, target.posY + target.height / 2, target.posZ, 0.04F), 
                                new TargetPoint(user.getEntityWorld().provider.getDimension(), user.posX, user.posY, user.posZ, 64.0F));
                    }
                }
            }
        }
    }
    
    @Override
    public boolean shouldSync(ItemStack stack) {
        return true;
    }
    
    @Override
    public int getSyncInterval(ItemStack stack) {
        return 10;
    }
    
    @Override
    public boolean hasAdditionalAugmentTooltip(ItemStack stack) {
        return true;
    }
    
    @Override
    public void appendAdditionalAugmentTooltip(ItemStack stack, List<String> tooltip) {
        if (stack.hasCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null)) {
            IRiftEnergyStorage energy = stack.getCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null);
            tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.stored_energy", new TextComponentTranslation(
                    RiftEnergyHelper.getEnergyAmountDescriptor(energy))).getFormattedText());
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        if (stack.hasCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null)) {
            IRiftEnergyStorage energy = stack.getCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null);
            tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.stored_energy", new TextComponentTranslation(
                    RiftEnergyHelper.getEnergyAmountDescriptor(energy))).getFormattedText());
        }
    }
    
}
