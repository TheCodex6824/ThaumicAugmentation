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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.internal.TAInternals;
import thecodex6824.thaumicaugmentation.common.TAConfigHolder;
import thecodex6824.thaumicaugmentation.common.event.WardEventHandler;
import thecodex6824.thaumicaugmentation.common.event.WardEventHandlerNoCoremodFallback;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationHandler;
import thecodex6824.thaumicaugmentation.common.internal.InternalMethodProvider;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.world.WorldDataCache;
import thecodex6824.thaumicaugmentation.common.world.feature.FractureUtils;
import thecodex6824.thaumicaugmentation.init.CapabilityHandler;
import thecodex6824.thaumicaugmentation.init.GUIHandler;
import thecodex6824.thaumicaugmentation.init.LootHandler;
import thecodex6824.thaumicaugmentation.init.MiscHandler;
import thecodex6824.thaumicaugmentation.init.ResearchHandler;
import thecodex6824.thaumicaugmentation.init.WorldHandler;
import thecodex6824.thaumicaugmentation.init.proxy.ISidedProxy;
import thecodex6824.thaumicaugmentation.server.command.TACommands;

@Mod(modid = ThaumicAugmentationAPI.MODID, name = ThaumicAugmentationAPI.NAME, version = ThaumicAugmentation.VERSION, useMetadata = true,
        certificateFingerprint = "@FINGERPRINT@")
public class ThaumicAugmentation {
    
    public static final String VERSION = "@VERSION@";

    @Instance(ThaumicAugmentationAPI.MODID)
    public static ThaumicAugmentation instance;

    private static Logger logger;

    @SidedProxy(serverSide = "thecodex6824.thaumicaugmentation.init.proxy.ServerProxy", clientSide = "thecodex6824.thaumicaugmentation.init.proxy.ClientProxy")
    public static ISidedProxy proxy = null;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        TAConfigHolder.preInit();
        CapabilityHandler.preInit();
        WorldHandler.preInit();
        NetworkRegistry.INSTANCE.registerGuiHandler(ThaumicAugmentation.instance, new GUIHandler());
        TAInternals.setInternalMethodProvider(new InternalMethodProvider());
        MiscHandler.preInit();
        LootHandler.preInit();
        IntegrationHandler.preInit();
        
        if (!TAConfig.disableWardFocus.getValue()) {
            if (ThaumicAugmentationAPI.isCoremodAvailable())
                MinecraftForge.EVENT_BUS.register(new WardEventHandler());
            else
                MinecraftForge.EVENT_BUS.register(new WardEventHandlerNoCoremodFallback());
        }
        
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        TANetwork.init();
        WorldHandler.init();
        ResearchHandler.init();
        MiscHandler.init();
        IntegrationHandler.init();
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        WorldHandler.postInit();
        IntegrationHandler.postInit();
        proxy.postInit();
    }
    
    @EventHandler
    public static void onServerStarting(FMLServerStartingEvent event) {
        for (WorldServer world : event.getServer().worlds)
            WorldDataCache.addOrUpdateData(world);
        
        FractureUtils.initDimensionCache();
        WorldDataCache.setInitialized();
        TACommands.registerCommands(event);
    }
    
    @EventHandler
    public static void onFingerPrintViolation(FMLFingerprintViolationEvent event) {
        if (!event.isDirectory()) {
            Logger tempLogger = LogManager.getLogger(ThaumicAugmentationAPI.MODID);
            tempLogger.warn("A file failed to match with the signing key.");
            tempLogger.warn("If you *know* this is a homebrew/custom build then this is expected, carry on.");
            tempLogger.warn("Otherwise, you might want to redownload this mod from the *official* CurseForge page.");
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
