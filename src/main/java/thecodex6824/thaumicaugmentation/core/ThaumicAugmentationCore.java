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

package thecodex6824.thaumicaugmentation.core;

import java.io.File;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;

@IFMLLoadingPlugin.Name(ThaumicAugmentationAPI.MODID + "core")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1005)
@IFMLLoadingPlugin.TransformerExclusions("thecodex6824.thaumicaugmentation.core")
public class ThaumicAugmentationCore implements IFMLLoadingPlugin {
    
    private static Logger log = LogManager.getLogger(ThaumicAugmentationAPI.MODID + "core");
    
    private static Configuration config;
    private static boolean enabled;
    
    public ThaumicAugmentationCore() {
        if (config != null)
            throw new RuntimeException("Coremod loading twice (?)");
        
        config = new Configuration(new File("config", ThaumicAugmentationAPI.MODID + ".cfg"));
    }
    
    public static Logger getLogger() {
        return log;
    }
    
    public static boolean isEnabled() {
        return enabled;
    }
    
    public static Configuration getConfig() {
        return config;
    }
    
    @Override
    public String getAccessTransformerClass() {
        return "thecodex6824.thaumicaugmentation.core.TATransformer";
    }
    
    @Override
    public String[] getASMTransformerClass() {
        return null;
    }
    
    @Override
    public String getModContainerClass() {
        return null;
    }
    
    @Override
    public String getSetupClass() {
        return null;
    }
    
    @Override
    public void injectData(Map<String, Object> data) {
        enabled = !config.getBoolean("DisableCoremod", "general", false, "");
        if (enabled)
            ThaumicAugmentationAPI.setCoremodAvailable();
    }
    
}
