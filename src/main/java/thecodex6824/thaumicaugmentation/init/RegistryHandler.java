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
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.block.BlockArcaneDoor;
import thecodex6824.thaumicaugmentation.common.block.BlockTemporaryLight;
import thecodex6824.thaumicaugmentation.common.block.BlockVisRegenerator;
import thecodex6824.thaumicaugmentation.common.block.BlockWardedChest;
import thecodex6824.thaumicaugmentation.common.block.trait.INoAutomaticItemBlockRegistration;
import thecodex6824.thaumicaugmentation.common.item.ItemArcaneDoor;
import thecodex6824.thaumicaugmentation.common.item.ItemKey;
import thecodex6824.thaumicaugmentation.common.item.ItemSealCopier;
import thecodex6824.thaumicaugmentation.common.item.ItemTieredCasterGauntlet;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.recipe.AuthorizedKeyCreationRecipe;
import thecodex6824.thaumicaugmentation.common.recipe.DyeableItemRecipe;
import thecodex6824.thaumicaugmentation.common.tile.TileArcaneDoor;
import thecodex6824.thaumicaugmentation.common.tile.TileTemporaryLight;
import thecodex6824.thaumicaugmentation.common.tile.TileVisRegenerator;
import thecodex6824.thaumicaugmentation.common.tile.TileWardedChest;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public class RegistryHandler {
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();
		registry.register(new BlockVisRegenerator("vis_regenerator"));
		registry.register(new BlockWardedChest("warded_chest"));
		registry.register(new BlockArcaneDoor("arcane_door"));
		registry.register(new BlockTemporaryLight("temporary_light"));
		
		GameRegistry.registerTileEntity(TileVisRegenerator.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "vis_regenerator"));
		GameRegistry.registerTileEntity(TileWardedChest.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "warded_chest"));
		GameRegistry.registerTileEntity(TileArcaneDoor.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "arcane_door"));
		GameRegistry.registerTileEntity(TileTemporaryLight.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "temporary_light"));
	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		for (Block b : TABlocks.getAllBlocks()) {
			if (!(b instanceof INoAutomaticItemBlockRegistration))
				registry.register(new ItemBlock(b).setRegistryName(b.getRegistryName()));
		}
		
		registry.register(new ItemTieredCasterGauntlet("gauntlet"));
		registry.register(new ItemTABase("material", "lattice", "warding_sigil"));
		registry.register(new ItemSealCopier("seal_copier"));
		registry.register(new ItemArcaneDoor("arcane_door"));
		registry.register(new ItemKey("key"));
	}
	
	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		event.getRegistry().register(new DyeableItemRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "dyeable_item")));
		event.getRegistry().register(new AuthorizedKeyCreationRecipe().setRegistryName(new ResourceLocation(ThaumicAugmentationAPI.MODID, "bound_key")));
	}
	
}
