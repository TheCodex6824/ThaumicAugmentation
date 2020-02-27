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

package thecodex6824.thaumicaugmentation.api;

import net.minecraft.util.ResourceLocation;

public final class TALootTables {

    private TALootTables() {}
    
    public static final ResourceLocation ELDRITCH_GUARDIAN = new ResourceLocation(
            ThaumicAugmentationAPI.MODID, "entity/eldritch_guardian");
    public static final ResourceLocation TAINT_MOB = new ResourceLocation(
            ThaumicAugmentationAPI.MODID, "entity/taint");
    public static final ResourceLocation AUTOCASTER = new ResourceLocation(
            ThaumicAugmentationAPI.MODID, "entity/autocaster");
    public static final ResourceLocation AUTOCASTER_ELDRITCH = new ResourceLocation(
            ThaumicAugmentationAPI.MODID, "entity/autocaster_eldritch");
    
    public static ResourceLocation[] getAllLootTables() {
        return new ResourceLocation[] {ELDRITCH_GUARDIAN, TAINT_MOB, AUTOCASTER, AUTOCASTER_ELDRITCH};
    }
    
}
