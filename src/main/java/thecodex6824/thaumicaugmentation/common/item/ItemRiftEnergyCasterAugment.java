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

package thecodex6824.thaumicaugmentation.common.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.casters.ICaster;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.augment.Augment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.entity.IDimensionalFracture;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusAPI;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusStorage;
import thecodex6824.thaumicaugmentation.api.util.FocusWrapper;
import thecodex6824.thaumicaugmentation.common.capability.provider.CapabilityProviderAugmentRiftEnergyStorage;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.tile.TileEldritchLock;
import thecodex6824.thaumicaugmentation.common.util.ItemHelper;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRiftEnergyCasterAugment extends ItemTABase {

    public ItemRiftEnergyCasterAugment() {
        super();
        setMaxStackSize(1);
    }
    
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        CapabilityProviderAugmentRiftEnergyStorage storage = new CapabilityProviderAugmentRiftEnergyStorage(new Augment() {
            
            private boolean syncNeeded;
            
            @Override
            public boolean canBeAppliedToItem(ItemStack augmentable) {
                return augmentable.getItem() instanceof ICaster;
            }
            
            @Override
            public boolean isCompatible(ItemStack otherAugment) {
                return stack.getItem() != otherAugment.getItem();
            }
            
            @Override
            public boolean onCastPre(ItemStack caster, FocusWrapper focusPackage, Entity user) {
                IImpetusStorage energy = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                // not actually removing energy is intentional
                if (energy != null && energy.extractEnergy(10, true) == 10)
                    focusPackage.setFocusPower(focusPackage.getFocusPower() * TAConfig.impetusConductorFactor.getValue().floatValue());
                
                return false;
            }
            
            @Override
            public boolean onTick(Entity user) {
                if (!user.getEntityWorld().isRemote && user.getEntityWorld().getTotalWorldTime() % 20 == 0) {
                    IImpetusStorage stackStorage = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                    if (stackStorage != null && stackStorage.canReceive() && stackStorage.getEnergyStored() < stackStorage.getMaxEnergyStored()) {
                        syncNeeded = ImpetusAPI.drainNearbyEnergyIntoStorage(user.getEntityWorld(), stackStorage, 
                                user.getEntityBoundingBox().grow(user.width * 2, user.height, user.width * 2), 
                                user.getPositionVector().add(0, user.height / 2, 0));
                        if (stackStorage.getEnergyStored() == stackStorage.getMaxEnergyStored()) {
                            user.getEntityWorld().playSound(null, user.getPosition(), SoundsTC.runicShieldEffect, SoundCategory.PLAYERS,
                                    0.5F + user.getEntityWorld().rand.nextFloat() / 5.0F, 0.75F + user.getEntityWorld().rand.nextFloat() / 2.0F);
                        }
                    }
                }
                
                return false;
            }
            
            @Override
            public boolean onInteractBlock(Entity user, ItemStack used, BlockPos target, EnumFacing face, EnumHand hand) {
                World world = user.getEntityWorld();
                if (!world.isRemote && world.getBlockState(target).getBlock() == TABlocks.ELDRITCH_LOCK_IMPETUS) {
                    TileEntity tile = world.getTileEntity(target);
                    IImpetusStorage stackStorage = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                    if (tile instanceof TileEldritchLock && ImpetusAPI.tryExtractFully(stackStorage, 30, user)) {
                        ((TileEldritchLock) tile).open();
                        world.playSound(null, target, TASounds.RIFT_ENERGY_ZAP, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        for (int i = 0; i < 4; ++i)
                            ImpetusAPI.createImpetusParticles(world, user.getPositionVector().add(0, user.height / 2, 0), new Vec3d(target));
                    }
                }
                
                return false;
            }
            
            @Override
            public boolean onInteractEntity(Entity user, ItemStack used, Entity target, EnumHand hand) {
                if (!user.getEntityWorld().isRemote && target instanceof IDimensionalFracture) {
                    IDimensionalFracture fracture = (IDimensionalFracture) target;
                    if (!fracture.isOpening() && !fracture.isOpen()) {
                        IImpetusStorage stackStorage = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                        if (ImpetusAPI.tryExtractFully(stackStorage, 15, user)) {
                            fracture.open();
                            target.playSound(TASounds.RIFT_ENERGY_ZAP, 0.5F + target.getEntityWorld().rand.nextFloat() / 5.0F,
                                    0.75F + target.getEntityWorld().rand.nextFloat() / 2.0F);
                            for (int i = 0; i < 3; ++i) {
                                TANetwork.INSTANCE.sendToAllAround(new PacketParticleEffect(ParticleEffect.VOID_STREAKS, 
                                        user.posX, user.posY + user.height / 2, user.posZ, target.posX, target.posY + target.height / 2, target.posZ, 0.08F), 
                                        new TargetPoint(user.getEntityWorld().provider.getDimension(), user.posX, user.posY, user.posZ, 64.0F));
                            }
                            syncNeeded = true;
                        }
                    }
                }
                
                return false;
            }
            
            @Override
            public boolean shouldSync() {
                boolean sync = syncNeeded;
                syncNeeded = false;
                return sync;
            }
            
            @Override
            public boolean hasAdditionalAugmentTooltip() {
                return true;
            }
            
            @Override
            public void appendAdditionalAugmentTooltip(List<String> tooltip) {
                IImpetusStorage energy = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                if (energy != null) {
                    tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.stored_energy",
                            ImpetusAPI.getSuggestedChatColorForDescriptor(energy) + new TextComponentTranslation(
                            ImpetusAPI.getEnergyAmountDescriptor(energy)).getFormattedText()).getFormattedText());
                }
            }
            
        }, new ImpetusStorage(300, 15));
        if (nbt != null && nbt.hasKey("Parent", NBT.TAG_COMPOUND))
            storage.deserializeNBT(nbt.getCompoundTag("Parent"));
        
        return storage;
    }
    
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        NBTTagCompound tag = new NBTTagCompound();
        if (stack.hasTagCompound()) {
            NBTTagCompound item = stack.getTagCompound().copy();
            if (!ThaumicAugmentation.proxy.isSingleplayer() && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
                item.removeTag("cap");

            tag.setTag("item", item);
        }
        
        tag.setTag("cap", new NBTTagCompound());
        NBTTagCompound augment = ItemHelper.tryMakeCapabilityTag(stack, CapabilityAugment.AUGMENT);
        if (augment != null)
            tag.getCompoundTag("cap").setTag("augment", augment);

        NBTTagCompound energy = ItemHelper.tryMakeCapabilityTag(stack, CapabilityImpetusStorage.IMPETUS_STORAGE);
        if (energy != null)
            tag.getCompoundTag("cap").setTag("energy", energy);

        return tag;
    }
    
    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        if (nbt != null) {
            if (nbt.hasKey("cap", NBT.TAG_COMPOUND)) {
                ((Augment) stack.getCapability(CapabilityAugment.AUGMENT, null)).deserializeNBT(nbt.getCompoundTag("cap").getCompoundTag("augment"));
                ((ImpetusStorage) stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null)).deserializeNBT(nbt.getCompoundTag("cap").getCompoundTag("energy"));
            }
            if (nbt.hasKey("item", NBT.TAG_COMPOUND))
                stack.setTagCompound(nbt.getCompoundTag("item"));
            else if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
                nbt.removeTag("cap");
                if (!nbt.isEmpty())
                    stack.setTagCompound(nbt);
            }
            
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && !ThaumicAugmentation.proxy.isSingleplayer()) {
                if (!stack.hasTagCompound())
                    stack.setTagCompound(new NBTTagCompound());
                
                stack.getTagCompound().setTag("cap", nbt.getCompoundTag("cap"));
            }
        }
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == TAItems.CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
            ItemStack empty = new ItemStack(this);
            items.add(empty);
            
            ItemStack full = new ItemStack(this);
            IImpetusStorage energy = full.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
            while (energy.getEnergyStored() < energy.getMaxEnergyStored())
                energy.receiveEnergy(energy.getMaxEnergyStored(), false);
            
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && !ThaumicAugmentation.proxy.isSingleplayer()) {
                if (!full.hasTagCompound())
                    full.setTagCompound(new NBTTagCompound());
                
                full.getTagCompound().setTag("cap", new NBTTagCompound());
                full.getTagCompound().getCompoundTag("cap").setTag("augment", ((Augment) full.getCapability(CapabilityAugment.AUGMENT, null)).serializeNBT());
                full.getTagCompound().getCompoundTag("cap").setTag("energy", ((ImpetusStorage) energy).serializeNBT());
            }
            
            items.add(full);
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        IImpetusStorage energy = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
        if (energy != null) {
            tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.stored_energy",
                    ImpetusAPI.getSuggestedChatColorForDescriptor(energy) + new TextComponentTranslation(
                    ImpetusAPI.getEnergyAmountDescriptor(energy)).getFormattedText()).getFormattedText());
        }
    }
    
}
