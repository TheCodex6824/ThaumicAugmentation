/**
 *	Thaumic Augmentation
 *	Copyright (c) 2019 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.common.item.prefab;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.item.trait.IModelProvider;

public class ItemTABase extends Item implements IModelProvider {
	
	public static void sharedInit(Item item, String name, String... subItems) {
		item.setRegistryName(name);
		item.setTranslationKey(ThaumicAugmentationAPI.MODID + "." + name);
		item.setHasSubtypes(subItems.length > 0);
		item.setCreativeTab(TAItems.CREATIVE_TAB);	
	}
	
	protected String[] subItemNames;
	
	public ItemTABase() {}
	
	public ItemTABase(String name, String... subItems) {
		sharedInit(this, name, subItems);
		subItemNames = subItems;
	}
	
	@Override
	public String getTranslationKey(ItemStack stack) {
		if (subItemNames.length > 0 && stack.getMetadata() < subItemNames.length)
			return super.getTranslationKey() + "." + subItemNames[stack.getMetadata()];
		else
			return super.getTranslationKey();
	}
	
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (tab == TAItems.CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
			if (subItemNames.length > 0) {
				for (int i = 0; i < subItemNames.length; ++i)
					items.add(new ItemStack(this, 1, i));
			}
			else
				super.getSubItems(tab, items);
		}
	}
	
	@Override
	public int getTotalSubtypes() {
		return subItemNames.length > 0 ? subItemNames.length : 1;
	}
	
	@Override
	public boolean isDamageable() {
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public ModelResourceLocation getModelResourceLocation(int metadata) {
		if (subItemNames.length > 0 && metadata < subItemNames.length)
			return new ModelResourceLocation(getRegistryName() + "_" + subItemNames[metadata], "inventory");
		else
			return new ModelResourceLocation(getRegistryName().toString(), "inventory");
	}
	
}
