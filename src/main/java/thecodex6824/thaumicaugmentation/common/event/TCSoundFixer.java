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

package thecodex6824.thaumicaugmentation.common.event;

import org.objectweb.asm.Type;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.core.transformer.TransformUtil;

/**
 * TC seems to have quite a few instances where the sound is played using {@link EntityPlayer#playSound(net.minecraft.util.SoundEvent, float, float)}.
 * This would be fine except for the fact it is only called server side, and internally it calls {@link World#playSound(EntityPlayer, double, double, double, net.minecraft.util.SoundEvent, net.minecraft.util.SoundCategory, float, float)}.
 * This function on the server side plays the sound to everyone *except* the passed in player, expecting the same call to happen client side.
 * Since it never happens, the client player never gets to hear the sounds.
 */
@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public class TCSoundFixer {

    protected static final String CASTER_MANAGER = "thaumcraft.common.items.casters.CasterManager";
    protected static final String CHANGE_FOCUS = "changeFocus";
    
    protected static final String LOOT_BAG = "thaumcraft.common.items.curios.ItemLootBag";
    protected static final String LOOT_RIGHT_CLICK = TransformUtil.remapMethodName("net/minecraft/item/Item", "func_77659_a", Type.getType(ActionResult.class),
            Type.getType(World.class), Type.getType(EntityPlayer.class), Type.getType(EnumHand.class));
    
    protected static final String PHIAL = "thaumcraft.common.items.consumables.ItemPhial";
    protected static final String PHIAL_USE_FIRST = "onItemUseFirst";
    
    protected static boolean frameMatches(StackTraceElement frame, String className, String methodName) {
        return frame.getClassName().equals(className) && frame.getMethodName().equals(methodName);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void fixEntitySounds(PlaySoundAtEntityEvent event) {
        if (event.getEntity() instanceof EntityPlayer && !event.getEntity().getEntityWorld().isRemote) {
            if (event.getSound() == SoundsTC.ticks) {
                StackTraceElement[] stack = new Throwable().getStackTrace();
                if (stack.length >= 8) {
                    StackTraceElement frame = stack[7];
                    if (frameMatches(frame, CASTER_MANAGER, CHANGE_FOCUS)) {
                        event.getEntity().getEntityWorld().playSound(null, event.getEntity().getPosition(), event.getSound(),
                                event.getCategory(), event.getVolume(), event.getPitch());
                        event.setCanceled(true);
                    }
                }
            }
            else if (event.getSound() == SoundsTC.coins) {
                StackTraceElement[] stack = new Throwable().getStackTrace();
                if (stack.length >= 8) {
                    StackTraceElement frame = stack[7];
                    if (frameMatches(frame, LOOT_BAG, LOOT_RIGHT_CLICK)) {
                        event.getEntity().getEntityWorld().playSound(null, event.getEntity().getPosition(), event.getSound(),
                                event.getCategory(), event.getVolume(), event.getPitch());
                        event.setCanceled(true);
                    }
                }
            }
            else if (event.getSound() == SoundEvents.ITEM_BOTTLE_FILL) {
                StackTraceElement[] stack = new Throwable().getStackTrace();
                if (stack.length >= 8) {
                    StackTraceElement frame = stack[7];
                    if (frameMatches(frame, PHIAL, PHIAL_USE_FIRST)) {
                        event.getEntity().getEntityWorld().playSound(null, event.getEntity().getPosition(), event.getSound(),
                                event.getCategory(), event.getVolume(), event.getPitch());
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
    
}
