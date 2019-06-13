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

import java.util.ArrayDeque;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.world.WorldDataCache;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class QueuedWorldGenManager {

    private QueuedWorldGenManager() {}
    
    private static final ArrayDeque<Runnable> QUEUE = new ArrayDeque<>();
    
    public static void enqueueGeneration(Runnable generator) {
        QUEUE.add(generator);
    }
    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if (event.side == Side.SERVER && event.phase == Phase.START) {
            if (WorldDataCache.isInitialized() && !QUEUE.isEmpty())
                QUEUE.pop().run();
        }
    }
    
}
