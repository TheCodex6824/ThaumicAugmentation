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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.items.IWarpingGear;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.TAMaterials;
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.util.RaytraceHelper;
import thecodex6824.thaumicaugmentation.client.sound.SoundHandleSpecialSound;
import thecodex6824.thaumicaugmentation.common.util.IModelProvider;
import thecodex6824.thaumicaugmentation.common.util.ISoundHandle;

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
    public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        return false;
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
        if (!target.getEntityWorld().isRemote && checkEntity(attacker, target)) {
            target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60));
            target.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 120));
        }
        
        stack.damageItem(1, attacker);
        return true;
    }
    
    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, world, entity, itemSlot, isSelected);
        if (stack.isItemDamaged() && entity != null && entity.ticksExisted % 20 == 0) {
            stack.attemptDamageItem(-1, world.rand,
                    entity instanceof EntityPlayerMP ? (EntityPlayerMP) entity : null);
        }
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
    
    protected boolean checkEntity(EntityLivingBase user, Entity entity) {
        if (entity.isDead || !entity.isNonBoss())
            return false;
        
        boolean allowed = false;
        if (entity instanceof EntityPlayer && ThaumicAugmentation.proxy.isPvPEnabled())
            allowed = true;
        else if (entity instanceof EntityItem || entity instanceof EntityBoat || entity instanceof EntityMinecart ||
                entity instanceof EntityFallingBlock || entity instanceof EntityTNTPrimed || entity instanceof IProjectile ||
                entity instanceof EntityFireball || entity instanceof EntityXPOrb) {
            
            allowed = true;
        }
        else if (entity instanceof EntityLivingBase && !(entity instanceof EntityArmorStand))
            allowed = true;
        
        if (allowed) {
            Team userTeam = user.getTeam();
            if (userTeam == null || userTeam.getAllowFriendlyFire() ||
                    !userTeam.isSameTeam(entity.getTeam())) {
                
                return true;
            }
        }
        
        return false;
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
    
    protected Vec3d calculateVortexCenter(EntityLivingBase player) {
        if (!player.isSneaking()) {
            double reach = 4.0;
            if (player instanceof EntityPlayer)
                reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
            
            return !player.getEntityWorld().isRemote ?
                    RaytraceHelper.raytracePosition(player, reach, null) :
                    RaytraceHelper.raytracePosition(player, reach, ThaumicAugmentation.proxy.getPartialTicks(), null);
        }
        else
            return player.getPositionVector();
    }
    
    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        super.onUsingTick(stack, player, count);
        Vec3d center = calculateVortexCenter(player);
        for (Entity target : player.getEntityWorld().getEntitiesWithinAABBExcludingEntity(player,
                new AxisAlignedBB(center.x, center.y, center.z, center.x, center.y, center.z).grow(7.5, 7.5, 7.5))) {
            
            if (checkEntity(player, target) && player.canEntityBeSeen(target) &&
                    !target.getRecursivePassengersByType(EntityLivingBase.class).contains(player)) {
                
                Vec3d targetVector = target.getPositionVector();
                double dist = center.distanceTo(targetVector);
                if (dist >= 1.0) {
                    Vec3d difference = targetVector.subtract(center).normalize();
                    double sizeMod = Math.max(Math.sqrt(target.width * target.width * target.height), 1.0);
                    target.motionX -= difference.x / dist / sizeMod;
                    target.motionZ -= difference.z / dist / sizeMod;
                }
            }
        }
        
        if (count % 10 == 0)
            stack.damageItem(1, player);
        
        if (player.getEntityWorld().isRemote) {
            if ((getMaxItemUseDuration(stack) - count) % 190 == 0) {
                final int id = player.getEntityId();
                ISoundHandle handle = ThaumicAugmentation.proxy.playSpecialSound(TASounds.PRIMAL_CUTTER_VORTEX, player.getSoundCategory(), prev -> {
                    Entity entity = Minecraft.getMinecraft().world.getEntityByID(id);
                    if (entity instanceof EntityLivingBase) {
                        ItemStack active = ((EntityLivingBase) entity).getActiveItemStack();
                        if (!active.isEmpty() && active.getItem() == TAItems.PRIMAL_CUTTER)
                            return calculateVortexCenter((EntityLivingBase) entity);
                    }
                    
                    return null;
                }, (float) center.x, (float) center.y, (float) center.z, 0.75F, 1.0F, false, 0);
                if (handle instanceof SoundHandleSpecialSound)
                    ((SoundHandleSpecialSound) handle).setFadeOut(40);
            }
            
            if (player.getEntityWorld().isAirBlock(new BlockPos(center.add(0.0, -0.05, 0.0)))) {
                ThaumicAugmentation.proxy.getRenderHelper().renderSmokeSpiral(player.world, center.x, center.y, center.z, player.width / 2.0F, 
                        player.getEntityWorld().rand.nextInt(360), (int) center.y - 2, 0x221F2F);
                ThaumicAugmentation.proxy.getRenderHelper().renderSmokeSpiral(player.world, center.x, center.y, center.z, player.width * 2.0F, 
                        player.getEntityWorld().rand.nextInt(360), (int) center.y - 3, 0x221F2F);
                ThaumicAugmentation.proxy.getRenderHelper().renderSmokeSpiral(player.world, center.x, center.y, center.z, player.width * 4.0F, 
                        player.getEntityWorld().rand.nextInt(360), (int) center.y - 3, 0x221F2F);
            }
            
            ThaumicAugmentation.proxy.getRenderHelper().renderSmokeSpiral(player.world, center.x, center.y, center.z, player.width / 2.0F, 
                    player.getEntityWorld().rand.nextInt(360), (int) center.y - 1, 0x221F2F);
            ThaumicAugmentation.proxy.getRenderHelper().renderSmokeSpiral(player.world, center.x, center.y, center.z, player.width * 2.0F, 
                    player.getEntityWorld().rand.nextInt(360), (int) center.y, 0x221F2F);
            ThaumicAugmentation.proxy.getRenderHelper().renderSmokeSpiral(player.world, center.x, center.y, center.z, player.width * 4.0F, 
                    player.getEntityWorld().rand.nextInt(360), (int) center.y, 0x221F2F);
        }
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.type == EnumEnchantmentType.WEAPON || super.canApplyAtEnchantingTable(stack, enchantment);
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
    public IRarity getForgeRarity(ItemStack stack) {
        return TAMaterials.RARITY_ELDRITCH;
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
