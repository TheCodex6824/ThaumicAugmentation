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
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.Augment;
import thecodex6824.thaumicaugmentation.api.augment.AugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.entity.CapabilityPortalState;
import thecodex6824.thaumicaugmentation.api.entity.IPortalState;
import thecodex6824.thaumicaugmentation.api.entity.PortalState;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.FluxRiftImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.ImpetusNode;
import thecodex6824.thaumicaugmentation.api.item.IMorphicTool;
import thecodex6824.thaumicaugmentation.api.tile.IRiftJar;
import thecodex6824.thaumicaugmentation.api.tile.RiftJar;
import thecodex6824.thaumicaugmentation.api.warded.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.IWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.IWardStorageServer;
import thecodex6824.thaumicaugmentation.api.warded.IWardedInventory;
import thecodex6824.thaumicaugmentation.api.warded.IWardedTile;
import thecodex6824.thaumicaugmentation.api.warded.WardStorageClient;
import thecodex6824.thaumicaugmentation.api.warded.WardStorageServer;
import thecodex6824.thaumicaugmentation.api.warded.WardedInventory;
import thecodex6824.thaumicaugmentation.api.warded.WardedTile;
import thecodex6824.thaumicaugmentation.api.world.capability.CapabilityFractureLocations;
import thecodex6824.thaumicaugmentation.api.world.capability.FractureLocations;
import thecodex6824.thaumicaugmentation.api.world.capability.IFractureLocations;
import thecodex6824.thaumicaugmentation.common.capability.MorphicTool;
import thecodex6824.thaumicaugmentation.common.capability.SimpleCapabilityProvider;
import thecodex6824.thaumicaugmentation.common.world.feature.FractureUtils;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class CapabilityHandler {

    private CapabilityHandler() {}
    
    private static class DefaultStorage<C extends INBTSerializable<NBTTagCompound>> implements IStorage<C> {
        
        @Override
        public void readNBT(Capability<C> capability, C instance, EnumFacing side, NBTBase nbt) {
            instance.deserializeNBT((NBTTagCompound) nbt);
        }
        
        @Override
        public NBTBase writeNBT(Capability<C> capability, C instance, EnumFacing side) {
            return instance.serializeNBT();
        }
        
    }
    
    public static void preInit() {
        CapabilityManager.INSTANCE.register(IAugment.class, new DefaultStorage<>(), Augment::new);
        CapabilityManager.INSTANCE.register(IAugmentableItem.class, new DefaultStorage<>(), () -> new AugmentableItem(3));
        CapabilityManager.INSTANCE.register(IImpetusStorage.class, new DefaultStorage<>(), () -> new ImpetusStorage(1000));
        CapabilityManager.INSTANCE.register(IWardedInventory.class, new DefaultStorage<>(), () -> new WardedInventory(3));
        CapabilityManager.INSTANCE.register(IFractureLocations.class, new DefaultStorage<>(), FractureLocations::new);
        CapabilityManager.INSTANCE.register(IWardStorage.class, new IStorage<IWardStorage>() {
            
            @Override
            public void readNBT(Capability<IWardStorage> capability, IWardStorage instance, EnumFacing side,
                    NBTBase nbt) {
                
                if (instance instanceof IWardStorageServer)
                    ((IWardStorageServer) instance).deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            public NBTBase writeNBT(Capability<IWardStorage> capability, IWardStorage instance, EnumFacing side) {
                if (instance instanceof IWardStorageServer)
                    return ((IWardStorageServer) instance).serializeNBT();
                else
                    return new NBTTagCompound();
            }
            
        }, () -> { throw new UnsupportedOperationException("Cannot create a default ward storage impl (create one for client or server side instead)"); });
        
        CapabilityManager.INSTANCE.register(IWardedTile.class, new DefaultStorage<>(), () -> new WardedTile(null));
        CapabilityManager.INSTANCE.register(IPortalState.class, new DefaultStorage<>(), PortalState::new);
        CapabilityManager.INSTANCE.register(IMorphicTool.class, new DefaultStorage<>(), MorphicTool::new);
        CapabilityManager.INSTANCE.register(IImpetusNode.class, new DefaultStorage<>(), () -> new ImpetusNode(Integer.MAX_VALUE, Integer.MAX_VALUE));
        CapabilityManager.INSTANCE.register(IRiftJar.class, new DefaultStorage<>(), RiftJar::new);
    }
    
    @SubscribeEvent
    public static void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityFluxRift) {
            event.addCapability(new ResourceLocation(ThaumicAugmentationAPI.MODID, "rift_energy_storage"), new SimpleCapabilityProvider<>(
                    new FluxRiftImpetusStorage((EntityFluxRift) event.getObject()), CapabilityImpetusStorage.IMPETUS_STORAGE));
        }
        
        event.addCapability(new ResourceLocation(ThaumicAugmentationAPI.MODID, "portal_state"), new SimpleCapabilityProvider<>(
                new PortalState(), CapabilityPortalState.PORTAL_STATE));
    }
    
    @SubscribeEvent
    public static void onAttachCapabilitiesChunk(AttachCapabilitiesEvent<Chunk> event) {
        if (!TAConfig.disableEmptiness.getValue() && event.getObject().getWorld() != null && FractureUtils.isDimAllowedForLinking(event.getObject().getWorld().provider.getDimension())) {
            event.addCapability(new ResourceLocation(ThaumicAugmentationAPI.MODID, "fracture_location"), new SimpleCapabilityProvider<>(
                    new FractureLocations(event.getObject()), CapabilityFractureLocations.FRACTURE_LOCATIONS));
        }
        
        if (!TAConfig.disableWardFocus.getValue() && event.getObject().getWorld() != null) {
            event.addCapability(new ResourceLocation(ThaumicAugmentationAPI.MODID, "ward_storage"), new SimpleCapabilityProvider<>(
                    event.getObject().getWorld().isRemote ? new WardStorageClient() : new WardStorageServer(), CapabilityWardStorage.WARD_STORAGE));
        }
    }
    
}
