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
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.items.IWarpingGear;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.PacketParticleEffect.ParticleEffect;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.util.IModelProvider;

public class ItemPrimalCutter extends ItemTool implements IWarpingGear, IModelProvider<Item> {

    public static final ToolMaterial MATERIAL = EnumHelper.addToolMaterial(
            "PRIMAL_CUTTER", 5, 500, 8.0F, TAConfig.primalCutterDamage.getValue(), 20).setRepairItem(new ItemStack(ItemsTC.ingots, 1, 1));
    
    private static final ImmutableSet<String> TOOL_CLASSES = ImmutableSet.of("sword", "axe");
    private static final ImmutableSet<Block> EFFECTIVE = new ImmutableSet.Builder<Block>().add(
            Blocks.ACACIA_DOOR, Blocks.ACACIA_FENCE, Blocks.ACACIA_FENCE_GATE, Blocks.ACACIA_STAIRS,
            Blocks.BIRCH_DOOR, Blocks.BIRCH_FENCE, Blocks.BIRCH_FENCE_GATE, Blocks.BIRCH_STAIRS, Blocks.BOOKSHELF,
            Blocks.BROWN_MUSHROOM_BLOCK, Blocks.CHEST, Blocks.COCOA, Blocks.CRAFTING_TABLE, Blocks.DARK_OAK_DOOR,
            Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_FENCE_GATE, Blocks.DARK_OAK_STAIRS, Blocks.DAYLIGHT_DETECTOR,
            Blocks.DAYLIGHT_DETECTOR_INVERTED, Blocks.DOUBLE_WOODEN_SLAB, Blocks.JUKEBOX, Blocks.JUNGLE_DOOR,
            Blocks.JUNGLE_FENCE, Blocks.JUNGLE_FENCE_GATE, Blocks.JUNGLE_STAIRS, Blocks.LADDER, Blocks.LEAVES,
            Blocks.LEAVES2, Blocks.LIT_PUMPKIN, Blocks.LOG, Blocks.LOG2, Blocks.MELON_BLOCK, Blocks.NOTEBLOCK,
            Blocks.OAK_DOOR, Blocks.OAK_FENCE, Blocks.OAK_FENCE_GATE, Blocks.OAK_STAIRS, Blocks.PLANKS,
            Blocks.PUMPKIN, Blocks.RED_MUSHROOM_BLOCK, Blocks.SPRUCE_DOOR, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_FENCE_GATE,
            Blocks.SPRUCE_STAIRS, Blocks.STANDING_BANNER, Blocks.STANDING_SIGN, Blocks.TRAPPED_CHEST, Blocks.VINE,
            Blocks.WALL_BANNER, Blocks.WALL_SIGN, Blocks.WEB, Blocks.WOODEN_BUTTON, Blocks.WOODEN_PRESSURE_PLATE,
            Blocks.WOODEN_SLAB, BlocksTC.alembic, BlocksTC.arcaneEar, BlocksTC.arcaneEarToggle, BlocksTC.arcaneWorkbench,
            BlocksTC.bannerCrimsonCult, BlocksTC.bellows, BlocksTC.centrifuge, BlocksTC.doubleSlabGreatwood,
            BlocksTC.doubleSlabSilverwood, BlocksTC.hungryChest, BlocksTC.leafGreatwood, BlocksTC.leafSilverwood,
            BlocksTC.levitator, BlocksTC.logGreatwood, BlocksTC.logSilverwood, BlocksTC.lootCrateCommon, BlocksTC.lootCrateRare,
            BlocksTC.lootCrateUncommon, BlocksTC.placeholderTable, BlocksTC.plankGreatwood, BlocksTC.plankSilverwood,
            BlocksTC.researchTable, BlocksTC.slabGreatwood, BlocksTC.slabSilverwood, BlocksTC.stairsGreatwood,
            BlocksTC.stairsSilverwood, BlocksTC.tableWood, BlocksTC.taintFibre, BlocksTC.taintLog,
            TABlocks.VIS_REGENERATOR).addAll(BlocksTC.banners.values()).build();
    
    public ItemPrimalCutter() {
        super(3.0F, -2.4F, MATERIAL, EFFECTIVE);
        setHasSubtypes(true);
    }
    
    @Override
    public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
        Material mat = state.getMaterial();
        return mat != Material.LEAVES && mat != Material.PLANTS;
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        Material mat = state.getMaterial();
        return mat == Material.GOURD || mat == Material.LEAVES || mat == Material.VINE || mat == Material.WEB ||
                mat == Material.WOOD ? efficiency : super.getDestroySpeed(stack, state);
    }
    
    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return TOOL_CLASSES;
    }
    
    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (!target.getEntityWorld().isRemote && (!(target instanceof EntityPlayer) ||
                FMLCommonHandler.instance().getMinecraftServerInstance().isPVPEnabled())) {
            target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60));
            target.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 120));
        }
        
        stack.damageItem(1, attacker);
        return true;
    }
    
    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, world, entity, itemSlot, isSelected);
        if (stack.isItemDamaged() && entity != null && entity.ticksExisted % 20 == 0 && entity instanceof EntityLivingBase)
            stack.damageItem(-1, (EntityLivingBase) entity);
    }
    
    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return OreDictionary.containsMatch(false, OreDictionary.getOres("ingotVoid"), repair);
    }
    
    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }
    
    protected boolean preventDrawing(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        return stack.getTagCompound().getBoolean("drawingDisabled");
    }
    
    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return preventDrawing(stack) ? 0 : 72000;
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!preventDrawing(stack)) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        else
            return super.onItemRightClick(world, player, hand);
    }
    
    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() == newStack.getItem() && !preventDrawing(newStack);
    }
    
    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }
    
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }
    
    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        super.onUsingTick(stack, player, count);
        for (Entity target : player.getEntityWorld().getEntitiesWithinAABBExcludingEntity(player,
                player.getEntityBoundingBox().grow(5.0, 4.5, 5.0))) {
            
            if (!(target instanceof EntityPlayer) && !target.isDead && (target instanceof EntityLivingBase ||
                    target instanceof EntityItem || target instanceof EntityFallingBlock || target instanceof EntityBoat || target instanceof EntityMinecart ||
                    target instanceof IProjectile || target instanceof EntityTNTPrimed) && 
                    !target.getRecursivePassengersByType(EntityLivingBase.class).contains(player)) {
                Vec3d playerVector = player.getPositionVector();
                Vec3d targetVector = target.getPositionVector();
                
                double dist = playerVector.distanceTo(targetVector) + 0.1;
                Vec3d difference = targetVector.subtract(playerVector);
                
                target.motionX -= difference.x / 2.5 / dist;
                target.motionY -= difference.y / 2.5 / dist;
                target.motionZ -= difference.z / 2.5 / dist;
            }
        }
        
        if (count % 10 == 0) {
            stack.damageItem(1, player);
            if (count % 20 == 0) {
                player.playSound(SoundsTC.craftfail, 0.5F, 0.35F + player.getEntityWorld().rand.nextFloat() * 0.15F);
            }
        }
        
        if (!player.getEntityWorld().isRemote) {
            TANetwork.INSTANCE.sendToAllTracking(new PacketParticleEffect(ParticleEffect.SMOKE_SPIRAL,
                    player.posX, player.posY, player.posZ, player.width / 2.0, 
                    player.getEntityWorld().rand.nextInt(360), player.posY - 1.0, 0x221F2F), player);
        }
        else {
            ThaumicAugmentation.proxy.getRenderHelper().renderSmokeSpiral(player.world, player.posX, player.posY, player.posZ, player.width / 2.0F, 
                    player.getEntityWorld().rand.nextInt(360), (int) player.posY - 1, 0x221F2F);
        }
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == TAItems.CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
            ItemStack stack = new ItemStack(this);
            EnumInfusionEnchantment.addInfusionEnchantment(stack, EnumInfusionEnchantment.ARCING, 2);
            EnumInfusionEnchantment.addInfusionEnchantment(stack, EnumInfusionEnchantment.BURROWING, 1);
            items.add(stack);
            stack = stack.copy();
            if (!stack.hasTagCompound())
                stack.setTagCompound(new NBTTagCompound());
            
            stack.getTagCompound().setBoolean("drawingDisabled", true);
            items.add(stack);
        }
    }
    
    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }
    
    @Override
    public int getWarp(ItemStack stack, EntityPlayer player) {
        return 2;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName().toString(), "inventory"));
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(new TextComponentTranslation("enchantment.special.sapgreat").setStyle(new Style().setColor(TextFormatting.GOLD)).getFormattedText());
        super.addInformation(stack, world, tooltip, flag);
        if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("drawingDisabled"))
            tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.drawing_disabled").setStyle(new Style().setColor(TextFormatting.RED)).getFormattedText());
    }
    
}
