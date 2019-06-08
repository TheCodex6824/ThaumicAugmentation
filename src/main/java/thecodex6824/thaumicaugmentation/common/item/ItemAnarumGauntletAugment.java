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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.ICaster;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.energy.CapabilityAnarumStorage;
import thecodex6824.thaumicaugmentation.api.energy.IAnarumStorage;
import thecodex6824.thaumicaugmentation.api.util.AugmentUtils;
import thecodex6824.thaumicaugmentation.common.capability.SimpleCapabilityProvider;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

public class ItemAnarumGauntletAugment extends ItemTABase implements IAugment {

    public ItemAnarumGauntletAugment() {
        super();
        setMaxStackSize(1);
    }
    
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new SimpleCapabilityProvider<>(CapabilityAnarumStorage.create(1000, 25), 
                CapabilityAnarumStorage.ANARUM_STORAGE);
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
    public void onUserCast(ItemStack stack, ItemStack caster, FocusPackage focusPackage, Entity user) {
        if (stack.hasCapability(CapabilityAnarumStorage.ANARUM_STORAGE, null)) {
            IAnarumStorage energy = stack.getCapability(CapabilityAnarumStorage.ANARUM_STORAGE, null);
            if (energy.extractEnergy(10, true) == 10) {
                energy.extractEnergy(10, false);
                AugmentUtils.setPackagePower(focusPackage, focusPackage.getPower() * 1.1F);
            }
        }
    }
    
    @Override
    public boolean hasAdditionalAugmentTooltip(ItemStack stack) {
        return true;
    }
    
    @Override
    public void appendAdditionalAugmentTooltip(ItemStack stack, List<String> tooltip) {
        if (stack.hasCapability(CapabilityAnarumStorage.ANARUM_STORAGE, null)) {
            IAnarumStorage energy = stack.getCapability(CapabilityAnarumStorage.ANARUM_STORAGE, null);
            tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.stored_energy", energy.getEnergyStored(), energy.getMaxEnergyStored()).getFormattedText());
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        if (stack.hasCapability(CapabilityAnarumStorage.ANARUM_STORAGE, null)) {
            IAnarumStorage energy = stack.getCapability(CapabilityAnarumStorage.ANARUM_STORAGE, null);
            tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.stored_energy", energy.getEnergyStored(), energy.getMaxEnergyStored()).getFormattedText());
        }
    }
    
}
