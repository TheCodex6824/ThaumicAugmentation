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

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.items.ItemsTC;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.recipe.VoidseerGauntletInfusionRecipe;

public class RecipeHandler {

	public static void init() {
		ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "GauntletVoid"), 
				new VoidseerGauntletInfusionRecipe());
		ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "SealCopier"), 
				new InfusionRecipe("SEAL_COPIER", new ItemStack(TAItems.SEAL_COPIER), 1, 
				new AspectList().add(Aspect.MIND, 25).add(Aspect.MECHANISM, 10), 
				new ItemStack(ItemsTC.golemBell), new Object[] { 
						new ItemStack(ItemsTC.brain), new ItemStack(ItemsTC.brain), new ItemStack(ItemsTC.seals, 1, 0)
				}
		));
		
		ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "GauntletThaumium"), new ShapedArcaneRecipe(
				new ResourceLocation(ThaumicAugmentationAPI.MODID, "GAUNTLET_THAUMIUM"), "GAUNTLET_THAUMIUM", 250, 
				new AspectList().add(Aspect.AIR, 2).add(Aspect.EARTH, 2).add(Aspect.ENTROPY, 2).add(Aspect.FIRE, 2).add(
				Aspect.ORDER, 2).add(Aspect.WATER, 2), new ItemStack(TAItems.GAUNTLET, 1, 0), new Object[] {
						"PPP",
						"FRF",
						"FTF",
						'P', new ItemStack(ItemsTC.plate, 1, 2), 'F', new ItemStack(ItemsTC.fabric), 'R', new ItemStack(ItemsTC.visResonator),
						'T', new ItemStack(ItemsTC.thaumometer)
				}
		));
		ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "Lattice"), new ShapedArcaneRecipe(
				new ResourceLocation(ThaumicAugmentationAPI.MODID, "LATTICE"), "VIS_REGENERATOR", 25, 
				new AspectList().add(Aspect.AIR, 1).add(Aspect.WATER, 1), 
				new ItemStack(TAItems.MATERIAL, 1, 0), new Object[] {
						"SLS",
						"LFL",
						"SLS",
						'S', new ItemStack(BlocksTC.plankSilverwood), 'L', new ItemStack(BlocksTC.leafSilverwood), 'F', new ItemStack(ItemsTC.filter)
				}
		));
		ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "VisRegenerator"), new ShapedArcaneRecipe(
				new ResourceLocation(ThaumicAugmentationAPI.MODID, "VIS_REGENERATOR"), "VIS_REGENERATOR", 100, 
				new AspectList().add(Aspect.AIR, 2), 
				new ItemStack(TABlocks.VIS_REGENERATOR, 1, 0), new Object[] {
						"GIG",
						"BLB",
						"GEG",
						'G', new ItemStack(BlocksTC.plankGreatwood), 'I', new ItemStack(Blocks.IRON_BARS), 'B', new ItemStack(ItemsTC.plate, 1, 0),
						'L', new ItemStack(TAItems.MATERIAL, 1, 0), 'E', new ItemStack(ItemsTC.mechanismSimple)
				}
		));
		ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "WardingSigil"), new ShapedArcaneRecipe(
				new ResourceLocation(ThaumicAugmentationAPI.MODID, "WardingSigil"), "WARDED_ARCANA@2", 10, 
				new AspectList().add(Aspect.ORDER, 1), 
				new ItemStack(TAItems.MATERIAL, 1, 1), new Object[] {
						" T ",
						"PBP",
						" T ",
						'T', new ItemStack(ItemsTC.tallow), 'P', "dyePurple", 'B', new ItemStack(ItemsTC.brain)
				}
		));
		ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "WardedChest"), new ShapedArcaneRecipe(
				new ResourceLocation(ThaumicAugmentationAPI.MODID, "WardedChest"), "WARDED_ARCANA", 75, 
				new AspectList().add(Aspect.ORDER, 2), 
				new ItemStack(TABlocks.WARDED_CHEST), new Object[] {
						" S ",
						"TCT",
						"TTT",
						'T', new ItemStack(ItemsTC.plate, 1, 2), 'S', new ItemStack(TAItems.MATERIAL, 1, 1), 'C', "chestWood"
				}
		));
		ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneDoorWood"), new ShapedArcaneRecipe(
				new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneDoorWood"), "ARCANE_DOOR", 100, 
				new AspectList().add(Aspect.ORDER, 2), 
				new ItemStack(TAItems.ARCANE_DOOR, 1, 0), new Object[] {
						"GBG",
						"GSG",
						"GBG",
						'G', new ItemStack(BlocksTC.plankGreatwood), 'B', new ItemStack(ItemsTC.plate, 1, 0), 'S', new ItemStack(TAItems.MATERIAL, 1, 1)
				}
		));
		ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneDoorMetal"), new ShapedArcaneRecipe(
				new ResourceLocation(ThaumicAugmentationAPI.MODID, "ArcaneDoorMetal"), "ARCANE_DOOR", 100, 
				new AspectList().add(Aspect.ORDER, 2), 
				new ItemStack(TAItems.ARCANE_DOOR, 1, 1), new Object[] {
						"TIT",
						"TST",
						"TIT",
						'T', new ItemStack(ItemsTC.plate, 1, 2), 'I', new ItemStack(ItemsTC.plate, 1, 1), 'S', new ItemStack(TAItems.MATERIAL, 1, 1)
				}
		));
	}
	
}
