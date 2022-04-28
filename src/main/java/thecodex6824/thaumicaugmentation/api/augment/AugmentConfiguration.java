/**
 *  Thaumic Augmentation
 *  Copyright (c) 2022 KevoHoff.
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

package thecodex6824.thaumicaugmentation.api.augment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentConfiguration;

/**
* Default implementation of the Augmentable Item capability.
* @author KevoHoff
* 
*/
public class AugmentConfiguration implements IAugmentConfiguration {

    private Map<Integer, ItemStack> configuration;
    
    public AugmentConfiguration() {
        configuration = new HashMap<Integer, ItemStack>();
    }
    
    public AugmentConfiguration(ItemStack[] augs) {
        configuration = new HashMap<>();
        int slot = 0;
        for (ItemStack aug : augs) {
            configuration.put(slot, aug);
            slot++;
        }
    }
    
    @Override
    public ItemStack[] getAugmentConfig() {
        return configuration.values().toArray(new ItemStack[0]);
    }

    @Override
    public void setAugment(ItemStack augment, int slot) {
        configuration.put(slot, augment);
    }

    @Override
    public boolean removeAugment(int slot) {
        boolean res = false;
        if (configuration.containsKey(slot)) {
            res = true;
            configuration.remove(slot);
        }
        
        return res;
    }

    @Override
    public boolean isAugmentAcceptable(ItemStack augment) {
        boolean res = true;
        if (configuration.containsValue(augment)) {
            res = false;
        }
        return res;    // TODO: Add augment acceptable method; is it specific to an augmentable item?
    }
    
}
