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

package thecodex6824.thaumicaugmentation.common.integration;

import electroblob.wizardry.entity.projectile.EntityMagicFireball;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;

public class IntegrationEBWizardry implements IIntegrationHolder {

    @Override
    public void preInit() {}
    
    @Override
    public void init() {}
    
    @Override
    public void postInit() {}
    
    @SuppressWarnings("null")
    public void onDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
            if (event.getSource().getImmediateSource() instanceof EntityMagicFireball &&
                    !ThaumcraftCapabilities.knowsResearch(player, "f_fireball")) {
                
                IPlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
                if (knowledge.addResearch("f_fireball")) {
                    knowledge.sync(player);
                    player.sendStatusMessage(new TextComponentTranslation("got.projectile").setStyle(
                            new Style().setColor(TextFormatting.DARK_PURPLE)), true);
                }
            }
        }
    }
    
}
