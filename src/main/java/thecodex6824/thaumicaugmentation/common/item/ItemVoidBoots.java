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

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.items.IRechargable;
import thaumcraft.api.items.IVisDiscountGear;
import thaumcraft.api.items.IWarpingGear;
import thaumcraft.api.items.RechargeHelper;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.TAMaterials;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.entity.PlayerMovementAbilityManager;
import thecodex6824.thaumicaugmentation.api.entity.PlayerMovementAbilityManager.MovementType;
import thecodex6824.thaumicaugmentation.api.item.IArmorReduceFallDamage;
import thecodex6824.thaumicaugmentation.api.item.IDyeableItem;
import thecodex6824.thaumicaugmentation.common.util.IModelProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class ItemVoidBoots extends ItemArmor implements IDyeableItem, IModelProvider<Item>, IArmorReduceFallDamage, IRechargable,
    IVisDiscountGear, ISpecialArmor, IWarpingGear {

    protected static final String TEXTURE_PATH = 
            new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/models/armor/boots_void.png").toString();

    protected static final String TEXTURE_PATH_OVERLAY = 
            new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/models/armor/boots_void_overlay.png").toString();

    protected static final BiFunction<EntityPlayer, MovementType, Float> MOVEMENT_FUNC = new BiFunction<EntityPlayer, MovementType, Float>() {
        @Override
        public Float apply(EntityPlayer player, MovementType type) {
            switch (type) {
                case DRY_GROUND: {
                    float boost = TAConfig.voidBootsLandSpeedBoost.getValue().floatValue();
                    return player.isSneaking() ? boost / TAConfig.voidBootsSneakReduction.getValue().floatValue() : boost;
                }
                case JUMP_BEGIN: return TAConfig.voidBootsJumpBoost.getValue().floatValue();
                case JUMP_FACTOR: return TAConfig.voidBootsJumpFactor.getValue().floatValue();
                case STEP_HEIGHT: return !player.isSneaking() ? TAConfig.voidBootsStepHeight.getValue().floatValue() : 0.0F;
                case WATER_GROUND: {
                    float boost = Math.max(TAConfig.voidBootsLandSpeedBoost.getValue().floatValue() / 4.0F, TAConfig.voidBootsWaterSpeedBoost.getValue().floatValue());
                    return player.isSneaking() ? boost / TAConfig.voidBootsSneakReduction.getValue().floatValue() : boost;
                }
                case WATER_SWIM: {
                    float boost = TAConfig.voidBootsWaterSpeedBoost.getValue().floatValue();
                    return player.isSneaking() ? boost / TAConfig.voidBootsSneakReduction.getValue().floatValue() : boost;
                }
                default: return 0.0F;
            }
        }
    };

    protected static final Predicate<EntityPlayer> CONTINUE_FUNC = new Predicate<EntityPlayer>() {
        @Override
        public boolean test(EntityPlayer player) {
            for (ItemStack stack : player.getArmorInventoryList()) {
                if (stack.getItem() == TAItems.VOID_BOOTS)
                    return true;
            }

            return false;
        }
    };

    public ItemVoidBoots() {
        super(TAMaterials.VOID_BOOTS, 0, EntityEquipmentSlot.FEET);
    }

    @Override
    public void damageArmor(EntityLivingBase entity, @Nonnull ItemStack stack, DamageSource source, int damage, int slot) {
        if (source != DamageSource.FALL)
            stack.damageItem(damage, entity);
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, @Nonnull ItemStack armor, int slot) {
        return 0;
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, @Nonnull ItemStack armor, DamageSource source, double damage,
            int slot) {

        int priority = 0;
        double ratio = 0.04; // 1 / 25.0
        if (source.isMagicDamage()) {
            priority = 1;
            ratio = 0.028571428571429; // 1 / 35.0
        }
        else if (source.isUnblockable()) {
            priority = 0;
            ratio = 0.0;
        }

        return new ArmorProperties(priority, ratio, armor.getMaxDamage() + 1 - armor.getItemDamage());
    }

    @Override
    public int getVisDiscount(ItemStack stack, EntityPlayer player) {
        return 5;
    }

    @Override
    public int getWarp(ItemStack stack, EntityPlayer player) {
        return 3;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return type == null ? TEXTURE_PATH : TEXTURE_PATH_OVERLAY;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == TAItems.CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
            ItemStack base = new ItemStack(this, 1, 0);
            items.add(base);
            ItemStack charged = base.copy();
            RechargeHelper.rechargeItemBlindly(charged, null, getMaxCharge(charged, null));
            items.add(charged);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName().toString(), "inventory"));
    }

    @Override
    public int getMaxCharge(ItemStack stack, EntityLivingBase entity) {
        return 480;
    }

    @Override
    public EnumChargeDisplay showInHud(ItemStack arg0, EntityLivingBase arg1) {
        return IRechargable.EnumChargeDisplay.PERIODIC;
    }

    @Override
    public int getDefaultDyedColorForMeta(int meta) {
        return meta == 0 ? TAConfig.defaultVoidBootsColor.getValue() : 0;
    }

    @Override
    public int getDyedColor(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        if (!stack.getTagCompound().hasKey("color", NBT.TAG_INT))
            stack.getTagCompound().setInteger("color", getDefaultDyedColorForMeta(stack.getMetadata()));

        return stack.getTagCompound().getInteger("color");
    }

    @Override
    public void setDyedColor(ItemStack stack, int color) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        stack.getTagCompound().setInteger("color", color);
    }

    @Override
    public boolean hasColor(ItemStack stack) {
        return true;
    }

    @Override
    public int getColor(ItemStack stack) {
        return getDyedColor(stack);
    }

    @Override
    public void removeColor(ItemStack stack) {
        setDyedColor(stack, getDefaultDyedColorForMeta(stack.getMetadata()));
    }

    @Override
    public void setColor(ItemStack stack, int color) {
        setDyedColor(stack, color);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
            float hitY, float hitZ, EnumHand hand) {

        ItemStack stack = player.getHeldItem(hand);
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == Blocks.CAULDRON && state.getValue(BlockCauldron.LEVEL) > 0 && 
                getDyedColor(stack) != getDefaultDyedColorForMeta(stack.getMetadata())) {
            setDyedColor(stack, getDefaultDyedColorForMeta(stack.getMetadata()));
            world.setBlockState(pos, state.withProperty(BlockCauldron.LEVEL, state.getValue(BlockCauldron.LEVEL) - 1));
            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 0.5F, 1.0F);
            return EnumActionResult.SUCCESS;
        }

        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    @Override
    public IRarity getForgeRarity(ItemStack stack) {
        return TAMaterials.RARITY_ELDRITCH;
    }

    @Override
    public float getNewFallDamage(ItemStack stack, float origDamage, float distance) {
        if (RechargeHelper.getCharge(stack) > 0)
            return origDamage / 6.0F - 1.0F;

        return origDamage;
    }
    
    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
        if (!world.isRemote && player.ticksExisted % 20 == 0) {
            if (stack.getItemDamage() > 0) {
                stack.attemptDamageItem(-1, player.getRNG(),
                        player instanceof EntityPlayerMP ? (EntityPlayerMP) player : null);
            }

            int current = 0;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("energyRemaining", NBT.TAG_INT))
                current = stack.getTagCompound().getInteger("energyRemaining");
            else if (!stack.hasTagCompound())
                stack.setTagCompound(new NBTTagCompound());

            if (current > 0)
                --current;
            if (current <= 0 && RechargeHelper.consumeCharge(stack, player, 3))
                current = 60;

            stack.getTagCompound().setInteger("energyRemaining", current);
        }
        if (PlayerMovementAbilityManager.isValidSideForMovement(player)) {
            boolean apply = !player.capabilities.isFlying && !player.isElytraFlying() && RechargeHelper.getCharge(stack) > 0;
            if (apply && !PlayerMovementAbilityManager.playerHasAbility(player, MOVEMENT_FUNC, CONTINUE_FUNC))
                PlayerMovementAbilityManager.put(player, MOVEMENT_FUNC, CONTINUE_FUNC);
            else if (!apply && PlayerMovementAbilityManager.playerHasAbility(player, MOVEMENT_FUNC, CONTINUE_FUNC))
                PlayerMovementAbilityManager.remove(player, MOVEMENT_FUNC, CONTINUE_FUNC);
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        int color = getDyedColor(stack);
        if (color != getDefaultDyedColorForMeta(stack.getMetadata())) {
            if (flag.isAdvanced())
                tooltip.add(new TextComponentTranslation("item.color", TextFormatting.GRAY + String.format("#%06X", color)).getFormattedText());
            else
                tooltip.add(TextFormatting.ITALIC + new TextComponentTranslation("item.dyed").getFormattedText());
        }
    }
    
}
