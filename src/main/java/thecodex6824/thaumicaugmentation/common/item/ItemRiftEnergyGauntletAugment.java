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
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.ICaster;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.augment.Augment;
import thecodex6824.thaumicaugmentation.api.energy.CapabilityRiftEnergyStorage;
import thecodex6824.thaumicaugmentation.api.energy.IRiftEnergyStorage;
import thecodex6824.thaumicaugmentation.api.energy.RiftEnergyHelper;
import thecodex6824.thaumicaugmentation.api.energy.RiftEnergyStorage;
import thecodex6824.thaumicaugmentation.api.entity.IDimensionalFracture;
import thecodex6824.thaumicaugmentation.api.util.AugmentUtils;
import thecodex6824.thaumicaugmentation.common.capability.CapabilityProviderAugmentRiftEnergyStorage;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class ItemRiftEnergyGauntletAugment extends ItemTABase {

    public ItemRiftEnergyGauntletAugment() {
        super();
        setMaxStackSize(1);
    }
    
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new CapabilityProviderAugmentRiftEnergyStorage(new Augment(stack) {
            
            private boolean syncNeeded;
            
            @Override
            public boolean canBeAppliedToItem(ItemStack augmentable) {
                return augmentable.getItem() instanceof ICaster;
            }
            
            @Override
            public boolean isCompatible(ItemStack otherAugment) {
                return stack.getItem() != otherAugment.getItem();
            }
            
            @Override
            public void onCast(ItemStack caster, FocusPackage focusPackage, Entity user) {
                if (stack.hasCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null)) {
                    IRiftEnergyStorage energy = stack.getCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null);
                    // not actually removing energy is intentional
                    if (energy.extractEnergy(10, true) == 10)
                        AugmentUtils.setPackagePower(focusPackage, focusPackage.getPower() * 1.1F);
                }
            }
            
            @Override
            public void onTick(Entity user) {
                if (!user.getEntityWorld().isRemote && user.getEntityWorld().getTotalWorldTime() % 20 == 0) {
                    IRiftEnergyStorage stackStorage = stack.getCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null);
                    if (stackStorage.canReceive() && stackStorage.getEnergyStored() < stackStorage.getMaxEnergyStored()) {
                        syncNeeded = RiftEnergyHelper.drainNearbyEnergyIntoStorage(user.getEntityWorld(), stackStorage, 
                                user.getEntityBoundingBox().grow(user.width / 2, user.height / 2, user.width / 2), 
                                user.getPositionVector().add(0, user.height / 2, 0));
                    }
                }
            }
            
            @Override
            public void onInteractEntity(Entity user, ItemStack used, Entity target, EnumHand hand) {
                if (!user.getEntityWorld().isRemote && target instanceof IDimensionalFracture) {
                    IDimensionalFracture fracture = (IDimensionalFracture) target;
                    if (!fracture.isOpening() && !fracture.isOpen()) {
                        IRiftEnergyStorage stackStorage = stack.getCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null);
                        if (stackStorage.canExtract() && stackStorage.extractEnergy(75, true) == 75) {
                            stackStorage.extractEnergy(75, false);
                            fracture.open();
                            TANetwork.INSTANCE.sendToAllAround(new PacketParticleEffect(ParticleEffect.VOID_STREAKS, 
                                    user.posX, user.posY + user.height / 2, user.posZ, target.posX, target.posY + target.height / 2, target.posZ, 0.32F), 
                                    new TargetPoint(user.getEntityWorld().provider.getDimension(), user.posX, user.posY, user.posZ, 64.0F));
                            syncNeeded = true;
                        }
                    }
                }
            }
            
            @Override
            public boolean shouldSync() {
                boolean sync = syncNeeded;
                syncNeeded = false;
                return sync;
            }
            
            @Override
            public boolean hasAdditionalAugmentTooltip() {
                return true;
            }
            
            @Override
            public void appendAdditionalAugmentTooltip(List<String> tooltip) {
                if (stack.hasCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null)) {
                    IRiftEnergyStorage energy = stack.getCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null);
                    tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.stored_energy", new TextComponentTranslation(
                            RiftEnergyHelper.getEnergyAmountDescriptor(energy))).getFormattedText());
                }
            }
            
        }, new RiftEnergyStorage(600, 10));
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == TAItems.CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
            ItemStack empty = new ItemStack(this);
            items.add(empty);
            
            ItemStack full = new ItemStack(this);
            IRiftEnergyStorage energy = full.getCapability(CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE, null);
            while (energy.getEnergyStored() < energy.getMaxEnergyStored())
                energy.receiveEnergy(energy.getMaxEnergyStored(), false);
            items.add(full);
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
