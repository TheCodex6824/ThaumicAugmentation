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

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.capability.CapabilityAugmentableItem;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID, value = Side.CLIENT)
public final class ClientEventHandler {

    private ClientEventHandler() {}
    
    private static void handleAugmentTooltips(ItemTooltipEvent event, IAugmentableItem cap) {
        for (ItemStack augment : cap.getAllAugments()) {
            if (augment.getItem() instanceof IAugment) {
                event.getToolTip().add("    " + new TextComponentTranslation(augment.getItem().getTranslationKey(augment)).getFormattedText());
                IAugment aug = (IAugment) augment.getItem();
                if (aug.hasAdditionalAugmentTooltip(augment)) {
                    for (String s : aug.getAdditionalAugmentTooltip(augment))
                        event.getToolTip().add("        " + s);
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
            IAugmentableItem cap = event.getItemStack().getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
            if (cap.isAugmented()) {
                event.getToolTip().add(TextFormatting.BOLD + "" + TextFormatting.RED + new TextComponentTranslation(
                        "thaumicaugmentation.text.augmented").getFormattedText());
                handleAugmentTooltips(event, cap);
            }
        }
    }
    
}
