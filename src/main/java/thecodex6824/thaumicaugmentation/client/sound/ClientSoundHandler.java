/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

package thecodex6824.thaumicaugmentation.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraftforge.client.EnumHelperClient;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.client.event.IClientResourceReloadDispatcher;
import thecodex6824.thaumicaugmentation.common.util.IResourceReloadDispatcher;

@EventBusSubscriber(value = Side.CLIENT)
public class ClientSoundHandler {

    public static MusicType EMPTINESS_MUSIC_NOOP;
  
    private static EmptinessSoundTicker emptinessSound = null;
    
    public static void init() {
        if (!TAConfig.disableEmptiness.getValue()) {
            EMPTINESS_MUSIC_NOOP = EnumHelperClient.addMusicType(ThaumicAugmentationAPI.MODID + ":emptiness_music_noop", 
                    TASounds.EMPTINESS_AMBIENCE, Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
        }
    }
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (!TAConfig.disableEmptiness.getValue() && event.phase == Phase.START) {
            if (emptinessSound == null) {
                emptinessSound = new EmptinessSoundTicker(Minecraft.getMinecraft());
                IResourceReloadDispatcher dispatcher = ThaumicAugmentation.proxy.getResourceReloadDispatcher();
                if (dispatcher instanceof IClientResourceReloadDispatcher)
                    ((IClientResourceReloadDispatcher) dispatcher).registerListener(emptinessSound);
            }
            
            emptinessSound.update();
        }
    }
    
}
