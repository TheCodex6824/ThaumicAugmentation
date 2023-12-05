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
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionEnum;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionFloat;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionInt;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionIntList;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionIntSet;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionLong;
import thecodex6824.thaumicaugmentation.api.config.ConfigOptionStringList;
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
    public static ConfigOptionBoolean serverMovementCalculation;

    public static ConfigOptionBoolean opWardOverride;
    public static ConfigOptionBoolean singlePlayerWardOverride;
    public static ConfigOptionBoolean disableWardFocus;
    
    public enum TileWardMode {
        NONE,
        NOTICK,
        ALL
    }
    
    public static ConfigOptionEnum<TileWardMode> tileWardMode;
    public static ConfigOptionBoolean disableExpensiveWardFeatures;

    public static ConfigOptionBoolean reducedEffects;
    public static ConfigOptionBoolean optimizedFluxRiftRenderer;
    public static ConfigOptionBoolean enableBoosterKeybind;
    public static ConfigOptionBoolean disableShaders;
    public static ConfigOptionStringList morphicArmorExclusions;
    public static ConfigOptionBoolean disableCreativeOnlyText;
    public static ConfigOptionBoolean disableStabilizerText;
    public static ConfigOptionBoolean disableFramebuffers;
    public static ConfigOptionInt bulkRenderDistance;

    public static ConfigOptionIntList defaultGauntletColors;
    public static ConfigOptionInt defaultVoidBootsColor;

    public static ConfigOptionInt emptinessDimID;
    public static ConfigOptionBoolean disableEmptiness;
    public static ConfigOptionDouble emptinessMoveFactor;
    public static ConfigOptionInt fractureGenChance;
    public static ConfigOptionStringList fractureDimList;
    public static ConfigOptionInt fractureLocatorUpdateInterval;
    public static ConfigOptionBoolean fracturesAlwaysTeleport;
    
    public static ConfigOptionBoolean disableCoremod;
    public static ConfigOptionStringList disabledTransformers;
    
    public static ConfigOptionBoolean gauntletCastAnimation;
    
    public static ConfigOptionLong terraformerImpetusCost;
    public static ConfigOptionLong shieldFocusImpetusCost;
    
    public static ConfigOptionInt impetusGeneratorEnergyPerImpetus;
    public static ConfigOptionInt impetusGeneratorMaxExtract;
    public static ConfigOptionInt impetusGeneratorBufferSize;
    
    public static ConfigOptionBoolean allowWussRiftSeed;
    
    public static ConfigOptionFloat cannonBeamDamage;
    public static ConfigOptionLong cannonBeamCostInitial;
    public static ConfigOptionDouble cannonBeamCostTick;
    public static ConfigOptionDouble cannonBeamRange;
    
    public static ConfigOptionFloat cannonRailgunDamage;
    public static ConfigOptionLong cannonRailgunCost;
    public static ConfigOptionInt cannonRailgunCooldown;
    public static ConfigOptionDouble cannonRailgunRange;
    
    public static ConfigOptionFloat cannonBurstDamage;
    public static ConfigOptionLong cannonBurstCost;
    public static ConfigOptionInt cannonBurstCooldown;
    public static ConfigOptionDouble cannonBurstRange;
    
    public static ConfigOptionFloat primalCutterDamage;
    
    public static ConfigOptionStringList deniedCategories;
    
    public static ConfigOptionBoolean generateSpires;
    public static ConfigOptionInt spireMinDist;
    public static ConfigOptionInt spireSpacing;
    
    public static ConfigOptionDouble experienceModifierCap;
    public static ConfigOptionDouble experienceModifierBase;
    public static ConfigOptionDouble experienceModifierScale;
    
    public static ConfigOptionDouble elementalModifierPositiveFactor;
    public static ConfigOptionDouble elementalModifierNegativeFactor;
    
    public static ConfigOptionDouble dimensionalModifierOverworldPostiveFactor;
    public static ConfigOptionDouble dimensionalModifierOverworldNegativeFactor;
    public static ConfigOptionIntSet dimensionalModifierOverworldDims;
    
    public static ConfigOptionDouble dimensionalModifierNetherPostiveFactor;
    public static ConfigOptionDouble dimensionalModifierNetherNegativeFactor;
    public static ConfigOptionIntSet dimensionalModifierNetherDims;
    
    public static ConfigOptionDouble dimensionalModifierEndPostiveFactor;
    public static ConfigOptionDouble dimensionalModifierEndNegativeFactor;
    public static ConfigOptionIntSet dimensionalModifierEndDims;
    
    public static ConfigOptionDouble dimensionalModifierEmptinessPostiveFactor;
    public static ConfigOptionDouble dimensionalModifierEmptinessNegativeFactor;
    public static ConfigOptionIntSet dimensionalModifierEmptinessDims;
    
    public static ConfigOptionInt frenzyModifierCooldown;
    public static ConfigOptionDouble frenzyModifierScaleFactor;
    public static ConfigOptionInt frenzyModifierMaxLevel;
    
    public static ConfigOptionDouble impetusConductorFactor;
    
    public static ConfigOptionBoolean movementCompat;
    
    public static ConfigOptionFloat baseHarnessSpeed;
    public static ConfigOptionDouble baseHarnessCost;
    
    public static ConfigOptionFloat gyroscopeHarnessSpeed;
    public static ConfigOptionDouble gyroscopeHarnessCost;
    
    public static ConfigOptionFloat girdleHarnessSpeed;
    public static ConfigOptionDouble girdleHarnessCost;
    
    public static ConfigOptionDouble elytraHarnessBoostCost;
    
    public static ConfigOptionBoolean allowOfflinePlayerResearch;
    
    public static ConfigOptionBoolean undeadEldritchGuardians;
    
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
