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

package thecodex6824.thaumicaugmentation.client;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID, value = Side.CLIENT)
public final class ClientEventHandler {

    private ClientEventHandler() {}
    
    private static void handleAugmentTooltips(ItemTooltipEvent event, IAugmentableItem cap) {
        LinkedList<LinkedList<String>> tooltip = new LinkedList<>();
        for (ItemStack augment : cap.getAllAugments()) {
            if (augment.hasCapability(CapabilityAugment.AUGMENT, null)) {
                LinkedList<String> thisTooltip = new LinkedList<>();
                thisTooltip.add(new TextComponentTranslation(augment.getItem().getTranslationKey(augment) + ".name").getFormattedText());
                IAugment aug = augment.getCapability(CapabilityAugment.AUGMENT, null);
                if (aug.hasAdditionalAugmentTooltip())
                    aug.appendAdditionalAugmentTooltip(thisTooltip);
                
                tooltip.add(thisTooltip);
            }
        }
        
        for (LinkedList<String> list : tooltip) {
            event.getToolTip().add("    " + list.remove(0));
            for (String str : list)
                event.getToolTip().add("        " + str);
        }
    }
    
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
            IAugmentableItem cap = event.getItemStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
            if (cap.isAugmented()) {
                event.getToolTip().add(new TextComponentTranslation("thaumicaugmentation.text.augmented", 
                        cap.getUsedAugmentSlots(), cap.getTotalAugmentSlots()).getFormattedText());
                handleAugmentTooltips(event, cap);
            }
        }
    }
    
}
