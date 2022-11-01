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

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAMaterials;
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.*;
import thecodex6824.thaumicaugmentation.api.augment.builder.IImpulseCannonAugment;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusAPI;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusStorage;
import thecodex6824.thaumicaugmentation.api.util.RaytraceHelper;
import thecodex6824.thaumicaugmentation.common.capability.provider.CapabilityProviderImpulseCannon;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.network.PacketImpulseBeam;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.util.ItemHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ItemImpulseCannon extends ItemTABase {

    public ItemImpulseCannon() {
        super();
        setMaxStackSize(1);
        setHasSubtypes(true);
        addPropertyOverride(new ResourceLocation(ThaumicAugmentationAPI.MODID, "arm"), new IItemPropertyGetter() {
            @Override
            public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                if (entity != null) {
                    if (entity.getHeldItemMainhand().equals(stack) && entity.getHeldItemOffhand().isEmpty())
                        return 1.0F;
                    else if (entity.getHeldItemOffhand().equals(stack) && entity.getHeldItemMainhand().isEmpty())
                        return 1.0F;
                }
                
                return 0.0F;
            }
        });
    }
    
    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        CapabilityProviderImpulseCannon provider = new CapabilityProviderImpulseCannon(new AugmentableItem(1) {
            @Override
            public boolean isAugmentAcceptable(ItemStack augment, int slot) {
                return augment.getCapability(CapabilityAugment.AUGMENT, null) instanceof IImpulseCannonAugment;
            }
        }, new ImpetusStorage(1000, 100, 1000));
        
        if (nbt != null && nbt.hasKey("Parent", NBT.TAG_COMPOUND))
            provider.deserializeNBT(nbt.getCompoundTag("Parent"));
        
        return provider;
    }
    
    @Nullable
    protected IImpulseCannonAugment getAugment(ItemStack stack) {
        IAugmentableItem augments = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
        if (augments != null && augments.isAugmented()) {
            IAugment aug = augments.getAugment(0).getCapability(CapabilityAugment.AUGMENT, null);
            if (aug instanceof IImpulseCannonAugment)
                return (IImpulseCannonAugment) aug;
        }
            
        return null;
    }
    
    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return ItemStack.areItemsEqual(oldStack, newStack);
    }
    
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.areItemsEqual(oldStack, newStack);
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        IImpetusStorage buffer = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
        IImpulseCannonAugment aug = getAugment(stack);
        if (!world.isRemote) {
            if (aug != null && !aug.isTickable(player) && buffer != null) {
                long cost = aug.getImpetusCostPerUsage(player);
                if (ImpetusAPI.tryExtractFully(buffer, cost, player)) {
                    aug.onCannonUsage(player);
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                }
            }
            else if ((aug == null || aug.isTickable(player)) && buffer != null) {
                long cost = aug != null ? aug.getImpetusCostPerUsage(player) : TAConfig.cannonBeamCostInitial.getValue();
                if (buffer.canExtract() && buffer.getEnergyStored() >= cost) {
                    player.setActiveHand(hand);
                    if (aug == null) {
                        Random rand = player.getRNG();
                        player.getEntityWorld().playSound(null, new BlockPos(player.getPositionEyes(1.0F)), TASounds.IMPULSE_CANNON_BEAM_START,
                                SoundCategory.PLAYERS, 1.0F, (rand.nextFloat() - rand.nextFloat()) / 4.0F + 1.0F);
                        PacketImpulseBeam packet = new PacketImpulseBeam(player.getEntityId(), false);
                        TANetwork.INSTANCE.sendToAllTracking(packet, player);
                        if (player instanceof EntityPlayerMP)
                            TANetwork.INSTANCE.sendTo(packet, (EntityPlayerMP) player);
                    }
                        
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                }
            }
            
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        else
            return super.onItemRightClick(world, player, hand);
    }
    
    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        IImpetusStorage buffer = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
        IImpulseCannonAugment aug = getAugment(stack);
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        if (!player.getEntityWorld().isRemote) {
            if (aug != null && buffer != null) {
                double cost = aug.getImpetusCostPerTick(player, count);
                double accumulated = stack.getTagCompound().getDouble("acc") + cost;
                if (accumulated >= 1.0) {
                    long remove = (long) Math.floor(accumulated);
                    if (ImpetusAPI.tryExtractFully(buffer, remove, player))
                        accumulated -= remove;
                }
                
                stack.getTagCompound().setDouble("acc", accumulated);
                if (accumulated < 1.0)
                    aug.onCannonTick(player, count);
                else {
                    player.stopActiveHand();
                    aug.onStopCannonTick(player, count);
                }
            }
            else if (buffer != null) {
                double cost = TAConfig.cannonBeamCostTick.getValue();
                double accumulated = stack.getTagCompound().getDouble("acc") + cost;
                if (accumulated >= 1.0) {
                    long remove = (long) Math.floor(accumulated);
                    if (ImpetusAPI.tryExtractFully(buffer, remove, player))
                        accumulated -= remove;
                }
                
                stack.getTagCompound().setDouble("acc", accumulated);
                if (accumulated < 1.0) {
                    Entity e = RaytraceHelper.raytraceEntity(player, TAConfig.cannonBeamRange.getValue());
                    if (e != null) {
                        ImpetusAPI.causeImpetusDamage(player, e, TAConfig.cannonBeamDamage.getValue());
                        if (e instanceof EntityLivingBase) {
                            EntityLivingBase base = (EntityLivingBase) e;
                            base.hurtResistantTime = Math.min(base.hurtResistantTime, 2);
                            base.lastDamage = 0.0F;
                        }
                    }
                    
                    if (player.ticksExisted % 20 == 0) {
                        PacketImpulseBeam packet = new PacketImpulseBeam(player.getEntityId(), false);
                        TANetwork.INSTANCE.sendToAllTracking(packet, player);
                        if (player instanceof EntityPlayerMP)
                            TANetwork.INSTANCE.sendTo(packet, (EntityPlayerMP) player);
                    }
                }
                else {
                    player.stopActiveHand();
                    Random rand = player.getRNG();
                    player.getEntityWorld().playSound(null, new BlockPos(player.getPositionEyes(1.0F)), TASounds.IMPULSE_CANNON_BEAM_END,
                            SoundCategory.PLAYERS, 1.0F, (rand.nextFloat() - rand.nextFloat()) / 4.0F + 1.0F);
                    PacketImpulseBeam packet = new PacketImpulseBeam(player.getEntityId(), true);
                    TANetwork.INSTANCE.sendToAllTracking(packet, player);
                    if (player instanceof EntityPlayerMP)
                        TANetwork.INSTANCE.sendTo(packet, (EntityPlayerMP) player);
                }
            }
        }
    }
    
    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int timeLeft) {
        if (!world.isRemote) {
            IImpulseCannonAugment aug = getAugment(stack);
            if (aug == null) {
                Random rand = entity.getRNG();
                entity.getEntityWorld().playSound(null, new BlockPos(entity.getPositionEyes(1.0F)), TASounds.IMPULSE_CANNON_BEAM_END,
                        SoundCategory.PLAYERS, (rand.nextFloat() - rand.nextFloat()) / 2.0F + 0.5F, (rand.nextFloat() - rand.nextFloat()) / 4.0F + 1.0F);
                PacketImpulseBeam packet = new PacketImpulseBeam(entity.getEntityId(), true);
                TANetwork.INSTANCE.sendToAllTracking(packet, entity);
                if (entity instanceof EntityPlayerMP)
                    TANetwork.INSTANCE.sendTo(packet, (EntityPlayerMP) entity);
            }
            else if (aug.isTickable(entity))
                aug.onStopCannonTick(entity, timeLeft);
        }
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }
    
    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        IImpulseCannonAugment aug = getAugment(stack);
        if (aug != null)
            return aug.getMaxUsageDuration();
        else
            return 72000;
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
        NBTTagCompound augment = ItemHelper.tryMakeCapabilityTag(stack, CapabilityAugmentableItem.AUGMENTABLE_ITEM);
        if (augment != null)
            tag.getCompoundTag("cap").setTag("augmentable", augment);

        NBTTagCompound energy = ItemHelper.tryMakeCapabilityTag(stack, CapabilityImpetusStorage.IMPETUS_STORAGE);
        if (energy != null)
            tag.getCompoundTag("cap").setTag("energy", energy);

        return tag;
    }
    
    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        if (nbt != null) {
            if (nbt.hasKey("cap", NBT.TAG_COMPOUND)) {
                ((AugmentableItem) stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)).deserializeNBT(nbt.getCompoundTag("cap").getCompoundTag("augmentable"));
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
    public boolean isFull3D() {
        return true;
    }
    
    @Override
    public IRarity getForgeRarity(ItemStack stack) {
        return TAMaterials.RARITY_ELDRITCH;
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
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelResourceLocation LENS_BEAM = new ModelResourceLocation(ThaumicAugmentationAPI.MODID + ":impulse_cannon_beam", "inventory");
        ModelResourceLocation LENS_RAILGUN = new ModelResourceLocation(ThaumicAugmentationAPI.MODID + ":impulse_cannon_railgun", "inventory");
        ModelResourceLocation LENS_BURST = new ModelResourceLocation(ThaumicAugmentationAPI.MODID + ":impulse_cannon_burst", "inventory");
        ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                IAugmentableItem augments = stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null);
                if (augments != null && augments.isAugmented()) {
                    IAugment aug = augments.getAugment(0).getCapability(CapabilityAugment.AUGMENT, null);
                    if (aug instanceof IImpulseCannonAugment) {
                        switch (((IImpulseCannonAugment) aug).getLensModel()) {
                            case RAILGUN: return LENS_RAILGUN;
                            case BURST: return LENS_BURST;
                            case BEAM:
                            default: return LENS_BEAM;
                        }
                    }
                }
                
                return LENS_BEAM;
            }
        });
        
        ModelLoader.registerItemVariants(this, LENS_BEAM, LENS_RAILGUN, LENS_BURST);
    }
    
}
