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

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.common.blocks.basic.BlockBannerTCItem;
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.Augment;
import thecodex6824.thaumicaugmentation.api.augment.AugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.builder.IElytraHarnessAugment;
import thecodex6824.thaumicaugmentation.api.entity.CapabilityPortalState;
import thecodex6824.thaumicaugmentation.api.entity.IPortalState;
import thecodex6824.thaumicaugmentation.api.entity.PortalState;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.FluxRiftImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.ImpetusNode;
import thecodex6824.thaumicaugmentation.api.item.BiomeSelector;
import thecodex6824.thaumicaugmentation.api.item.IBiomeSelector;
import thecodex6824.thaumicaugmentation.api.item.IImpetusLinker;
import thecodex6824.thaumicaugmentation.api.item.IMorphicTool;
import thecodex6824.thaumicaugmentation.api.item.ImpetusLinker;
import thecodex6824.thaumicaugmentation.api.tile.IRiftJar;
import thecodex6824.thaumicaugmentation.api.tile.RiftJar;
import thecodex6824.thaumicaugmentation.api.warded.entity.IWardOwnerProvider;
import thecodex6824.thaumicaugmentation.api.warded.storage.CapabilityWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.storage.IWardStorage;
import thecodex6824.thaumicaugmentation.api.warded.storage.WardStorageClient;
import thecodex6824.thaumicaugmentation.api.warded.storage.WardStorageServer;
import thecodex6824.thaumicaugmentation.api.warded.tile.IWardedInventory;
import thecodex6824.thaumicaugmentation.api.warded.tile.IWardedTile;
import thecodex6824.thaumicaugmentation.api.warded.tile.WardedInventory;
import thecodex6824.thaumicaugmentation.api.warded.tile.WardedTile;
import thecodex6824.thaumicaugmentation.api.world.capability.CapabilityFractureLocations;
import thecodex6824.thaumicaugmentation.api.world.capability.FractureLocations;
import thecodex6824.thaumicaugmentation.api.world.capability.IFractureLocations;
import thecodex6824.thaumicaugmentation.client.renderer.AugmentRenderer;
import thecodex6824.thaumicaugmentation.common.capability.MorphicTool;
import thecodex6824.thaumicaugmentation.common.capability.SimpleCapabilityProvider;
import thecodex6824.thaumicaugmentation.common.capability.SimpleCapabilityProviderNoSave;
import thecodex6824.thaumicaugmentation.common.world.feature.FractureUtils;

@EventBusSubscriber(modid = ThaumicAugmentationAPI.MODID)
public final class CapabilityHandler {

    private CapabilityHandler() {}
    
    public static void preInit() {
        CapabilityManager.INSTANCE.register(IAugment.class, new IStorage<IAugment>() {
            
            @Override
            public void readNBT(Capability<IAugment> capability, IAugment instance, EnumFacing side, NBTBase nbt) {
                if (!(instance instanceof Augment) || !(nbt instanceof NBTTagCompound))
                    throw new UnsupportedOperationException("Can't serialize non-API implementation");
                
                ((Augment) instance).deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            @Nullable
            public NBTBase writeNBT(Capability<IAugment> capability, IAugment instance, EnumFacing side) {
                if (!(instance instanceof Augment))
                    throw new UnsupportedOperationException("Can't deserialize non-API implementation");
                
                return ((Augment) instance).serializeNBT();
            }
            
        }, Augment::new);
        
        CapabilityManager.INSTANCE.register(IAugmentableItem.class, new IStorage<IAugmentableItem>() {
            
            @Override
            public void readNBT(Capability<IAugmentableItem> capability, IAugmentableItem instance, EnumFacing side, NBTBase nbt) {
                if (!(instance instanceof AugmentableItem) || !(nbt instanceof NBTTagCompound))
                    throw new UnsupportedOperationException("Can't serialize non-API implementation");
                
                ((AugmentableItem) instance).deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            @Nullable
            public NBTBase writeNBT(Capability<IAugmentableItem> capability, IAugmentableItem instance, EnumFacing side) {
                if (!(instance instanceof AugmentableItem))
                    throw new UnsupportedOperationException("Can't deserialize non-API implementation");
                
                return ((AugmentableItem) instance).serializeNBT();
            }
            
        }, () -> new AugmentableItem(3));
        
        CapabilityManager.INSTANCE.register(IImpetusStorage.class, new IStorage<IImpetusStorage>() {
            
            @Override
            public void readNBT(Capability<IImpetusStorage> capability, IImpetusStorage instance, EnumFacing side, NBTBase nbt) {
                if ((!(instance instanceof ImpetusStorage) && !(instance instanceof FluxRiftImpetusStorage)) || !(nbt instanceof NBTTagCompound))
                    throw new UnsupportedOperationException("Can't serialize non-API implementation");
                
                if (instance instanceof ImpetusStorage)
                    ((ImpetusStorage) instance).deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            @Nullable
            public NBTBase writeNBT(Capability<IImpetusStorage> capability, IImpetusStorage instance, EnumFacing side) {
                if (!(instance instanceof ImpetusStorage) && !(instance instanceof FluxRiftImpetusStorage))
                    throw new UnsupportedOperationException("Can't deserialize non-API implementation");
                
                return instance instanceof ImpetusStorage ? ((ImpetusStorage) instance).serializeNBT() :
                    new NBTTagCompound();
            }
            
        }, () -> new ImpetusStorage(1000));
        
        CapabilityManager.INSTANCE.register(IWardedInventory.class, new IStorage<IWardedInventory>() {
            
            @Override
            public void readNBT(Capability<IWardedInventory> capability, IWardedInventory instance, EnumFacing side, NBTBase nbt) {
                if (!(instance instanceof WardedInventory) || !(nbt instanceof NBTTagCompound))
                    throw new UnsupportedOperationException("Can't serialize non-API implementation");
                
                ((WardedInventory) instance).deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            @Nullable
            public NBTBase writeNBT(Capability<IWardedInventory> capability, IWardedInventory instance, EnumFacing side) {
                if (!(instance instanceof WardedInventory))
                    throw new UnsupportedOperationException("Can't deserialize non-API implementation");
                
                return ((WardedInventory) instance).serializeNBT();
            }
            
        }, () -> new WardedInventory(3));
        
        CapabilityManager.INSTANCE.register(IFractureLocations.class, new IStorage<IFractureLocations>() {
            
            @Override
            public void readNBT(Capability<IFractureLocations> capability, IFractureLocations instance, EnumFacing side, NBTBase nbt) {
                if (!(instance instanceof FractureLocations) || !(nbt instanceof NBTTagCompound))
                    throw new UnsupportedOperationException("Can't serialize non-API implementation");
                
                ((FractureLocations) instance).deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            @Nullable
            public NBTBase writeNBT(Capability<IFractureLocations> capability, IFractureLocations instance, EnumFacing side) {
                if (!(instance instanceof FractureLocations))
                    throw new UnsupportedOperationException("Can't deserialize non-API implementation");
                
                return ((FractureLocations) instance).serializeNBT();
            }
            
        }, FractureLocations::new);
        
        CapabilityManager.INSTANCE.register(IWardStorage.class, new IStorage<IWardStorage>() {
            
            @Override
            public void readNBT(Capability<IWardStorage> capability, IWardStorage instance, EnumFacing side, NBTBase nbt) {
                if (!(instance instanceof WardStorageServer) || !(nbt instanceof NBTTagCompound))
                    throw new UnsupportedOperationException("Can't serialize non-API implementation (or non-server implementations)");
                
                ((WardStorageServer) instance).deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            @Nullable
            public NBTBase writeNBT(Capability<IWardStorage> capability, IWardStorage instance, EnumFacing side) {
                if (!(instance instanceof WardStorageServer))
                    throw new UnsupportedOperationException("Can't deserialize non-API implementation (or non-server implementations)");
                
                return ((WardStorageServer) instance).serializeNBT();
            }
            
        }, () -> { throw new UnsupportedOperationException("Cannot create a default ward storage impl (create one for client or server side instead)"); });
        
        CapabilityManager.INSTANCE.register(IWardedTile.class, new IStorage<IWardedTile>() {
            
            @Override
            public void readNBT(Capability<IWardedTile> capability, IWardedTile instance, EnumFacing side, NBTBase nbt) {
                if (!(instance instanceof WardedTile) || !(nbt instanceof NBTTagCompound))
                    throw new UnsupportedOperationException("Can't serialize non-API implementation");
                
                ((WardedTile) instance).deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            @Nullable
            public NBTBase writeNBT(Capability<IWardedTile> capability, IWardedTile instance, EnumFacing side) {
                if (!(instance instanceof WardedTile))
                    throw new UnsupportedOperationException("Can't deserialize non-API implementation");
                
                return ((WardedTile) instance).serializeNBT();
            }
            
        }, () -> new WardedTile(null));
        
        CapabilityManager.INSTANCE.register(IPortalState.class, new IStorage<IPortalState>() {
            
            @Override
            public void readNBT(Capability<IPortalState> capability, IPortalState instance, EnumFacing side, NBTBase nbt) {
                if (!(instance instanceof PortalState) || !(nbt instanceof NBTTagCompound))
                    throw new UnsupportedOperationException("Can't serialize non-API implementation");
                
                ((PortalState) instance).deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            @Nullable
            public NBTBase writeNBT(Capability<IPortalState> capability, IPortalState instance, EnumFacing side) {
                if (!(instance instanceof PortalState))
                    throw new UnsupportedOperationException("Can't deserialize non-API implementation");
                
                return ((PortalState) instance).serializeNBT();
            }
            
        }, PortalState::new);
        
        CapabilityManager.INSTANCE.register(IMorphicTool.class, new IStorage<IMorphicTool>() {
            
            @Override
            public void readNBT(Capability<IMorphicTool> capability, IMorphicTool instance, EnumFacing side, NBTBase nbt) {
                if (!(instance instanceof MorphicTool) || !(nbt instanceof NBTTagCompound))
                    throw new UnsupportedOperationException("Can't serialize non-API implementation");
                
                ((MorphicTool) instance).deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            @Nullable
            public NBTBase writeNBT(Capability<IMorphicTool> capability, IMorphicTool instance, EnumFacing side) {
                if (!(instance instanceof MorphicTool))
                    throw new UnsupportedOperationException("Can't deserialize non-API implementation");
                
                return ((MorphicTool) instance).serializeNBT();
            }
            
        }, MorphicTool::new);
        
        CapabilityManager.INSTANCE.register(IImpetusNode.class, new IStorage<IImpetusNode>() {
            
            @Override
            public void readNBT(Capability<IImpetusNode> capability, IImpetusNode instance, EnumFacing side, NBTBase nbt) {
                if (!(instance instanceof ImpetusNode) || !(nbt instanceof NBTTagCompound))
                    throw new UnsupportedOperationException("Can't serialize non-API implementation");
                
                ((WardedTile) instance).deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            @Nullable
            public NBTBase writeNBT(Capability<IImpetusNode> capability, IImpetusNode instance, EnumFacing side) {
                if (!(instance instanceof ImpetusNode))
                    throw new UnsupportedOperationException("Can't deserialize non-API implementation");
                
                return ((ImpetusNode) instance).serializeNBT();
            }
            
        }, () -> new ImpetusNode(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        CapabilityManager.INSTANCE.register(IRiftJar.class, new IStorage<IRiftJar>() {
            
            @Override
            public void readNBT(Capability<IRiftJar> capability, IRiftJar instance, EnumFacing side, NBTBase nbt) {
                if (!(instance instanceof RiftJar) || !(nbt instanceof NBTTagCompound))
                    throw new UnsupportedOperationException("Can't serialize non-API implementation");
                
                ((RiftJar) instance).deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            @Nullable
            public NBTBase writeNBT(Capability<IRiftJar> capability, IRiftJar instance, EnumFacing side) {
                if (!(instance instanceof RiftJar))
                    throw new UnsupportedOperationException("Can't deserialize non-API implementation");
                
                return ((RiftJar) instance).serializeNBT();
            }
            
        }, RiftJar::new);
        
        CapabilityManager.INSTANCE.register(IBiomeSelector.class, new IStorage<IBiomeSelector>() {
            
            @Override
            public void readNBT(Capability<IBiomeSelector> capability, IBiomeSelector instance, EnumFacing side, NBTBase nbt) {
                if (!(instance instanceof BiomeSelector) || !(nbt instanceof NBTTagCompound))
                    throw new UnsupportedOperationException("Can't serialize non-API implementation");
                
                ((BiomeSelector) instance).deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            @Nullable
            public NBTBase writeNBT(Capability<IBiomeSelector> capability, IBiomeSelector instance, EnumFacing side) {
                if (!(instance instanceof BiomeSelector))
                    throw new UnsupportedOperationException("Can't deserialize non-API implementation");
                
                return ((BiomeSelector) instance).serializeNBT();
            }
            
        }, BiomeSelector::new);
        
        CapabilityManager.INSTANCE.register(IImpetusLinker.class, new IStorage<IImpetusLinker>() {
            
            @Override
            public void readNBT(Capability<IImpetusLinker> capability, IImpetusLinker instance, EnumFacing side, NBTBase nbt) {
                if (!(instance instanceof ImpetusLinker) || !(nbt instanceof NBTTagCompound))
                    throw new UnsupportedOperationException("Can't serialize non-API implementation");
                
                ((ImpetusLinker) instance).deserializeNBT((NBTTagCompound) nbt);
            }
            
            @Override
            @Nullable
            public NBTBase writeNBT(Capability<IImpetusLinker> capability, IImpetusLinker instance, EnumFacing side) {
                if (!(instance instanceof ImpetusLinker))
                    throw new UnsupportedOperationException("Can't deserialize non-API implementation");
                
                return ((ImpetusLinker) instance).serializeNBT();
            }
            
        }, ImpetusLinker::new);
        
        CapabilityManager.INSTANCE.register(IWardOwnerProvider.class, new IStorage<IWardOwnerProvider>() {
            
            @Override
            public void readNBT(Capability<IWardOwnerProvider> capability, IWardOwnerProvider instance, EnumFacing side,
                    NBTBase nbt) {}
            
            @Override
            @Nullable
            public NBTBase writeNBT(Capability<IWardOwnerProvider> capability, IWardOwnerProvider instance,
                    EnumFacing side) {
                return null;
            }
            
        }, () -> new IWardOwnerProvider() {
            @Override
            @Nullable
            public UUID getWardOwnerUUID() {
                return null;
            }
        });
    }
    
    @SubscribeEvent
    public static void onAttachCapabilitiesItemStack(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() == Items.BANNER || event.getObject().getItem() == ItemBlock.getItemFromBlock(BlocksTC.bannerCrimsonCult) || event.getObject().getItem() instanceof BlockBannerTCItem) {
            event.addCapability(new ResourceLocation(ThaumicAugmentationAPI.MODID, "elytra_harness_augment"), new SimpleCapabilityProviderNoSave<IAugment>(new IElytraHarnessAugment() {
                @Override
                public boolean isCosmetic() {
                    return true;
                }
                
                @Override
                @SideOnly(Side.CLIENT)
                public void render(ItemStack stack, RenderPlayer renderer, ModelBiped base, EntityPlayer player, float limbSwing,
                        float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
                        float scale) {
                    
                    AugmentRenderer.renderBanner(stack, renderer, base, player, limbSwing, limbSwingAmount,
                            partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
                }
                
                @Override
                @SideOnly(Side.CLIENT)
                public void renderFlightParticles(ItemStack cosmetic, RenderPlayer renderer, ModelBiped base, EntityPlayer player,
                        float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
                        float headPitch, float scale) {
                    
                    AugmentRenderer.renderBannerParticle(cosmetic, renderer, base, player, limbSwing, limbSwingAmount,
                            partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
                }
                
            }, CapabilityAugment.AUGMENT));
        }
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
