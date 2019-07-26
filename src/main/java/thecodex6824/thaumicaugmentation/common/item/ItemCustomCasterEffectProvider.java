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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.builder.caster.CasterAugmentBuilder;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

public class ItemCustomCasterEffectProvider extends ItemTABase {
    
    public ItemCustomCasterEffectProvider() {
        super();
        setHasSubtypes(true);
    }
    
    @Override
    public String getTranslationKey(ItemStack stack) {
        return "item." + getID(stack).replace(':', '.') + ".name";
    }
    
    @Override
    public String getUnlocalizedNameInefficiently(ItemStack stack) {
        return "item." + getID(stack).replace(':', '.');
    }
    
    @Override
    public String getCreatorModId(ItemStack stack) {
        return getIDNamespace(stack);
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == TAItems.CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
            for (ResourceLocation loc : CasterAugmentBuilder.getAllEffectProviders())
                items.add(CasterAugmentBuilder.createStackForEffectProvider(loc));
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flagIn) {
        CasterAugmentBuilder.getEffectProvider(getProviderID(stack)).appendAdditionalTooltip(stack, tooltip);
    }
    
    protected static String getID(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getString("id") : "";
    }
    
    protected static String getIDNamespace(ItemStack stack) {
        String id = getID(stack);
        int index = id.indexOf(':');
        return index != -1 ? id.substring(0, index) : id;
    }
    
    protected static String getIDPath(ItemStack stack) {
        String id = getID(stack);
        int index = id.indexOf(':');
        return index != -1 ? id.substring(index + 1) : id;
    }
    
    public static ResourceLocation getProviderID(ItemStack stack) {
        String id = getID(stack);
        return id.isEmpty() ? new ResourceLocation(ThaumicAugmentationAPI.MODID, "null") : new ResourceLocation(id);
    }
    
    public static ItemStack create(ResourceLocation id) {
        ItemStack stack = new ItemStack(TAItems.AUGMENT_BUILDER_EFFECT);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", id.toString());
        stack.setTagCompound(tag);
        return stack;
    }
    
}
