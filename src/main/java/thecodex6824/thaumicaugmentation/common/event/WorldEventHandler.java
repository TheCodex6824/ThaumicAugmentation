/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.event.RiftJarVoidItemEvent;
import thecodex6824.thaumicaugmentation.common.entity.EntityPrimalWisp;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public class WorldEventHandler {
    
    @SubscribeEvent
    public static void onItemVoid(RiftJarVoidItemEvent event) {
        if (event.getItemStack().getItem() == TAItems.ELDRITCH_LOCK_KEY || event.getItemStack().getItem() == TAItems.RESEARCH_NOTES) {
            if (!event.isSimulated()) {
                BlockPos pos = event.getPosition();
                event.getWorld().destroyBlock(pos, false);
                event.getWorld().createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 3.0F, false);
            }
            
            event.setCanceled(true);
        }
        else if (!event.isSimulated() && event.getItemStack().getItem() == TAItems.RIFT_JAR) {
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
