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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.items.IWarpingGear;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.item.CapabilityMorphicTool;
import thecodex6824.thaumicaugmentation.api.item.IMorphicTool;
import thecodex6824.thaumicaugmentation.common.capability.CapabilityProviderMorphicTool;
import thecodex6824.thaumicaugmentation.common.capability.MorphicTool;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

public class ItemMorphicTool extends ItemTABase implements IWarpingGear {

    private static final IMorphicTool NULL_TOOL = new IMorphicTool() {
        
        @Override
        public NBTTagCompound serializeNBT() {
            return null;
        }
        
        @Override
        public void deserializeNBT(NBTTagCompound nbt) {}
        
        @Override
        public void setFunctionalStack(ItemStack stack) {}
        
        @Override
        public void setDisplayStack(ItemStack stack) {}
        
        @Override
        public ItemStack getFunctionalStack() {
            return ItemStack.EMPTY;
        }
        
        @Override
        public ItemStack getDisplayStack() {
            return ItemStack.EMPTY;
        }
    };
    
    public ItemMorphicTool() {
        super();
        setMaxStackSize(1);
    }
    
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        CapabilityProviderMorphicTool tool = new CapabilityProviderMorphicTool(new MorphicTool());
        if (nbt != null && nbt.hasKey("Parent", NBT.TAG_COMPOUND))
            tool.deserializeNBT(nbt.getCompoundTag("Parent"));
        
        return tool;
    }
    
    private IMorphicTool getTool(ItemStack stack) {
        return stack.hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null) ?
                stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null) : NULL_TOOL;
    }
    
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        NBTTagCompound tag = new NBTTagCompound();
        if (stack.hasTagCompound())
            tag.setTag("item", stack.getTagCompound().copy());
        
        tag.setTag("cap", stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).serializeNBT());
        return tag;
    }
    
    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        if (nbt != null) {
            if (nbt.hasKey("cap", NBT.TAG_COMPOUND))
                stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).deserializeNBT(nbt.getCompoundTag("cap"));
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
    public int getWarp(ItemStack stack, EntityPlayer player) {
        ItemStack func = getTool(stack).getFunctionalStack();
        if (func.getItem() instanceof IWarpingGear)
            return Math.max(((IWarpingGear) func.getItem()).getWarp(func, player) - 1, 0);
        else
            return 0;
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }
    
    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        if (oldStack.hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null))
            oldStack = getTool(oldStack).getFunctionalStack();
        if (newStack.hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null))
            newStack = getTool(newStack).getFunctionalStack();
        
        return oldStack.getItem().canContinueUsing(oldStack, newStack);
    }
    
    @Override
    public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().canDestroyBlockInCreative(world, pos, func, player);
    }
    
    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, EntityLivingBase entity,
            EntityLivingBase attacker) {
        
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().canDisableShield(func, shield, entity, attacker);
    }
    
    @Override
    public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().canHarvestBlock(state, func);
    }
    
    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().doesSneakBypassUse(func, world, pos, player);
    }
    
    @Override
    public ImmutableMap<String, ITimeValue> getAnimationParameters(ItemStack stack, World world,
            EntityLivingBase entity) {
        
        ItemStack display = getTool(stack).getDisplayStack();
        return display.getItem().getAnimationParameters(display, world, entity);
    }
    
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().getAttributeModifiers(slot, func);
    }
    
    @Override
    public int getDamage(ItemStack stack) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().getDamage(func);
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().getDestroySpeed(func, state);
    }
    
    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().getDurabilityForDisplay(func);
    }
    
    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().getHarvestLevel(func, toolClass, player, blockState);
    }
    
    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }
    
    @Override
    public int getItemBurnTime(ItemStack itemStack) {
        return 0;
    }
    
    @Override
    public int getItemEnchantability(ItemStack stack) {
        return 0;
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        ItemStack display = getTool(stack).getDisplayStack();
        if (display.isEmpty())
            return super.getItemStackDisplayName(stack);
        else
            return display.getItem().getItemStackDisplayName(display);
    }
    
    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        ItemStack display = getTool(stack).getDisplayStack();
        return display.getItem().getItemUseAction(display);
    }
    
    @Override
    public int getMaxDamage(ItemStack stack) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().getMaxDamage(func);
    }
    
    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().getMaxItemUseDuration(func);
    }
    
    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().getRGBDurabilityForDisplay(func);
    }
    
    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().getToolClasses(func);
    }
    
    @Override
    public String getTranslationKey(ItemStack stack) {
        ItemStack display = getTool(stack).getDisplayStack();
        if (display.isEmpty())
            return super.getTranslationKey(stack);
        else
            return display.getItem().getTranslationKey(display);
    }
    
    @Override
    public String getUnlocalizedNameInefficiently(ItemStack stack) {
        ItemStack display = getTool(stack).getDisplayStack();
        if (display.isEmpty())
            return super.getUnlocalizedNameInefficiently(stack);
        else
            return display.getItem().getUnlocalizedNameInefficiently(display);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().hasEffect(func);
    }
    
    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().hitEntity(func, target, attacker);
    }
    
    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }
    
    @Override
    public boolean isDamageable() {
        return true;
    }
    
    @Override
    public boolean isDamaged(ItemStack stack) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().isDamaged(func);
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }
    
    @Override
    public boolean isRepairable() {
        return false;
    }
    
    @Override
    public boolean isShield(ItemStack stack, @Nullable EntityLivingBase entity) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().isShield(func, entity);
    }
    
    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target,
            EnumHand hand) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().itemInteractionForEntity(func, playerIn, target, hand);
    }
    
    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos,
            EntityLivingBase entityLiving) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().onBlockDestroyed(func, worldIn, state, pos, entityLiving);
    }
    
    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        ItemStack func = getTool(itemstack).getFunctionalStack();
        return func.getItem().onBlockStartBreak(func, pos, player);
    }
    
    @Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        ItemStack func = getTool(item).getFunctionalStack();
        return func.getItem().onDroppedByPlayer(func, player);
    }
    
    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().onEntitySwing(entityLiving, func);
    }
    
    // setStackInSlot in EntityPlayer plays a sound client-side when setting stacks
    private static void setStackWithoutAnnoyingNoise(EntityLivingBase entity, EnumHand hand, ItemStack stack) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (hand == EnumHand.MAIN_HAND)
                player.inventory.mainInventory.set(player.inventory.currentItem, stack);
            else if (hand == EnumHand.OFF_HAND)
                player.inventory.offHandInventory.set(0, stack);
        }
        else
            entity.setHeldItem(hand, stack);
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack func = getTool(playerIn.getHeldItem(handIn)).getFunctionalStack();
        if (func.isEmpty())
            return super.onItemRightClick(worldIn, playerIn, handIn);
        else {
            // UGLY HACK TIME!
            // we can't actually pass a stack and instead just rely on the hand, so fake it for the forwarded call
            ItemStack old = playerIn.getHeldItem(handIn);
            setStackWithoutAnnoyingNoise(playerIn, handIn, func);
            ActionResult<ItemStack> innerResult = func.getItem().onItemRightClick(worldIn, playerIn, handIn);
            old.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).setFunctionalStack(innerResult.getResult());
            ActionResult<ItemStack> result = new ActionResult<>(innerResult.getType(), old);
            setStackWithoutAnnoyingNoise(playerIn, handIn, old);
            if (result.getType() == EnumActionResult.SUCCESS && playerIn.getActiveHand() != null) {
                playerIn.resetActiveHand();
                playerIn.setActiveHand(playerIn.getActiveHand());
            }
            
            return result;
        }
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        
        ItemStack func = getTool(player.getHeldItem(hand)).getFunctionalStack();
        if (func.isEmpty())
            return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        else {
            ItemStack old = player.getHeldItem(hand);
            setStackWithoutAnnoyingNoise(player, hand, func);
            EnumActionResult result = func.getItem().onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
            old.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).setFunctionalStack(player.getHeldItem(hand));
            setStackWithoutAnnoyingNoise(player, hand, old);
            return result;
        }
    }
   
    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        ItemStack func = getTool(stack).getFunctionalStack();
        ItemStack ret = func.getItem().onItemUseFinish(func, worldIn, entityLiving);
        stack.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).setFunctionalStack(ret);
        return stack;
    }
    
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
            float hitY, float hitZ, EnumHand hand) {
        
        ItemStack func = getTool(player.getHeldItem(hand)).getFunctionalStack();
        if (func.isEmpty())
            return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
        else {
            ItemStack old = player.getHeldItem(hand);
            setStackWithoutAnnoyingNoise(player, hand, func);
            EnumActionResult result = func.getItem().onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
            old.getCapability(CapabilityMorphicTool.MORPHIC_TOOL, null).setFunctionalStack(player.getHeldItem(hand));
            setStackWithoutAnnoyingNoise(player, hand, old);
            return result;
        }
    }
    
    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        ItemStack func = getTool(stack).getFunctionalStack();
        func.getItem().onPlayerStoppedUsing(func, worldIn, entityLiving, timeLeft);
    }
    
    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        ItemStack func = getTool(stack).getFunctionalStack();
        func.getItem().onUpdate(func, worldIn, entityIn, itemSlot, isSelected);
    }
    
    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        ItemStack func = getTool(stack).getFunctionalStack();
        func.getItem().onUsingTick(func, player, count);
    }
    
    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().onLeftClickEntity(func, player, entity);
    }
    
    @Override
    public void setDamage(ItemStack stack, int damage) {
        // don't worry, this is painful to read for me too
        // but vanilla decides to reset the metadata after the onItemUse call for creative mode players only (?)
        // which will mess up the internal stack's meta if this check isn't here
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace.length < 4 || (!trace[3].getClassName().equals("net.minecraft.server.management.PlayerInteractionManager") &&
                !trace[3].getClassName().equals("net.minecraft.client.multiplayer.PlayerControllerMP"))) {
            ItemStack func = getTool(stack).getFunctionalStack();
            func.getItem().setDamage(func, damage);
        }
    }
    
    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        if (oldStack.hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null))
            oldStack = getTool(oldStack).getFunctionalStack();
        if (newStack.hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null))
            newStack = getTool(newStack).getFunctionalStack();
        
        return oldStack.getItem().shouldCauseBlockBreakReset(oldStack, newStack);
    }
    
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null))
            oldStack = getTool(oldStack).getFunctionalStack();
        if (newStack.hasCapability(CapabilityMorphicTool.MORPHIC_TOOL, null))
            newStack = getTool(newStack).getFunctionalStack();
        
        return oldStack.getItem().shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }
    
    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        ItemStack func = getTool(stack).getFunctionalStack();
        return func.getItem().showDurabilityBar(func);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        ItemStack func = getTool(stack).getFunctionalStack();
        func.getItem().addInformation(func, worldIn, tooltip, flagIn);
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {}
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(
                "ta_special:morphic_tool", "inventory"));
    }
    
}
