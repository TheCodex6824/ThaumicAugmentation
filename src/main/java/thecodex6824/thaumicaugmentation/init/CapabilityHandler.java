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

package thecodex6824.thaumicaugmentation.init;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.energy.CapabilityRiftEnergyStorage;
import thecodex6824.thaumicaugmentation.api.warded.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.WardStorageClient;
import thecodex6824.thaumicaugmentation.api.warded.WardStorageServer;
import thecodex6824.thaumicaugmentation.api.world.capability.CapabilityFractureLocations;
import thecodex6824.thaumicaugmentation.api.world.capability.FractureLocations;
import thecodex6824.thaumicaugmentation.common.capability.RiftEnergyStorageFluxRiftImpl;
import thecodex6824.thaumicaugmentation.common.capability.SimpleCapabilityProvider;
import thecodex6824.thaumicaugmentation.common.capability.init.CapabilityAugmentInit;
import thecodex6824.thaumicaugmentation.common.capability.init.CapabilityAugmentableItemInit;
import thecodex6824.thaumicaugmentation.common.capability.init.CapabilityFractureLocationInit;
import thecodex6824.thaumicaugmentation.common.capability.init.CapabilityRiftEnergyStorageInit;
import thecodex6824.thaumicaugmentation.common.capability.init.CapabilityWardStorageInit;
import thecodex6824.thaumicaugmentation.common.capability.init.CapabilityWardedInventoryInit;
import thecodex6824.thaumicaugmentation.common.capability.init.CapabilityWardedTileInit;
import thecodex6824.thaumicaugmentation.common.world.feature.FractureUtils;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class CapabilityHandler {

    private CapabilityHandler() {}
    
    public static void preInit() {
        CapabilityAugmentInit.init();
        CapabilityAugmentableItemInit.init();
        CapabilityRiftEnergyStorageInit.init();
        CapabilityWardedInventoryInit.init();
        CapabilityFractureLocationInit.init();
        CapabilityWardStorageInit.init();
        CapabilityWardedTileInit.init();
    }
    
    @SubscribeEvent
    public static void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityFluxRift) {
            event.addCapability(new ResourceLocation(ThaumicAugmentationAPI.MODID, "rift_energy_storage"), new SimpleCapabilityProvider<>(
                    new RiftEnergyStorageFluxRiftImpl((EntityFluxRift) event.getObject()), CapabilityRiftEnergyStorage.RIFT_ENERGY_STORAGE));
        }
    }
    
    @SubscribeEvent
    public static void onAttachCapabilitiesChunk(AttachCapabilitiesEvent<Chunk> event) {
        if (!TAConfig.disableEmptiness.getValue() && FractureUtils.isDimAllowedForLinking(event.getObject().getWorld().provider.getDimension())) {
            event.addCapability(new ResourceLocation(ThaumicAugmentationAPI.MODID, "fracture_location"), new SimpleCapabilityProvider<>(
                    new FractureLocations(event.getObject()), CapabilityFractureLocations.FRACTURE_LOCATIONS));
        }
        
        if (!TAConfig.disableWardFocus.getValue()) {
            event.addCapability(new ResourceLocation(ThaumicAugmentationAPI.MODID, "ward_storage"), new SimpleCapabilityProvider<>(
                    event.getObject().getWorld().isRemote ? new WardStorageClient() : new WardStorageServer(), CapabilityWardStorage.WARD_STORAGE));
        }
    }
    
}
