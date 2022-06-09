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

package thecodex6824.thaumicaugmentation.common.integration;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;

public class IntegrationHandler {

    public static final String JEID_MOD_ID = "jeid";
    public static final String BOTANIA_MOD_ID = "botania";
    public static final String AURACONTROL_MOD_ID = "auracontrol";
    public static final String EB_WIZARDRY_MOD_ID = "ebwizardry";
    public static final ComparableVersion EB_WIZARDRY_MIN_VERSION = new ComparableVersion("4.2.0");
    
    private static HashMap<String, IIntegrationHolder> integrations = new HashMap<>();
    
    public static void preInit() {
        if (Loader.isModLoaded(JEID_MOD_ID))
            integrations.put(JEID_MOD_ID, new IntegrationJEID());
        if (Loader.isModLoaded(BOTANIA_MOD_ID))
            integrations.put(BOTANIA_MOD_ID, new IntegrationBotania());
        if (Loader.isModLoaded(AURACONTROL_MOD_ID))
            integrations.put(AURACONTROL_MOD_ID, new IntegrationAuraControl());
        if (Loader.isModLoaded(EB_WIZARDRY_MOD_ID)) {
            // Older versions of EB Wizardry do not have some things we want
            String version = Loader.instance().getIndexedModList().get(EB_WIZARDRY_MOD_ID).getVersion();
            if (new ComparableVersion(version).compareTo(EB_WIZARDRY_MIN_VERSION) >= 0)
                integrations.put(EB_WIZARDRY_MOD_ID, new IntegrationEBWizardry());
        }
        
        for (IIntegrationHolder holder : integrations.values()) {
            if (holder.registerEventBus())
                MinecraftForge.EVENT_BUS.register(holder);
            
            holder.preInit();
        }
    }
    
    public static void init() {
        for (IIntegrationHolder holder : integrations.values())
            holder.init();
    }
    
    public static void postInit() {
        ThaumicAugmentation.getLogger().info("The following mods were detected and have integration enabled:");
        for (Map.Entry<String, IIntegrationHolder> entry : integrations.entrySet()) {
            entry.getValue().postInit();
            ThaumicAugmentation.getLogger().info(entry.getKey());
        }
    }
    
    public static boolean isIntegrationPresent(String modid) {
        return integrations.containsKey(modid);
    }
    
    @Nullable
    public static IIntegrationHolder getIntegration(String modid) {
        return integrations.get(modid);
    }
    
}
