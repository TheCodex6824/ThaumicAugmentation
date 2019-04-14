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
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ScanBlock;
import thaumcraft.api.research.ScanningManager;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;

public class ResearchHandler {
	
	public static void init() {
		ResearchCategories.registerCategory("THAUMIC_AUGMENTATION", "FIRSTSTEPS", new AspectList(),
				new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/items/seal_copier.png"),
				new ResourceLocation("thaumcraft", "textures/gui/gui_research_back_1.jpg"), new ResourceLocation("thaumcraft", "textures/gui/gui_research_back_over.png")
		);
		ThaumcraftApi.registerResearchLocation(new ResourceLocation(ThaumicAugmentationAPI.MODID, "research/misc.json"));
		ThaumcraftApi.registerResearchLocation(new ResourceLocation(ThaumicAugmentationAPI.MODID, "research/foci.json"));
		ThaumcraftApi.registerResearchLocation(new ResourceLocation(ThaumicAugmentationAPI.MODID, "research/gauntlets.json"));
		ThaumcraftApi.registerResearchLocation(new ResourceLocation(ThaumicAugmentationAPI.MODID, "research/warded.json"));
		
		ScanningManager.addScannableThing(new ScanBlock("f_LEAFSILVERWOOD", new Block[] {BlocksTC.leafSilverwood}));
	}
	
}
