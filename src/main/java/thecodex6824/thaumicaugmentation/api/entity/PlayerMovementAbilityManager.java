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

package thecodex6824.thaumicaugmentation.api.entity;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.WeakHashMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumicaugmentation.api.TAConfig;

/**
 * Class that manages non-attribute movement changes.
 * @author TheCodex6824
 */
public final class PlayerMovementAbilityManager {

    private PlayerMovementAbilityManager() {}
    
    public static enum MovementType {
        DRY_GROUND,
        WATER_GROUND,
        WATER_SWIM,
        JUMP_BEGIN,
        JUMP_FACTOR,
        STEP_HEIGHT
    }

    private static final class PlayerFunctions {

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

    private static final class OldMovementData {

        public OldMovementData(float s, float j) {
            stepHeight = s;
            jumpMovementFactor = j;
        }

        public float stepHeight;
        public float jumpMovementFactor;
    }

    private static final class FlyData {
        
        public FlyData(boolean e, boolean f, float s) {
            flyEnabled = e;
            wasFlying = f;
            flySpeed = s;
        }
        
        public boolean flyEnabled;
        public boolean wasFlying;
        public float flySpeed;
        
    }
    
    private static WeakHashMap<EntityPlayer, FlyData> flyData = new WeakHashMap<>();
    private static WeakHashMap<EntityPlayer, OldMovementData> oldMovementValues = new WeakHashMap<>();
    private static WeakHashMap<EntityPlayer, LinkedList<PlayerFunctions>> players = 
            new WeakHashMap<>();

    public static boolean isValidSideForMovement(EntityPlayer player) {
        // we want to update if:
        // 1. we are the client (to move differently at all)
        // 2. we are the DEDICATED server and it is enabled in the config (to update network vars)
        // In SP this is shared between client and server so picking client is fine
        return player.getEntityWorld().isRemote ||
                (FMLCommonHandler.instance().getSide() == Side.SERVER && TAConfig.serverMovementCalculation.getValue());
    }
    
    public static void put(EntityPlayer player, BiFunction<EntityPlayer, MovementType, Float> func, Predicate<EntityPlayer> continueApplying) {
        if (!oldMovementValues.containsKey(player))
            oldMovementValues.put(player, new OldMovementData(player.stepHeight, player.jumpMovementFactor));

        if (players.containsKey(player))
            players.get(player).add(new PlayerFunctions(func, continueApplying));
        else {
            LinkedList<PlayerFunctions> newList = new LinkedList<>();
            newList.add(new PlayerFunctions(func, continueApplying));
            players.put(player, newList);
        }
    }

    public static boolean remove(EntityPlayer player, BiFunction<EntityPlayer, MovementType, Float> func, Predicate<EntityPlayer> pred) {
        boolean result = players.remove(player.getCachedUniqueIdString(), new PlayerFunctions(func, pred));
        if (players.containsKey(player) && players.get(player).size() == 0) {

            OldMovementData data = oldMovementValues.get(player);
            player.stepHeight -= player.stepHeight - data.stepHeight;
            player.jumpMovementFactor -= player.jumpMovementFactor - data.jumpMovementFactor;
            players.remove(player);
            if (oldMovementValues.containsKey(player))
                oldMovementValues.remove(player);
        }

        return result;
    }

    public static boolean playerHasAbility(EntityPlayer player, BiFunction<EntityPlayer, MovementType, Float> func, Predicate<EntityPlayer> pred) {
        return players.containsKey(player) && players.get(player).contains(new PlayerFunctions(func, pred));
    }

    private static boolean isPlayerMovingNotVertically(EntityPlayer player) {
        return player.moveForward > 0.0001F || player.moveForward < -0.0001F || player.moveStrafing > 0.0F || 
                player.moveStrafing < -0.0001F;
    }

    private static void movePlayer(EntityPlayer player, float factor) {
        if ((player.moveForward > 0.0001F || player.moveForward < -0.0001F) && (player.moveStrafing > 0.0F || 
                player.moveStrafing < -0.0001F)) {

            float normalized = (float) Math.sqrt(player.moveStrafing * player.moveStrafing + player.moveForward * player.moveForward);
            player.moveRelative(factor * (Math.signum(player.moveStrafing) / (normalized > 1 ? normalized : 1 / normalized)), 0.0F, factor * (Math.signum(player.moveForward) / (normalized > 1 ? normalized : 1 / normalized)), 1.0F);
        }
        else if (player.moveForward > 0.0001F || player.moveForward < -0.0001F)
            player.moveRelative(0.0F, 0.0F, factor * Math.signum(player.moveForward), 1.0F);
        else
            player.moveRelative(factor * Math.signum(player.moveStrafing), 0.0F, 0.0F, 1.0F);
    }

    public static void tick(EntityPlayer player) {
        if (players.containsKey(player)) {
            OldMovementData data = oldMovementValues.get(player);
            float stepHeight = data.stepHeight;
            float jumpMovementFactor = data.jumpMovementFactor;
            ListIterator<PlayerFunctions> it = players.get(player).listIterator();
            while (it.hasNext()) {
                PlayerFunctions func = it.next();
                if (!func.continueFunction.test(player)) {
                    it.remove();
                    if (players.containsKey(player) && players.get(player).size() == 0 &&
                            oldMovementValues.containsKey(player)) {

                        player.stepHeight -= player.stepHeight - data.stepHeight;
                        player.jumpMovementFactor -= player.jumpMovementFactor - data.jumpMovementFactor;
                        players.remove(player);
                        oldMovementValues.remove(player);
                    }

                    continue;
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
        if (players.containsKey(player)) {
            for (PlayerFunctions func : players.get(player))
                player.motionY += func.tickFunction.apply(player, MovementType.JUMP_BEGIN);
        }
    }
    
    public static void recordFlyState(EntityPlayer player) {
        flyData.put(player, new FlyData(player.capabilities.allowFlying, player.capabilities.isFlying, player.capabilities.getFlySpeed()));
    }
    
    public static boolean popAndApplyFlyState(EntityPlayer player) {
        FlyData data = flyData.remove(player);
        if (data != null) {
            player.capabilities.allowFlying = data.flyEnabled;
            player.capabilities.isFlying &= data.wasFlying;
            player.capabilities.setFlySpeed(data.flySpeed);
            player.sendPlayerAbilities();
            return true;
        }
        else
            return false;
    }

}
