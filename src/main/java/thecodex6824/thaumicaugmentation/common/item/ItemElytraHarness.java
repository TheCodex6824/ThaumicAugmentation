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

package thecodex6824.thaumicaugmentation.common.item;

import javax.annotation.Nullable;

import baubles.api.BaubleType;
import baubles.api.cap.BaubleItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.items.IRechargable;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.items.RechargeHelper;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.augment.AugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.builder.IElytraHarnessAugment;
import thecodex6824.thaumicaugmentation.common.capability.provider.CapabilityProviderHarness;
import thecodex6824.thaumicaugmentation.common.event.AugmentEventHandler;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationHandler;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.item.trait.IElytraCompat;
import vazkii.botania.api.item.IPhantomInkable;

@Optional.Interface(iface = "vazkii.botania.api.item.IPhantomInkable", modid = IntegrationHandler.BOTANIA_MOD_ID)
public class ItemElytraHarness extends ItemTABase implements IElytraCompat, IRechargable, IPhantomInkable {

    protected static final int VIS_MAX = 50;
    
    public ItemElytraHarness() {
        super();
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(221);
    }
    
    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        CapabilityProviderHarness provider = new CapabilityProviderHarness(new AugmentableItem(2) {
        
            @Override
            public boolean isAugmentAcceptable(ItemStack augment, int slot) {
                if (!(augment.getCapability(CapabilityAugment.AUGMENT, null) instanceof IElytraHarnessAugment))
                    return false;
                else if (((IElytraHarnessAugment) augment.getCapability(CapabilityAugment.AUGMENT, null)).isCosmetic()) {
                    for (ItemStack stack : getAllAugments()) {
                        if (!stack.isEmpty()) {
                            IAugment aug = stack.getCapability(CapabilityAugment.AUGMENT, null);
                            if (aug instanceof IElytraHarnessAugment && ((IElytraHarnessAugment) aug).isCosmetic())
                                return false;
                        }
                    }
                }
                else {
                    for (ItemStack stack : getAllAugments()) {
                        if (!stack.isEmpty()) {
                            IAugment aug = stack.getCapability(CapabilityAugment.AUGMENT, null);
                            if (aug instanceof IElytraHarnessAugment && !((IElytraHarnessAugment) aug).isCosmetic())
                                return false;
                        }
                    }
                }
                
                return true;
            }
            
        }, new BaubleItem(BaubleType.BODY) {
            
            protected boolean sync;
            
            @Override
            public void onEquipped(ItemStack itemstack, EntityLivingBase player) {
                AugmentEventHandler.onEquipmentChange(player);
                sync = true;
            }
            
            @Override
            public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
                AugmentEventHandler.onEquipmentChange(player);
            }
            
            @Override
            public void onWornTick(ItemStack stack, EntityLivingBase entity) {
                if (!entity.world.isRemote) {
                    if (!(entity instanceof EntityPlayer) || (!((EntityPlayer) entity).isCreative()) &&
                            (entity.ticksElytraFlying + 1) % 20 == 0) {
                        if (RechargeHelper.getCharge(stack) > 0)
                            RechargeHelper.consumeCharge(stack, entity, 1);
                        else if (stack.getItemDamage() < stack.getMaxDamage() - 1) {
                            stack.damageItem(1, entity);
                            sync = true;
                        }
                    }
                    
                    if (entity.ticksExisted % 60 == 0 && entity.onGround) {
                        if (stack.getItemDamage() > 0) {
                            // still repair the stack in creative
                            stack.setItemDamage(stack.getItemDamage() - 1);
                            sync = true;
                        }
                    }
                }
            }
            
            @Override
            public boolean willAutoSync(ItemStack itemstack, EntityLivingBase player) {
                boolean res = sync;
                sync = false;
                return res;
            }
        });
        
        if (nbt != null && nbt.hasKey("Parent", NBT.TAG_COMPOUND))
            provider.deserializeNBT(nbt.getCompoundTag("Parent"));
        
        return provider;
    }
    
    @Override
    public int getMaxCharge(ItemStack stack, EntityLivingBase entity) {
        if (entity != null && !entity.onGround)
            return RechargeHelper.getCharge(stack);
        else
            return VIS_MAX;
    }
    
    @Override
    public EnumChargeDisplay showInHud(ItemStack arg0, EntityLivingBase arg1) {
        return EnumChargeDisplay.NORMAL;
    }
    
    @Override
    public boolean allowElytraFlight(EntityPlayer wearer, ItemStack stack) {
        return RechargeHelper.getCharge(stack) > 0 || stack.getItemDamage() < stack.getMaxDamage() - 1;
    }
    
    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return toRepair.getItemDamage() > 0 && !repair.isEmpty() && 
                OreDictionary.itemMatches(new ItemStack(ItemsTC.ingots, 1, 1), repair, false);
    }
    
    @Override
    @Optional.Method(modid = IntegrationHandler.BOTANIA_MOD_ID)
    public boolean hasPhantomInk(ItemStack stack) {
        if (stack.hasTagCompound())
            return stack.getTagCompound().getBoolean("phantomInk");
        
        return false;
    }
    
    @Override
    @Optional.Method(modid = IntegrationHandler.BOTANIA_MOD_ID)
    public void setPhantomInk(ItemStack stack, boolean ink) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        stack.getTagCompound().setBoolean("phantomInk", ink);
    }
    
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        NBTTagCompound tag = new NBTTagCompound();
        if (stack.hasTagCompound())
            tag.setTag("item", stack.getTagCompound().copy());
        
        tag.setTag("cap", ((AugmentableItem) stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)).serializeNBT());
        return tag;
    }
    
    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        if (nbt != null) {
            ((AugmentableItem) stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)).deserializeNBT(nbt.getCompoundTag("cap"));
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
    
}
