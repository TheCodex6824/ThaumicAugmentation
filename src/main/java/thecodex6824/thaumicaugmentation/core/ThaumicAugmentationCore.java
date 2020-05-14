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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSet;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;

@Name("Thaumic Augmentation Core Plugin")
@MCVersion("1.12.2")
@SortingIndex(1005)
@TransformerExclusions("thecodex6824.thaumicaugmentation.core")
public class ThaumicAugmentationCore implements IFMLLoadingPlugin {
    
    private static Logger log = LogManager.getLogger(ThaumicAugmentationAPI.MODID + "core");
    
    private static Configuration config;
    private static boolean enabled;
    private static ImmutableSet<String> excludedTransformers;
    
    public ThaumicAugmentationCore() {
        if (config != null)
            throw new RuntimeException("Coremod loading twice (?)");
        
        config = new Configuration(new File("config", ThaumicAugmentationAPI.MODID + ".cfg"));
        enabled = !config.getBoolean("disableCoremod", "general", false, "");
        excludedTransformers = ImmutableSet.copyOf(config.getStringList("disabledTransformers", "general", new String[0], ""));
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
    
    public static Set<String> getExcludedTransformers() {
        return excludedTransformers;
    }
    
    @Override
    public String getAccessTransformerClass() {
        return null;
    }
    
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {"thecodex6824.thaumicaugmentation.core.TATransformer"};
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
        /* 
         * Note: this seems to have been reverted in 1.8.0_252, keeping this here until
         * the update has existed for a while and distros get around to packaging it.
         * 
         * This is here because GradleStart sets the sys_paths to null.
         * It expects it to be repopulated by the JVM, which was the behavior until java 8 version 242.
         * Starting with version 242 it just asserts that it's not null, and does not repopulate it.
         * This will make any native library loading attempt crash, which for me manifests as LWJGL crashing when loading AWT.
         * See the change in openjdk source here: https://hg.openjdk.java.net/jdk8u/jdk8u/jdk/rev/1d666f78532a
         */
        Object deobf = data.get("runtimeDeobfuscationEnabled");
        if (deobf instanceof Boolean && (Boolean) deobf == false) {
            String[] versions = System.getProperty("java.version").split("_");
            if (versions.length >= 2 && versions[0].equals("1.8.0")) {
                int version = Integer.parseInt(versions[versions.length - 1]);
                if (version >= 242 && version < 252) {
                    log.info("Java version 1.8.0_[242, 252) detected in dev env, working around native loading crash...");
                    try {
                        Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
                        sysPathsField.setAccessible(true);
                        if (sysPathsField.get(null) == null) {
                            Method init = ClassLoader.class.getDeclaredMethod("initLibraryPaths");
                            init.setAccessible(true);
                            init.invoke(null);
                        }
                    }
                    catch(Exception ex) {
                        // just ignore it, this JVM is probably unaffected
                    }
                }
            }
        }
        
        if (enabled)
            ThaumicAugmentationAPI.setCoremodAvailable();
    }
    
}
