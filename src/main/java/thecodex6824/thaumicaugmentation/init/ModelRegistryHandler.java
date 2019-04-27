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

package thecodex6824.thaumicaugmentation.init;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.ILightSourceBlock;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.item.trait.IModelProvider;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID, value = Side.CLIENT)
public class ModelRegistryHandler {

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		
		ModelLoader.setCustomStateMapper(TABlocks.TEMPORARY_LIGHT, new StateMap.Builder().ignore(ILightSourceBlock.LIGHT_LEVEL).build());
		
		for (Block b : TABlocks.getAllBlocks()) {
			if (b instanceof BlockTABase)
				ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(b), 0, ((BlockTABase) b).getModelResourceLocation(0));
			else
				ThaumicAugmentation.getLogger().warn("A block model ({}) was not registered! This is probably a bad thing!", b.getRegistryName());
		}
		
		for (Item item : TAItems.getAllItems()) {
			if (item instanceof IModelProvider) {
				for (int i = 0; i < ((IModelProvider) item).getTotalSubtypes(); ++i)
					ModelLoader.setCustomModelResourceLocation(item, i, ((IModelProvider) item).getModelResourceLocation(i));
			}
			else
				ThaumicAugmentation.getLogger().warn("An item model ({}) was not registered! This is probably a bad thing!", item.getRegistryName());
		}
	}
	
}
