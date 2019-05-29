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

package thecodex6824.thaumicaugmentation.init;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectEventProxy;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.casters.FocusEngine;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusEffectLight;

public class MiscHandler {

    public static void init() {
        FocusEngine.registerElement(FocusEffectLight.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/foci/light.png"), 
                Aspect.LIGHT.getColor());
    }
    
    public static void postInit() {
    	AspectEventProxy proxy = new AspectEventProxy();
    	proxy.registerComplexObjectTag(new ItemStack(TAItems.ARCANE_DOOR, 1, 0), new AspectList().add(Aspect.PROTECT, 23));
    	proxy.registerComplexObjectTag(new ItemStack(TAItems.ARCANE_DOOR, 1, 1), new AspectList().add(Aspect.PROTECT, 19));
    	proxy.registerComplexObjectTag(new ItemStack(TAItems.GAUNTLET, 1, 0), new AspectList().add(Aspect.MAGIC, 8));
    	proxy.registerComplexObjectTag(new ItemStack(TAItems.GAUNTLET, 1, 1), new AspectList().add(Aspect.ELDRITCH, 27).add(Aspect.VOID, 23));
    	proxy.registerObjectTag(new ItemStack(TAItems.KEY, 1, 0), new AspectList().add(Aspect.PROTECT, 5).add(Aspect.MIND, 10).add(Aspect.METAL, 3));
    	proxy.registerObjectTag(new ItemStack(TAItems.KEY, 1, 1), new AspectList().add(Aspect.PROTECT, 5).add(Aspect.MIND, 10).add(Aspect.METAL, 3));
    	proxy.registerObjectTag(new ItemStack(TAItems.KEY, 1, 2), new AspectList().add(Aspect.PROTECT, 5).add(Aspect.MIND, 10).add(Aspect.METAL, 3));
    	proxy.registerComplexObjectTag(new ItemStack(TAItems.MATERIAL, 1, 0), new AspectList().add(Aspect.AURA, 7).add(Aspect.PLANT, 6));
    	proxy.registerObjectTag(new ItemStack(TAItems.MATERIAL, 1, 1), new AspectList().add(Aspect.PROTECT, 15).add(Aspect.MIND, 10));
    	proxy.registerObjectTag(new ItemStack(TAItems.RIFT_SEED), new AspectList());
    	proxy.registerObjectTag(new ItemStack(TAItems.SEAL_COPIER), new AspectList().add(Aspect.MIND, 15).add(Aspect.TOOL, 5));
    	proxy.registerComplexObjectTag(new ItemStack(TAItems.VOID_BOOTS), new AspectList().add(Aspect.ELDRITCH, 43).add(Aspect.VOID, 23));
    	
    	proxy.registerComplexObjectTag(new ItemStack(TABlocks.ARCANE_TRAPDOOR_METAL), new AspectList().add(Aspect.PROTECT, 7));
    	proxy.registerComplexObjectTag(new ItemStack(TABlocks.ARCANE_TRAPDOOR_WOOD), new AspectList().add(Aspect.PROTECT, 7));
    	proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 0), new AspectList().add(Aspect.EARTH, 5).add(Aspect.VOID, 5));
    	proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 1), new AspectList().add(Aspect.EARTH, 3).add(Aspect.VOID, 3).add(Aspect.FLUX, 3));
    	proxy.registerObjectTag(new ItemStack(TABlocks.STONE, 1, 2), new AspectList().add(Aspect.EARTH, 3).add(Aspect.VOID, 3).add(Aspect.FLUX, 3));
    	proxy.registerObjectTag(new ItemStack(TABlocks.TAINT_FLOWER), new AspectList().add(Aspect.FLUX, 10).add(Aspect.PLANT, 5));
    	proxy.registerComplexObjectTag(new ItemStack(TABlocks.VIS_REGENERATOR), new AspectList().add(Aspect.AURA, 20).add(Aspect.MECHANISM, 15).add(Aspect.ENERGY, 5));
    	proxy.registerComplexObjectTag(new ItemStack(TABlocks.WARDED_CHEST), new AspectList().add(Aspect.PROTECT, 7));
    }

}
