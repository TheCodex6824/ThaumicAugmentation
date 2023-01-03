/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.builder.IElytraHarnessAugment;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusAPI;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusStorage;
import thecodex6824.thaumicaugmentation.client.event.ClientEventHandler;
import thecodex6824.thaumicaugmentation.client.renderer.AugmentRenderer;
import thecodex6824.thaumicaugmentation.common.capability.provider.CapabilityProviderElytraHarnessAugment;
import thecodex6824.thaumicaugmentation.common.capability.provider.SimpleCapabilityProviderNoSave;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.util.ItemHelper;

import javax.annotation.Nullable;
import java.util.List;

public class ItemElytraHarnessAugment extends ItemTABase {

    protected static abstract class HarnessAugment implements IElytraHarnessAugment {
        
        protected boolean sync;
        
        @Override
        public boolean canBeAppliedToItem(ItemStack augmentable) {
            return augmentable.getItem() == TAItems.ELYTRA_HARNESS;
        }
        
        public void setSyncNeeded() {
            sync = true;
        }
        
        @Override
        public boolean shouldSync() {
            boolean res = sync;
            sync = false;
            return res;
        }
    }
    
    public ItemElytraHarnessAugment() {
        super("impetus_booster");
        setMaxStackSize(1);
        setHasSubtypes(true);
    }
    
    protected IElytraHarnessAugment createAugmentForStack(ItemStack stack) {
        if (stack.getMetadata() == 0) {
            return new HarnessAugment() {
                
                @Override
                public boolean isCosmetic() {
                    return false;
                }
                
                @Override
                public boolean onTick(Entity user) {
                    if (user.world.isRemote && ThaumicAugmentation.proxy.isEntityClientPlayer(user)) {
                        if (ClientEventHandler.isBoosting((EntityPlayer) user)) {
                            Vec3d vec3d = user.getLookVec();
                            user.motionX += vec3d.x * 0.1 + (vec3d.x * 1.5 - user.motionX) * 0.5;
                            user.motionY += vec3d.y * 0.1 + (vec3d.y * 1.5 - user.motionY) * 0.5;
                            user.motionZ += vec3d.z * 0.1 + (vec3d.z * 1.5 - user.motionZ) * 0.5;
                        }
                    }
                    
                    return super.onTick(user);
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
                
                @Override
                @SideOnly(Side.CLIENT)
                public void render(ItemStack stack, RenderPlayer renderer, ModelBiped base, EntityPlayer player, float limbSwing, 
                        float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
                    
                    AugmentRenderer.renderElytraBooster(renderer, base, player, limbSwing, limbSwingAmount,
                            partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
                }
                
            };
        }
        else {
            return new HarnessAugment() {
                
                @Override
                public boolean isCosmetic() {
                    return false;
                }
                
            };
        }
    }
    
    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        if (stack.getMetadata() == 0) {
            CapabilityProviderElytraHarnessAugment provider = new CapabilityProviderElytraHarnessAugment(
                    createAugmentForStack(stack), new ImpetusStorage(1500, 75, 1, 0) {
                        @Override
                        public long extractEnergy(long maxToExtract, boolean simulate) {
                            long result = super.extractEnergy(maxToExtract, simulate);
                            if (!simulate)
                                ((HarnessAugment) stack.getCapability(CapabilityAugment.AUGMENT, null)).setSyncNeeded();
                        
                            return result;
                        }
                    }
            );
            if (nbt != null && nbt.hasKey("Parent", NBT.TAG_COMPOUND))
                provider.deserializeNBT(nbt.getCompoundTag("Parent"));
            
            return provider;
        }
        else {
            SimpleCapabilityProviderNoSave<IAugment> provider =
                    new SimpleCapabilityProviderNoSave<>(createAugmentForStack(stack), CapabilityAugment.AUGMENT);
            if (nbt != null && nbt.hasKey("Parent", NBT.TAG_COMPOUND))
                provider.deserializeNBT(nbt.getCompoundTag("Parent"));
            
            return provider;
        }
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

        NBTTagCompound cap = ItemHelper.tryMakeCapabilityTag(stack, CapabilityImpetusStorage.IMPETUS_STORAGE);
        if (cap != null)
            tag.setTag("cap", cap);

        return tag;
    }
    
    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        if (nbt != null) {
            if (nbt.hasKey("cap", NBT.TAG_COMPOUND))
                ((ImpetusStorage) stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null)).deserializeNBT(nbt.getCompoundTag("cap"));
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
