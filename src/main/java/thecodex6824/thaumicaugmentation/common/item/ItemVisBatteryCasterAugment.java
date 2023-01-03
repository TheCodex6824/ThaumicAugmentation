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

package thecodex6824.thaumicaugmentation.common.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aura.AuraHelper;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.augment.Augment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.common.capability.AugmentCasterVisBattery;
import thecodex6824.thaumicaugmentation.common.capability.provider.SimpleCapabilityProvider;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.util.ItemHelper;

import javax.annotation.Nullable;
import java.util.List;

public class ItemVisBatteryCasterAugment extends ItemTABase {

    protected static final float MAX_VIS = 40;
    
    public ItemVisBatteryCasterAugment() {
        super();
        setMaxStackSize(1);
    }
    
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        SimpleCapabilityProvider<IAugment> storage = new SimpleCapabilityProvider<>(new AugmentCasterVisBattery(),
                CapabilityAugment.AUGMENT);
        if (nbt != null && nbt.hasKey("Parent", NBT.TAG_COMPOUND))
            storage.deserializeNBT(nbt.getCompoundTag("Parent"));
        
        return storage;
    }
    
    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (!world.isRemote && world.getTotalWorldTime() % 10 == 0) {
            IAugment augment = stack.getCapability(CapabilityAugment.AUGMENT, null);
            if (augment instanceof AugmentCasterVisBattery) {
                AugmentCasterVisBattery battery = (AugmentCasterVisBattery) augment;
                if (battery.getVis() < battery.getMaxVis()) {
                    float inAura = AuraHelper.getVis(world, entity.getPosition());
                    if (inAura > AuraHelper.getAuraBase(world, entity.getPosition()) * 0.75F) {
                        float input = Math.min(battery.getMaxVis() - battery.getVis(), Math.min(inAura, 1.0F));
                        battery.setVis(battery.getVis() + AuraHelper.drainVis(world, entity.getPosition(), input, false));
                    }
                }
            }
        }
    }
    
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        NBTTagCompound tag = new NBTTagCompound();
        if (stack.hasTagCompound()) {
            NBTTagCompound item = stack.getTagCompound().copy();
            if (!ThaumicAugmentation.proxy.isSingleplayer() && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
                item.removeTag("cap");
            
            tag.setTag("item", item);
        }

        NBTTagCompound cap = ItemHelper.tryMakeCapabilityTag(stack, CapabilityAugment.AUGMENT);
        if (cap != null)
            tag.setTag("cap", cap);

        return tag;
    }
    
    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        if (nbt != null) {
            if (nbt.hasKey("cap", NBT.TAG_COMPOUND))
                ((Augment) stack.getCapability(CapabilityAugment.AUGMENT, null)).deserializeNBT(nbt.getCompoundTag("cap"));
            if (nbt.hasKey("item", NBT.TAG_COMPOUND))
                stack.setTagCompound(nbt.getCompoundTag("item"));
            else if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
                nbt.removeTag("cap");
                if (!nbt.isEmpty())
                    stack.setTagCompound(nbt);
            }
            
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && !ThaumicAugmentation.proxy.isSingleplayer()) {
                if (!stack.hasTagCompound())
                    stack.setTagCompound(new NBTTagCompound());
                
                stack.getTagCompound().setTag("cap", nbt.getCompoundTag("cap"));
            }
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        IAugment augment = stack.getCapability(CapabilityAugment.AUGMENT, null);
        if (augment instanceof AugmentCasterVisBattery) {
            tooltip.add(new TextComponentString(new TextComponentTranslation("tc.charge").getFormattedText() + " " +
                    Math.round(((AugmentCasterVisBattery) augment).getVis())).setStyle(
                    new Style().setColor(TextFormatting.YELLOW)).getFormattedText());
        }
    }
    
}
