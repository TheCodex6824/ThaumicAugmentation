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

import java.util.HashMap;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import net.minecraft.entity.player.EntityPlayer;

public class PlayerMovementAbilityManager {

	public static enum MovementType {
		DRY_GROUND,
		WATER_GROUND,
		WATER_SWIM,
		JUMP_BEGIN,
		JUMP_FACTOR,
		STEP_HEIGHT
	}

	private static class PlayerFunctions {

		public PlayerFunctions(BiFunction<EntityPlayer, MovementType, Float> func, Predicate<EntityPlayer> pred) {
			tickFunction = func;
			continueFunction = pred;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof PlayerFunctions) {
				PlayerFunctions func = (PlayerFunctions) obj;
				return tickFunction.equals(func.tickFunction) && continueFunction.equals(func.continueFunction);
			}

			return false;
		}

		@Override
		public int hashCode() {
			int ret = 7 * 31;
			ret += tickFunction.hashCode();
			ret *= 31;
			ret += continueFunction.hashCode();
			return ret;
		}

		public BiFunction<EntityPlayer, MovementType, Float> tickFunction;
		public Predicate<EntityPlayer> continueFunction;
	}

	private static class OldMovementData {

		public OldMovementData(float s, float j) {
			stepHeight = s;
			jumpMovementFactor = j;
		}

		public float stepHeight;
		public float jumpMovementFactor;	
	}

	private static HashMap<String, OldMovementData> oldMovementValues = new HashMap<>();
	private static Multimap<String, PlayerFunctions> players = 
			MultimapBuilder.hashKeys().arrayListValues().build();

	public static void put(EntityPlayer player, BiFunction<EntityPlayer, MovementType, Float> func, Predicate<EntityPlayer> continueApplying) {
		if (!oldMovementValues.containsKey(player.getCachedUniqueIdString()))
			oldMovementValues.put(player.getCachedUniqueIdString(), new OldMovementData(player.stepHeight, player.jumpMovementFactor));

		players.put(player.getCachedUniqueIdString(), new PlayerFunctions(func, continueApplying));
	}

	public static boolean remove(EntityPlayer player, BiFunction<EntityPlayer, MovementType, Float> func, Predicate<EntityPlayer> pred) {
		boolean result = players.remove(player.getCachedUniqueIdString(), new PlayerFunctions(func, pred));
		if (players.containsKey(player.getCachedUniqueIdString()) && players.get(player.getCachedUniqueIdString()).size() == 0) {

			OldMovementData data = oldMovementValues.get(player.getCachedUniqueIdString());
			player.stepHeight -= player.stepHeight - data.stepHeight;
			player.jumpMovementFactor -= player.jumpMovementFactor - data.jumpMovementFactor;
			players.removeAll(player.getCachedUniqueIdString());
			if (oldMovementValues.containsKey(player.getCachedUniqueIdString()))
				oldMovementValues.remove(player.getCachedUniqueIdString());
		}

		return result;
	}

	public static boolean playerHasAbility(EntityPlayer player, BiFunction<EntityPlayer, MovementType, Float> func, Predicate<EntityPlayer> pred) {
		return players.containsEntry(player.getCachedUniqueIdString(), new PlayerFunctions(func, pred));
	}

	private static boolean isPlayerMovingNotVertically(EntityPlayer player) {
		return player.moveForward > 0.0001F || player.moveForward < -0.0001F || player.moveStrafing > 0.0F || 
				player.moveStrafing < -0.0001F;
	}

	private static void movePlayer(EntityPlayer player, float factor) {
		if ((player.moveForward > 0.0001F || player.moveForward < -0.0001F) && (player.moveStrafing > 0.0F || 
				player.moveStrafing < -0.0001F))

			player.moveRelative(factor * Math.signum(player.moveStrafing), 0.0F, factor * Math.signum(player.moveForward), 1.0F);
		else if (player.moveForward > 0.0001F || player.moveForward < -0.0001F)
			player.moveRelative(0.0F, 0.0F, factor * Math.signum(player.moveForward), 1.0F);
		else
			player.moveRelative(factor * Math.signum(player.moveStrafing), 0.0F, 0.0F, 1.0F);
	}

	public static void tick(EntityPlayer player) {
		if (player.getEntityWorld().isRemote && players.containsKey(player.getCachedUniqueIdString())) {
			OldMovementData data = oldMovementValues.get(player.getCachedUniqueIdString());
			float stepHeight = data.stepHeight;
			float jumpMovementFactor = data.jumpMovementFactor;
			Iterator<PlayerFunctions> it = players.get(player.getCachedUniqueIdString()).iterator();
			while (it.hasNext()) {
				PlayerFunctions func = it.next();
				if (!func.continueFunction.test(player)) {
					it.remove();
					if (players.containsKey(player.getCachedUniqueIdString()) && players.get(player.getCachedUniqueIdString()).size() == 0 &&
							oldMovementValues.containsKey(player.getCachedUniqueIdString())) {

						player.stepHeight -= player.stepHeight - data.stepHeight;
						player.jumpMovementFactor -= player.jumpMovementFactor - data.jumpMovementFactor;
						players.removeAll(player.getCachedUniqueIdString());
						oldMovementValues.remove(player.getCachedUniqueIdString());
					}

					return;
				}

				stepHeight += func.tickFunction.apply(player, MovementType.STEP_HEIGHT);
				if (player.onGround && isPlayerMovingNotVertically(player))
					movePlayer(player, func.tickFunction.apply(player, player.isInWater() ? MovementType.WATER_GROUND : MovementType.DRY_GROUND));
				else {
					if (player.moveForward > 0.0F && player.isInWater())
						movePlayer(player, func.tickFunction.apply(player, MovementType.WATER_SWIM));

					jumpMovementFactor += func.tickFunction.apply(player, MovementType.JUMP_FACTOR);
				}
			}

			player.stepHeight = stepHeight;
			player.jumpMovementFactor = jumpMovementFactor;
		}
	}

	public static void onJump(EntityPlayer player) {
		if (players.containsKey(player.getCachedUniqueIdString())) {
			for (PlayerFunctions func : players.get(player.getCachedUniqueIdString()))
				player.motionY += func.tickFunction.apply(player, MovementType.JUMP_BEGIN);
		}
	}

}
