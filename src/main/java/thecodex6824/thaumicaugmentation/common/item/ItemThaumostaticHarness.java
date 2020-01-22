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
import thaumcraft.api.items.IRechargable;
import thaumcraft.api.items.RechargeHelper;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.augment.AugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.builder.IThaumostaticHarnessAugment;
import thecodex6824.thaumicaugmentation.api.entity.PlayerMovementAbilityManager;
import thecodex6824.thaumicaugmentation.common.capability.CapabilityProviderHarness;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationHandler;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import vazkii.botania.api.item.IPhantomInkable;

@Optional.Interface(iface = "vazkii.botania.api.item.IPhantomInkable", modid = IntegrationHandler.BOTANIA_MOD_ID)
public class ItemThaumostaticHarness extends ItemTABase implements IRechargable, IPhantomInkable {

    protected static final int DEFAULT_VIS_CAPACITY = 200;
    protected static final int DEFAULT_VIS_COST = 2;
    protected static final float DEFAULT_FLY_SPEED = 0.05F;
    
    public ItemThaumostaticHarness() {
        super();
        setMaxStackSize(1);
        setHasSubtypes(true);
    }
    
    protected static int getHarnessVisCapacity(ItemStack stack) {
        IAugmentableItem item = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        if (item != null) {
            IAugment aug = item.getAugment(0).getCapability(CapabilityAugment.AUGMENT, null);
            if (aug instanceof IThaumostaticHarnessAugment)
                return ((IThaumostaticHarnessAugment) aug).getVisCapacity();
        }
        
        return DEFAULT_VIS_CAPACITY;
    }
    
    protected static int getHarnessVisCost(ItemStack stack, EntityPlayer player) {
        IAugmentableItem item = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        if (item != null) {
            IAugment aug = item.getAugment(0).getCapability(CapabilityAugment.AUGMENT, null);
            if (aug instanceof IThaumostaticHarnessAugment)
                return ((IThaumostaticHarnessAugment) aug).getVisCostPerThreeSeconds(player);
        }
        
        return DEFAULT_VIS_COST;
    }
    
    protected static float getHarnessFlySpeed(ItemStack stack, EntityPlayer player) {
        IAugmentableItem item = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        if (item != null) {
            IAugment aug = item.getAugment(0).getCapability(CapabilityAugment.AUGMENT, null);
            if (aug instanceof IThaumostaticHarnessAugment)
                return ((IThaumostaticHarnessAugment) aug).getFlySpeed(player);
        }
        
        return DEFAULT_FLY_SPEED;
    }
    
    protected static void applyHarnessDrift(ItemStack stack, EntityPlayer player) {
        IAugmentableItem item = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        if (item != null) {
            IAugment aug = item.getAugment(0).getCapability(CapabilityAugment.AUGMENT, null);
            if (aug instanceof IThaumostaticHarnessAugment) {
                ((IThaumostaticHarnessAugment) aug).applyDrift(player);
                return;
            }
        }
        
        player.motionX *= 1.015F;
        player.motionY *= 1.015F;
        player.motionZ *= 1.015F;
    }
    
    protected static boolean allowSprintFlying(ItemStack stack, EntityPlayer player) {
        IAugmentableItem item = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        if (item != null) {
            IAugment aug = item.getAugment(0).getCapability(CapabilityAugment.AUGMENT, null);
            if (aug instanceof IThaumostaticHarnessAugment)
                return ((IThaumostaticHarnessAugment) aug).shouldAllowSprintFly(player);
        }
        
        return false;
    }
    
    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        CapabilityProviderHarness provider = new CapabilityProviderHarness(new AugmentableItem(1) {
            
            @Override
            public boolean isAugmentAcceptable(ItemStack augment, int slot) {
                return augment.getCapability(CapabilityAugment.AUGMENT, null) instanceof IThaumostaticHarnessAugment;
            }
            
            @Override
            public void setAugment(ItemStack augment, int slot) {
                super.setAugment(augment, slot);
                int difference = RechargeHelper.getCharge(stack) - getHarnessVisCapacity(stack);
                if (difference > 0)
                    RechargeHelper.consumeCharge(stack, null, difference);
            }
            
            @Override
            public ItemStack[] setAllAugments(ItemStack[] augs) {
                ItemStack[] ret = super.setAllAugments(augs);
                int difference = RechargeHelper.getCharge(stack) - getHarnessVisCapacity(stack);
                if (difference > 0)
                    RechargeHelper.consumeCharge(stack, null, difference);
                
                return ret;
            }
            
        }, new BaubleItem(BaubleType.BODY) {
            
            @Override
            public void onEquipped(ItemStack itemstack, EntityLivingBase entity) {
                if (!entity.world.isRemote && entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    int cost = getHarnessVisCost(itemstack, player);
                    float speed = getHarnessFlySpeed(itemstack, player);
                    if (RechargeHelper.getCharge(itemstack) >= cost) {
                        PlayerMovementAbilityManager.recordFlyState(player);
                        player.capabilities.allowFlying = true;
                        if (player.world.isRemote)
                            player.capabilities.flySpeed = speed;
                        
                        player.sendPlayerAbilities();
                    }
                }
            }
            
            @Override
            public void onWornTick(ItemStack itemstack, EntityLivingBase entity) {
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    int cost = getHarnessVisCost(itemstack, player);
                    if (player.capabilities.isFlying) {
                        int current = 0;
                        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("energyRemaining", NBT.TAG_INT))
                            current = stack.getTagCompound().getInteger("energyRemaining");
                        else if (!stack.hasTagCompound())
                            stack.setTagCompound(new NBTTagCompound());

                        if (current > 0)
                            --current;
                        if (current <= 0 && RechargeHelper.consumeCharge(stack, player, cost))
                            current = 60;
                        else if (current <= 0 && !player.world.isRemote) {
                            if (!PlayerMovementAbilityManager.popAndApplyFlyState(player)) {
                                if (!player.isCreative() && !player.isSpectator()) {
                                    player.capabilities.allowFlying = false;
                                    player.capabilities.isFlying = false;
                                }
                                
                                player.capabilities.flySpeed = 0.05F;
                                player.sendPlayerAbilities();
                            }
                        }

                        stack.getTagCompound().setInteger("energyRemaining", current);
                        if (player.capabilities.isFlying && player.isSprinting() && !allowSprintFlying(stack, player))
                            player.setSprinting(false);
                        if (player.capabilities.isFlying)
                            applyHarnessDrift(stack, player);
                    }
                    else if (!player.world.isRemote && RechargeHelper.getCharge(itemstack) < cost) {
                        if (!PlayerMovementAbilityManager.popAndApplyFlyState(player)) {
                            if (!player.isCreative() && !player.isSpectator()) {
                                player.capabilities.allowFlying = false;
                                player.capabilities.isFlying = false;
                            }
                            
                            player.capabilities.flySpeed = 0.05F;
                            player.sendPlayerAbilities();
                        }
                    }
                    else if (!player.world.isRemote) {
                        PlayerMovementAbilityManager.recordFlyState(player);
                        player.capabilities.allowFlying = true;
                        player.capabilities.flySpeed = getHarnessFlySpeed(itemstack, player);
                        player.sendPlayerAbilities();
                    }
                }
            }
            
            @Override
            public void onUnequipped(ItemStack itemstack, EntityLivingBase entity) {
                if (!entity.world.isRemote && entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    if (!PlayerMovementAbilityManager.popAndApplyFlyState(player)) {
                        if (!player.isCreative() && !player.isSpectator()) {
                            player.capabilities.allowFlying = false;
                            player.capabilities.isFlying = false;
                        }
                        
                        player.capabilities.flySpeed = 0.05F;
                        player.sendPlayerAbilities();
                    }
                }
            }
        });
        
        if (nbt != null && nbt.hasKey("Parent", NBT.TAG_COMPOUND))
            provider.deserializeNBT(nbt.getCompoundTag("Parent"));
        
        return provider;
    }
    
    @Override
    public int getMaxCharge(ItemStack stack, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isFlying)
            return RechargeHelper.getCharge(stack);
        else
            return getHarnessVisCapacity(stack);
    }
    
    @Override
    public EnumChargeDisplay showInHud(ItemStack arg0, EntityLivingBase arg1) {
        return EnumChargeDisplay.NORMAL;
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
        if (stack.hasTagCompound()) {
            NBTTagCompound item = stack.getTagCompound().copy();
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && !ThaumicAugmentation.proxy.isSingleplayer())
                item.removeTag("cap");
            
            tag.setTag("item", item);
        }
        
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
