/**
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Holds an instance of the capability for {@link IAugmentConfigurationStorage}.
 * @author TheCodex6824
 */
public final class CapabilityAugmentConfigurationStorage {

    private CapabilityAugmentConfigurationStorage() {}
    
    @CapabilityInject(IAugmentConfigurationStorage.class)
    public static final Capability<IAugmentConfigurationStorage> AUGMENT_CONFIGURATION_STORAGE = null;
    
}
