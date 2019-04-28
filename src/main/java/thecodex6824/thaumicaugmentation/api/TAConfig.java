/**
 *	Thaumic Augmentation
 *	Copyright (c) 2019 TheCodex6824.
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

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Holds configuration variables for Thaumic Augmentation.
 * @author TheCodex6824
 * 
 */
@Config(modid = ThaumicAugmentationAPI.MODID)
@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public class TAConfig {
	
	// TODO localize all the strings here
	
	@Name("GauntletVisDiscounts")
	@Comment({
	    "The discounts that will be applied to the vis cost of foci used in the thaumium and void metal caster gauntlets."
	})
	@RangeDouble(min = 0.0F, max = 1.0F)
	public static double[] gauntletVisDiscounts = {0.1, 0.3};
	
	@Name("GauntletCooldownModifiers")
	@Comment({
		"The multipliers that will be applied to the use cooldowns of the Thaumium and Void Metal caster gauntlets."
	})
	@RangeDouble(min = 0.0F, max = 1.0F)
	public static double[] gauntletCooldownModifiers = {0.80, 0.9};
	
	@Name("VoidseerExtraArea")
	@Comment({
		"The extra square area for the voidseer gauntlet, in chunks.",
		"An area of 3, for example, will mean vis will be taken in a 3x3 chunk area around the caster."
	})
	@RangeInt(min = 1, max = 32)
	public static int voidseerArea = 3;
	
	@Name("VoidBootsLandSpeedBoost")
	@Comment({
		"The boost applied while the wearer is on the ground, and on dry land.",
		"This is added to the base movement of the player per tick."
	})
	@RangeDouble(min = 0.0F, max = 10.0F)
	public static double voidBootsLandSpeedBoost = 0.12;
	
	@Name("VoidBootsWaterSpeedBoost")
	@Comment({
		"The boost applied while the wearer is in water.",
		"This is added to the base movement of the player per tick."
	})
	@RangeDouble(min = 0.0F, max = 10.0F)
	public static double voidBootsWaterSpeedBoost = 0.075;
	
	@Name("VoidBootsJumpBoost")
	@Comment({
		"The boost applied when the wearer jumps.",
		"This is added to the base jump height of the player."
	})
	@RangeDouble(min = 0.0F, max = 10.0F)
	public static double voidBootsJumpBoost = 0.45;
	
	@Name("VoidBootsJumpFactor")
	@Comment({
		"The boost applied to player movement while in the air.",
		"This itself is a speed, so it can make movement faster in the air than on the ground."
	})
	@RangeDouble(min = 0.0F, max = 10.0F)
	public static double voidBootsJumpFactor = 0.05;
	
	@Name("VoidBootsStepHeight")
	@Comment({
		"The boost applied to the player's step height (while not sneaking).",
		"This is added to the vanilla default value of 0.6."
	})
	@RangeDouble(min = 0.0F, max = 10.0F)
	public static double voidBootsStepHeight = 0.41;
	
	@Name("VoidBootsSneakReduction")
	@Comment({
		"Any speed boosts (not jump) will be divided by this value while sneaking"
	})
	@RangeDouble(min = 1.0F, max = 10.0F)
	public static double voidBootsSneakReduction = 4.0F;
	
	@SubscribeEvent
	public static void onConfigChanged(OnConfigChangedEvent event) {
		if (event.getModID().equals(ThaumicAugmentationAPI.MODID))
			ConfigManager.sync(ThaumicAugmentationAPI.MODID, Type.INSTANCE);
	}
	
}
