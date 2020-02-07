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

import java.util.Iterator;
import java.util.LinkedList;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.util.ISchedulableTask;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public class ScheduledEventHandler {
    
    private static final LinkedList<ISchedulableTask> TEMP = new LinkedList<>();
    private static final LinkedList<ISchedulableTask> ENTRIES = new LinkedList<>();
    
    private static boolean iterating = false;
    
    public static void registerTask(ISchedulableTask task) {
        if (!iterating)
            ENTRIES.add(task);
        else
            TEMP.add(task);
    }
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == Phase.END) {
            iterating = true;
            Iterator<ISchedulableTask> iterator = ENTRIES.iterator();
            while (iterator.hasNext()) {
                ISchedulableTask task = iterator.next();
                if (!task.execute())
                    iterator.remove();
            }
            
            if (!TEMP.isEmpty()) {
                ENTRIES.addAll(TEMP);
                TEMP.clear();
            }
            
            iterating = false;
        }
    }
    
}
