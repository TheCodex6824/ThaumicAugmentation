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

import baubles.api.BaubleType;
import baubles.api.cap.BaubleItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import thaumcraft.api.items.IRechargable;
import thaumcraft.api.items.RechargeHelper;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAMaterials;
import thecodex6824.thaumicaugmentation.api.augment.AugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.builder.IThaumostaticHarnessAugment;
import thecodex6824.thaumicaugmentation.api.entity.PlayerMovementAbilityManager;
import thecodex6824.thaumicaugmentation.common.capability.provider.CapabilityProviderHarness;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationHandler;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.util.ItemHelper;
import vazkii.botania.api.item.IPhantomInkable;

import javax.annotation.Nullable;

@Optional.Interface(iface = "vazkii.botania.api.item.IPhantomInkable", modid = IntegrationHandler.BOTANIA_MOD_ID)
public class ItemThaumostaticHarness extends ItemTABase implements IRechargable, IPhantomInkable {

    protected static final int DEFAULT_VIS_CAPACITY = 200;
    
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
    
    protected static double getHarnessVisCost(ItemStack stack, EntityPlayer player) {
        IAugmentableItem item = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        if (item != null) {
            IAugment aug = item.getAugment(0).getCapability(CapabilityAugment.AUGMENT, null);
            if (aug instanceof IThaumostaticHarnessAugment)
                return ((IThaumostaticHarnessAugment) aug).getVisCostPerTick(player);
        }
        
        return TAConfig.baseHarnessCost.getValue();
    }
    
    protected static float getHarnessFlySpeed(ItemStack stack, EntityPlayer player) {
        IAugmentableItem item = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        if (item != null) {
            IAugment aug = item.getAugment(0).getCapability(CapabilityAugment.AUGMENT, null);
            if (aug instanceof IThaumostaticHarnessAugment)
                return ((IThaumostaticHarnessAugment) aug).getFlySpeed(player);
        }
        
        return TAConfig.baseHarnessSpeed.getValue();
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
            
            protected void checkVis() {
                int difference = RechargeHelper.getCharge(stack) - getHarnessVisCapacity(stack);
                if (difference > 0)
                    RechargeHelper.consumeCharge(stack, null, difference);
            }
            
            @Override
            public boolean isAugmentAcceptable(ItemStack augment, int slot) {
                return augment.getCapability(CapabilityAugment.AUGMENT, null) instanceof IThaumostaticHarnessAugment;
            }
            
            @Override
            public void setAugment(ItemStack augment, int slot) {
                super.setAugment(augment, slot);
                checkVis();
            }
            
            @Override
            public ItemStack[] setAllAugments(ItemStack[] augs) {
                ItemStack[] ret = super.setAllAugments(augs);
                checkVis();
                return ret;
            }
            
            @Override
            public ItemStack removeAugment(int slot) {
                ItemStack ret = super.removeAugment(slot);
                checkVis();
                return ret;
            }
            
        }, new BaubleItem(BaubleType.BODY) {
            
            @Override
            public void onEquipped(ItemStack itemstack, EntityLivingBase entity) {
                if (!entity.world.isRemote && entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    double cost = getHarnessVisCost(itemstack, player);
                    float speed = getHarnessFlySpeed(itemstack, player);
                    if (player.isCreative() || RechargeHelper.getCharge(itemstack) >= cost) {
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
                    double cost = getHarnessVisCost(itemstack, player);
                    if (player.capabilities.isFlying) {
                        if (!player.world.isRemote) {
                            if (!stack.hasTagCompound())
                                stack.setTagCompound(new NBTTagCompound());
                            
                            double current = stack.getTagCompound().getDouble("acc") + cost;
                            if (current >= 1.0) {
                            	if (player.isCreative()) {
                            		current = 0.0;
                            	}
                            	else {
                            		int remove = (int) Math.floor(current);
                            		if (RechargeHelper.consumeCharge(stack, entity, remove))
                            			current -= remove;
                            	}
                            }
                            
                            if (current >= 1.0) {
                                if (!PlayerMovementAbilityManager.popAndApplyFlyState(player)) {
                                    if (!player.isCreative() && !player.isSpectator()) {
                                        player.capabilities.allowFlying = false;
                                        player.capabilities.isFlying = false;
                                    }
                                    
                                    player.capabilities.flySpeed = 0.05F;
                                    player.sendPlayerAbilities();
                                }
                            }
                             
                            stack.getTagCompound().setDouble("acc", Math.min(current, Math.max(cost, 1.0)));
                        }

                        // might have stopped flying above
                        if (player.capabilities.isFlying) {
                            if (player.isSprinting() && !allowSprintFlying(stack, player))
                                player.setSprinting(false);
                            
                            applyHarnessDrift(stack, player);
                        }
                    }
                    else if (!player.world.isRemote && !player.isCreative() && RechargeHelper.getCharge(itemstack) < cost) {
                        if (!PlayerMovementAbilityManager.popAndApplyFlyState(player)) {
                            if (!player.isCreative() && !player.isSpectator()) {
                                player.capabilities.allowFlying = false;
                                player.capabilities.isFlying = false;
                            }
                            
                            player.capabilities.flySpeed = 0.05F;
                            player.sendPlayerAbilities();
                        }
                    }
                    else if (!player.world.isRemote && !player.capabilities.allowFlying) {
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
        return EnumChargeDisplay.PERIODIC;
    }
    
    @Override
    public IRarity getForgeRarity(ItemStack stack) {
        return TAMaterials.RARITY_ARCANE;
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
            if (!ThaumicAugmentation.proxy.isSingleplayer() && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
                item.removeTag("cap");

            tag.setTag("item", item);
        }

        NBTTagCompound cap = ItemHelper.tryMakeCapabilityTag(stack, CapabilityAugmentableItem.AUGMENTABLE_ITEM);
        if (cap != null)
            tag.setTag("cap", cap);

        return tag;
    }
    
    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        if (nbt != null) {
            if (nbt.hasKey("cap", NBT.TAG_COMPOUND))
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
