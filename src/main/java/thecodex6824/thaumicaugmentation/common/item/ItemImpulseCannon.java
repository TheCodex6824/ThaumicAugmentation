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

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
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
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.AugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugmentableItem;
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
        }, new ImpetusStorage(800, 50, 800));
        
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
            else if (((aug != null && aug.isTickable(player)) || aug == null) && buffer != null) {
                long cost = aug != null ? aug.getImpetusCostPerTick(player, -1) : TAConfig.cannonBeamCost.getValue();
                if (buffer.canExtract() && buffer.getEnergyStored() >= cost) {
                    player.setActiveHand(hand);
                    if (aug == null) {
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
        if (!player.getEntityWorld().isRemote) {
            if (aug != null && buffer != null) {
                if (ImpetusAPI.tryExtractFully(buffer, aug.getImpetusCostPerTick(player, count), player))
                    aug.onCannonTick(player, count);
                else {
                    player.stopActiveHand();
                    aug.onStopCannonTick(player, count);
                }
            }
            else if (buffer != null) {
                if (ImpetusAPI.tryExtractFully(buffer, TAConfig.cannonBeamCost.getValue(), player)) {
                    Entity e = RaytraceHelper.raytraceEntity(player, TAConfig.cannonBeamRange.getValue());
                    if (e != null)
                        ImpetusAPI.causeImpetusDamage(player, e, TAConfig.cannonBeamDamage.getValue());
                    
                    if (player.ticksExisted % 20 == 0) {
                        PacketImpulseBeam packet = new PacketImpulseBeam(player.getEntityId(), false);
                        TANetwork.INSTANCE.sendToAllTracking(packet, player);
                        if (player instanceof EntityPlayerMP)
                            TANetwork.INSTANCE.sendTo(packet, (EntityPlayerMP) player);
                    }
                }
                else {
                    player.stopActiveHand();
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
        if (stack.hasTagCompound())
            tag.setTag("item", stack.getTagCompound().copy());
        
        tag.setTag("cap", new NBTTagCompound());
        tag.getCompoundTag("cap").setTag("augmentable", ((AugmentableItem) stack.getCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)).serializeNBT());
        tag.getCompoundTag("cap").setTag("energy", ((ImpetusStorage) stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null)).serializeNBT());
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
