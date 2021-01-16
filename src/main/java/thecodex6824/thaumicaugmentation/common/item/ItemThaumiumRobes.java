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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
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
import thaumcraft.api.items.IGoggles;
import thaumcraft.api.items.IRevealer;
import thaumcraft.api.items.IVisDiscountGear;
import thecodex6824.thaumicaugmentation.api.TAMaterials;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.item.IDyeableItem;
import thecodex6824.thaumicaugmentation.client.model.ModelTARobes;
import thecodex6824.thaumicaugmentation.common.util.IModelProvider;

@SuppressWarnings("deprecation")
public class ItemThaumiumRobes extends ItemArmor implements IVisDiscountGear,
    IGoggles, IRevealer, ISpecialArmor, IDyeableItem, IModelProvider<Item> {

    protected static final String TEXTURE_PATH = 
            new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/models/armor/thaumium_robes.png").toString();

    protected static final String TEXTURE_PATH_OVERLAY = 
            new ResourceLocation(ThaumicAugmentationAPI.MODID, "textures/models/armor/thaumium_robes_overlay.png").toString();
    
    @SideOnly(Side.CLIENT)
    protected ModelBiped model;
    
    public ItemThaumiumRobes(EntityEquipmentSlot slot) {
        super(TAMaterials.THAUMIUM_ROBES, 4, slot);
    }
    
    @Override
    public void damageArmor(EntityLivingBase entity, @Nonnull ItemStack stack, DamageSource source, int damage,
            int slot) {
        
        if (source != DamageSource.FALL)
            stack.damageItem(damage, entity);
    }
    
    @Override
    public int getArmorDisplay(EntityPlayer player, @Nonnull ItemStack armor, int slot) {
        return 0;
    }
    
    @Override
    public ArmorProperties getProperties(EntityLivingBase player, @Nonnull ItemStack armor, DamageSource source,
            double damage, int slot) {
        
        int priority = 0;
        double ratio = damageReduceAmount / 25.0;
        if (source.isMagicDamage()) {
            priority = 1;
            ratio = damageReduceAmount / 26.0;
        }
        else if (source.isUnblockable()) {
            priority = 0;
            ratio = 0.0;
        }

        return new ArmorProperties(priority, ratio, armor.getMaxDamage() + 1 - armor.getItemDamage());
    }
    
    @Override
    public int getDefaultDyedColorForMeta(int meta) {
        return 0xAD59A6;
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
    public int getVisDiscount(ItemStack stack, EntityPlayer player) {
        EntityEquipmentSlot slot = EntityLiving.getSlotForItemStack(stack);
        if (slot == EntityEquipmentSlot.HEAD)
            return 5;
        else if (slot == EntityEquipmentSlot.CHEST)
            return 4;
        else
            return 3;
    }
    
    @Override
    public boolean showIngamePopups(ItemStack stack, EntityLivingBase living) {
        return stack.getItem() instanceof ItemArmor &&
                ((ItemArmor) stack.getItem()).getEquipmentSlot() == EntityEquipmentSlot.HEAD;
    }
    
    @Override
    public boolean showNodes(ItemStack stack, EntityLivingBase living) {
        return stack.getItem() instanceof ItemArmor &&
                ((ItemArmor) stack.getItem()).getEquipmentSlot() == EntityEquipmentSlot.HEAD;
    }
    
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return type == null ? TEXTURE_PATH_OVERLAY : TEXTURE_PATH;
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
        return TAMaterials.RARITY_ARCANE;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName().toString(), "inventory"));
    }
    
    @Override
    @Nullable
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot,
            ModelBiped _default) {
        
        if (model == null) {
            if (slot == EntityEquipmentSlot.CHEST || slot == EntityEquipmentSlot.FEET)
                model = new ModelTARobes(1.0F);
            else
                model = new ModelTARobes(0.5F);
            
            model.bipedHead.showModel = slot == EntityEquipmentSlot.HEAD;
            model.bipedHeadwear.showModel = slot == EntityEquipmentSlot.HEAD;
            model.bipedBody.showModel = slot == EntityEquipmentSlot.CHEST || slot == EntityEquipmentSlot.LEGS;
            model.bipedRightArm.showModel = slot == EntityEquipmentSlot.CHEST;
            model.bipedLeftArm.showModel = slot == EntityEquipmentSlot.CHEST;
            model.bipedRightLeg.showModel = slot == EntityEquipmentSlot.LEGS;
            model.bipedLeftLeg.showModel = slot == EntityEquipmentSlot.LEGS;
        }
          
        return model;
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
