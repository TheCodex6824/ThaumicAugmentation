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

package thecodex6824.thaumicaugmentation.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import thaumcraft.api.casters.FocusPackage;

/**
 * Event fired when the caster cooldown is being calculated. This may not be fired for all casters - 
 * it is only guaranteed for Thaumic Augmentation casters.
 * @author TheCodex6824
 */
public class CasterCooldownEvent extends CastEvent {

    private int cooldown;
    
    public CasterCooldownEvent(EntityLivingBase living, ItemStack casterStack, FocusPackage fPackage, int cooldown) {
        super(living, casterStack, fPackage);
        this.cooldown = cooldown;
    }
    
    public int getCooldown() {
        return cooldown;
    }
    
    public void setCooldown(int newCooldown) {
        cooldown = newCooldown;
    }
    
}
