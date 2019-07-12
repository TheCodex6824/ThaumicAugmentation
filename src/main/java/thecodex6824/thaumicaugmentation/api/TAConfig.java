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

import thecodex6824.thaumicaugmentation.api.config.ConfigOptionBoolean;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionDouble;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionDoubleList;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionInt;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionIntList;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionStringToIntMap;
import thecodex6824.thaumicaugmentation.api.internal.TAInternals;

/**
 * Holds all of the configuration variables for Thaumic Augmentation. They will be
 * synced across sides when needed.
 * @author TheCodex6824
 */
public final class TAConfig {

    private TAConfig() {}
    
    public static ConfigOptionDoubleList gauntletVisDiscounts;
    public static ConfigOptionDoubleList gauntletCooldownModifiers;

    public static ConfigOptionInt voidseerArea;

    public static ConfigOptionDouble voidBootsLandSpeedBoost;
    public static ConfigOptionDouble voidBootsWaterSpeedBoost;
    public static ConfigOptionDouble voidBootsJumpBoost;
    public static ConfigOptionDouble voidBootsJumpFactor;
    public static ConfigOptionDouble voidBootsStepHeight;
    public static ConfigOptionDouble voidBootsSneakReduction;

    public static ConfigOptionBoolean opWardOverride;
    public static ConfigOptionBoolean disableWardFocus;

    public static ConfigOptionBoolean castedLightSimpleRenderer;

    public static ConfigOptionIntList defaultGauntletColors;
    public static ConfigOptionInt defaultVoidBootsColor;

    public static ConfigOptionInt emptinessDimID;
    public static ConfigOptionBoolean disableEmptiness;
    public static ConfigOptionDouble emptinessMoveFactor;
    public static ConfigOptionInt fractureGenChance;
    public static ConfigOptionStringToIntMap fractureDimList;
    public static ConfigOptionInt fractureLocatorUpdateInterval;
    public static ConfigOptionBoolean fracturesAlwaysTeleport;
    
    public static ConfigOptionBoolean disableCoremod;
    
    /**
     * Registers a callback to be notified when the config is synced or updated.
     * @param listener The callback
     */
    public static void addConfigListener(Runnable listener) {
        TAInternals.addConfigListener(listener);
    }
    
    /**
     * Removes a callback previously registered with {@link TAConfig#addConfigListener(Runnable)}
     * @param listener The callback
     * @return If the provided callback existed and was removed
     */
    public static boolean removeConfigListener(Runnable listener) {
        return TAInternals.removeConfigListener(listener);
    }

}
