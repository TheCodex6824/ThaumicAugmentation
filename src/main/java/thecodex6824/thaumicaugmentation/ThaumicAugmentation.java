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

package thecodex6824.thaumicaugmentation;

import org.apache.logging.log4j.Logger;

import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.world.WorldDataCache;
import thecodex6824.thaumicaugmentation.common.world.feature.FractureUtils;
import thecodex6824.thaumicaugmentation.init.proxy.ISidedProxy;

@Mod(modid = ThaumicAugmentationAPI.MODID, name = ThaumicAugmentationAPI.NAME, version = ThaumicAugmentation.VERSION, useMetadata = true)
public class ThaumicAugmentation {
    
    public static final String VERSION = "@VERSION@";

    @Instance(ThaumicAugmentationAPI.MODID)
    public static ThaumicAugmentation instance;

    private static Logger logger;

    @SidedProxy(serverSide = "thecodex6824.thaumicaugmentation.init.proxy.CommonProxy", clientSide = "thecodex6824.thaumicaugmentation.init.proxy.ClientProxy")
    public static ISidedProxy proxy = null;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        TANetwork.init();
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }
    
    @EventHandler
    public static void onServerStarting(FMLServerStartingEvent event) {
        for (WorldServer world : event.getServer().worlds)
            WorldDataCache.addOrUpdateData(world);
        
        FractureUtils.initDimensionCache();
        WorldDataCache.setInitialized();
    }

    public static Logger getLogger() {
        return logger;
    }
}
