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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.CasterAugmentBuilder;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.ICustomCasterAugment;
import thecodex6824.thaumicaugmentation.common.capability.AugmentCasterCustom;
import thecodex6824.thaumicaugmentation.common.capability.SimpleCapabilityProvider;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

public class ItemCustomCasterAugment extends ItemTABase {

    public ItemCustomCasterAugment() {
        super();
        setMaxStackSize(1);
        setHasSubtypes(true);
    }
    
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        AugmentCasterCustom augment = new AugmentCasterCustom();
        SimpleCapabilityProvider<IAugment> provider = new SimpleCapabilityProvider<>(augment, CapabilityAugment.AUGMENT);
        if (nbt != null) {
            NBTTagCompound data = nbt.getCompoundTag("Parent");
            // check for remapped augment
            if (data.isEmpty() || !data.hasKey("strength", NBT.TAG_COMPOUND) || !data.hasKey("effect", NBT.TAG_COMPOUND)) {
                if (data.isEmpty() || !data.hasKey("strength", NBT.TAG_COMPOUND)) {
                    augment.setStrengthProvider(CasterAugmentBuilder.createStackForStrengthProvider(new ResourceLocation(
                            ThaumicAugmentationAPI.MODID, "strength_elemental")));
                    if (stack.hasTagCompound())
                        augment.getStrengthProvider().getTagCompound().setString("aspect", stack.getTagCompound().getString("aspect"));
                }
                
                if (data.isEmpty() || !data.hasKey("effect", NBT.TAG_COMPOUND)) {
                    augment.setEffectProvider(CasterAugmentBuilder.createStackForEffectProvider(new ResourceLocation(
                            ThaumicAugmentationAPI.MODID, "effect_power")));
                }
            }
            if (!data.isEmpty())
                provider.deserializeNBT(nbt);
        }
        
        return provider;
    }
    
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        NBTTagCompound tag = new NBTTagCompound();
        if (stack.hasTagCompound())
            tag.setTag("item", stack.getTagCompound());
        
        tag.setTag("cap", stack.getCapability(CapabilityAugment.AUGMENT, null).serializeNBT());
        return tag;
    }
    
    @Override
    public void readNBTShareTag(ItemStack stack, NBTTagCompound nbt) {
        if (nbt != null) {
            if (nbt.hasKey("item", NBT.TAG_COMPOUND))
                stack.setTagCompound(nbt.getCompoundTag("item"));
            
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && !Minecraft.getMinecraft().isSingleplayer()) {
                if (!stack.hasTagCompound())
                    stack.setTagCompound(new NBTTagCompound());
                
                stack.getTagCompound().setTag("cap", nbt.getCompoundTag("cap"));
            }
            
            stack.getCapability(CapabilityAugment.AUGMENT, null).deserializeNBT(nbt.getCompoundTag("cap"));
        }
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {}
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.getCapability(CapabilityAugment.AUGMENT, null) instanceof ICustomCasterAugment) {
            ICustomCasterAugment aug = (ICustomCasterAugment) stack.getCapability(CapabilityAugment.AUGMENT, null);
            tooltip.add( new TextComponentTranslation(aug.getStrengthProvider().getTranslationKey()).getFormattedText());
            CasterAugmentBuilder.getStrengthProvider(ItemCustomCasterStrengthProvider.getProviderID(
                    aug.getStrengthProvider())).appendAdditionalTooltip(aug.getStrengthProvider(), tooltip);
            tooltip.add(new TextComponentTranslation(aug.getEffectProvider().getTranslationKey()).getFormattedText());
            CasterAugmentBuilder.getEffectProvider(ItemCustomCasterEffectProvider.getProviderID(
                    aug.getEffectProvider())).appendAdditionalTooltip(aug.getEffectProvider(), tooltip);
        }
    }
    
    @Override
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(
                "ta_special:custom_caster_augment", "inventory"));
    }
    
}
