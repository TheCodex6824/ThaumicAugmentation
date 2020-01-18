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

import java.util.ArrayList;
import java.util.Collections;

import baubles.api.BaublesApi;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.IItemHandler;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.common.golems.seals.SealHandler;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.AugmentAPI;
import thecodex6824.thaumicaugmentation.api.entity.AutocasterFocusRegistry;
import thecodex6824.thaumicaugmentation.common.golem.SealAttack;
import thecodex6824.thaumicaugmentation.common.golem.SealAttackAdvanced;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusEffectLight;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusEffectVoidShield;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusEffectWard;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusEffectWater;

public final class MiscHandler {

    private MiscHandler() {}
    
    public static void preInit() {
        SealHandler.registerSeal(new SealAttack());
        SealHandler.registerSeal(new SealAttackAdvanced());
    }
    
    public static void init() {
        FocusEngine.registerElement(FocusEffectLight.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/foci/light.png"), 
                Aspect.LIGHT.getColor());
        FocusEngine.registerElement(FocusEffectWard.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/foci/ward.png"),
                Aspect.PROTECT.getColor());
        FocusEngine.registerElement(FocusEffectVoidShield.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/foci/shield.png"),
                0x5000C8);
        FocusEngine.registerElement(FocusEffectWater.class, new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/foci/water.png"),
                Aspect.WATER.getColor());
        
        AugmentAPI.addAugmentableItemSource(new ResourceLocation(ThaumicAugmentationAPI.MODID, "default"), (entity) -> {
            if (entity instanceof EntityLivingBase)
                return ((EntityLivingBase) entity).getEquipmentAndArmor();
            
            return Collections.<ItemStack>emptyList();
        });
        AugmentAPI.addAugmentableItemSource(new ResourceLocation(ThaumicAugmentationAPI.MODID, "baubles"), (entity) -> {
            if (entity instanceof EntityPlayer) {
                IItemHandler handler = BaublesApi.getBaublesHandler((EntityPlayer) entity);
                ArrayList<ItemStack> stacks = new ArrayList<>(Collections.nCopies(handler.getSlots(), ItemStack.EMPTY));
                for (int i = 0; i < stacks.size(); ++i)
                    stacks.set(i, handler.getStackInSlot(i));
                
                return stacks;
            }
            
            return Collections.<ItemStack>emptyList();
        });
        
        AutocasterFocusRegistry.registerMaxDistance("thaumcraft.BOLT", 16.0);
        AutocasterFocusRegistry.registerMaxDistance("thaumcraft.TOUCH", 4.0);
        
        BlocksTC.stairsAncient.setTranslationKey("thaumcraft.stairs_ancient_tile");
        GameRegistry.addShapedRecipe(new ResourceLocation("thaumcraft", "StairsAncient"),
                new ResourceLocation(""), new ItemStack(BlocksTC.stairsAncient, 4, 0), 
                "S  ",
                "SS ",
                "SSS",
                'S', BlocksTC.stoneAncientTile
        );
    }

}
