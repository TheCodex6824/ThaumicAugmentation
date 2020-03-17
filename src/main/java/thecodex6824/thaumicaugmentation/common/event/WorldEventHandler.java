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

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.common.lib.events.ServerEvents;
import thaumcraft.common.world.aura.AuraHandler;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.event.RiftJarVoidItemEvent;
import thecodex6824.thaumicaugmentation.api.world.TADimensions;
import thecodex6824.thaumicaugmentation.common.entity.EntityPrimalWisp;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public class WorldEventHandler {

    // for performance, we only want to check the rift map if TC is about to check it
    private static final Field TC_SERVER_TICKS;
    
    static {
        Field f = null;
        try {
            f = ServerEvents.class.getDeclaredField("serverTicks");
            f.setAccessible(true);
        }
        catch (Exception ex) {
            FMLCommonHandler.instance().raiseException(ex, "Failed to access Thaumcraft's ServerEvents#serverTicks", true);
        }
        
        TC_SERVER_TICKS = f;
    }
    
    @SuppressWarnings("unchecked")
    private static int getTCEventHandlerServerTicks() {
        try {
            Map<Integer, Integer> ticks = (Map<Integer, Integer>) TC_SERVER_TICKS.get(null);
            if (ticks.containsKey(TADimensions.EMPTINESS.getId()))
                return ((Map<Integer, Integer>) TC_SERVER_TICKS.get(null)).get(TADimensions.EMPTINESS.getId());
        }
        catch (Exception ex) {
            FMLCommonHandler.instance().raiseException(ex, "Failed to access Thaumcraft's ServerEvents#serverTicks", true);
        }
        
        return 0;
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onWorldTick(WorldTickEvent event) {
        if (!TAConfig.disableEmptiness.getValue() && event.side == Side.SERVER && event.phase == Phase.END && 
                event.world.provider.getDimension() == TADimensions.EMPTINESS.getId() && getTCEventHandlerServerTicks() % 20 == 0)
            AuraHandler.riftTrigger.remove(TADimensions.EMPTINESS.getId());
    }
    
    @SubscribeEvent
    public static void onItemVoid(RiftJarVoidItemEvent event) {
        if (!event.isSimulated()) {
            if (event.getItemStack().getItem() == TAItems.ELDRITCH_LOCK_KEY) {
                BlockPos pos = event.getPosition();
                event.getWorld().destroyBlock(pos, false);
                event.getWorld().createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 3.0F, false);
                event.setCanceled(true);
            }
            else if (event.getItemStack().getItem() == TAItems.RIFT_JAR) {
                NBTTagCompound tag = event.getItemStack().getTagCompound();
                if (tag != null && tag.hasKey("seed", NBT.TAG_INT) && tag.getInteger("size") > 0) {
                    BlockPos pos = event.getPosition();
                    event.getWorld().destroyBlock(pos, false);
                    event.getWorld().createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 3.0F, false);
                    EntityPrimalWisp wisp = new EntityPrimalWisp(event.getWorld());
                    wisp.setPositionAndRotation(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                            event.getWorld().rand.nextInt(360) - 180, 0.0F);
                    wisp.rotationYawHead = wisp.rotationYaw;
                    wisp.renderYawOffset = wisp.rotationYaw;
                    wisp.onInitialSpawn(event.getWorld().getDifficultyForLocation(pos), null);
                    event.getWorld().spawnEntity(wisp);
                }
            }
        }
    }
    
}
