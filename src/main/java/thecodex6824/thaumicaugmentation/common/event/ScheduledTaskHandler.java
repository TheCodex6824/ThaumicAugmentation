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

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.util.ISchedulableTask;

import java.util.Iterator;
import java.util.Map.Entry;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public class ScheduledTaskHandler {
    
    private static final Object2IntOpenHashMap<ISchedulableTask> TEMP = new Object2IntOpenHashMap<>();
    private static final Object2IntOpenHashMap<ISchedulableTask> ENTRIES = new Object2IntOpenHashMap<>();
    
    private static boolean iterating = false;
    
    public static void registerTask(ISchedulableTask task, int delay) {
        if (!iterating)
            ENTRIES.put(task, delay);
        else
            TEMP.put(task, delay);
    }
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == Phase.END && !ENTRIES.isEmpty()) {
            iterating = true;
            Iterator<Object2IntOpenHashMap.Entry<ISchedulableTask>> iterator = ENTRIES.object2IntEntrySet().iterator();
            while (iterator.hasNext()) {
                Entry<ISchedulableTask, Integer> task = iterator.next();
                if (task.getValue() == 0) {
                    task.getKey().execute();
                    iterator.remove();
                }
                else
                    ENTRIES.addTo(task.getKey(), -1);
            }
            
            if (!TEMP.isEmpty()) {
                ENTRIES.putAll(TEMP);
                TEMP.clear();
            }
            
            iterating = false;
        }
    }
    
}
