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

package thecodex6824.thaumicaugmentation.common.golem;

import net.minecraft.util.ResourceLocation;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;

public class SealAttackAdvanced extends SealAttack implements ISealConfigToggles {

    private static final ResourceLocation ICON = new ResourceLocation(ThaumicAugmentationAPI.MODID, "items/seal/seal_attack_advanced");
    
    @Override
    public String getKey() {
        return ThaumicAugmentationAPI.MODID + ":attack_advanced";
    }
    
    @Override
    public SealToggle[] getToggles() {
        return props;
    }
    
    @Override
    public void setToggle(int index, boolean val) {
        props[index].setValue(val);
    }
    
    @Override
    public int[] getGuiCategories() {
        return new int[] {3, 0, 4};
    }
    
    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[] {EnumGolemTrait.FIGHTER, EnumGolemTrait.SMART};
    }
    
    @Override
    public ResourceLocation getSealIcon() {
        return ICON;
    }
    
}
