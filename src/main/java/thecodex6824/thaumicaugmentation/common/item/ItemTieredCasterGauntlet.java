/**
 *	Thaumic Augmentation
 *	Copyright (c) 2019 TheCodex6824.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.casters.CasterTriggerRegistry;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.ICaster;
import thaumcraft.api.casters.IFocusBlockPicker;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.api.casters.IInteractWithCaster;
import thaumcraft.api.items.IArchitect;
import thaumcraft.api.items.IWarpingGear;
import thaumcraft.common.items.casters.CasterManager;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.items.casters.foci.FocusEffectExchange;
import thaumcraft.common.lib.network.misc.PacketAuraToClient;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.world.aura.AuraChunk;
import thaumcraft.common.world.aura.AuraHandler;
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.item.IDyeableItem;
import thecodex6824.thaumicaugmentation.api.item.ITieredCaster;
import thecodex6824.thaumicaugmentation.common.item.foci.FocusEffectExchangeCompat;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class ItemTieredCasterGauntlet extends ItemTABase implements IArchitect, IDyeableItem, ITieredCaster, IWarpingGear {

	protected static final DecimalFormat VIS_FORMATTER = new DecimalFormat("#######.#");
	protected static final Method CASTER_IS_ON_COOLDOWN;
	
	protected static final float EPSILON = 1E-5F;
	
	static {
		Method cooldown = null;
		try {
			cooldown = CasterManager.class.getDeclaredMethod("isOnCooldown", EntityLivingBase.class);
			cooldown.setAccessible(true);
		}
		catch (Exception ex) {
			// just to give the exception a bit more context than a random reflection error
			FMLCommonHandler.instance().raiseException(ex, "Failed to access Thaumcraft's CasterManager#isOnCooldown", true);
		}
		
		CASTER_IS_ON_COOLDOWN = cooldown;
	}
	
	public ItemTieredCasterGauntlet(String name) {
		super(name, new String[] {"thaumium", "void"});
		setMaxStackSize(1);
		addPropertyOverride(new ResourceLocation("focus"), new IItemPropertyGetter() {
			@Override
			public float apply(ItemStack stack, World world, EntityLivingBase entity) {
				return isStoringFocus(stack) ? 1.0F : 0.0F;
			}
		});
	}
	
	@Override
	public boolean consumeVis(ItemStack stack, EntityPlayer user, float amount, boolean crafting, boolean simulate) {
		amount *= getConsumptionModifier(stack, user, crafting);
		if (stack.getMetadata() == 0 || TAConfig.voidseerArea <= 1) {
			if (amount <= AuraHelper.getVis(user.getEntityWorld(), user.getPosition())) {
				amount -= AuraHelper.drainVis(user.getEntityWorld(), user.getPosition(), amount, simulate);
				return amount <= 0.0F;
			}
			
			return false;
		}
		else {
			int validChunks = 0;
			float totalVis = 0.0F;
			for (int x = -TAConfig.voidseerArea / 2; x < (int) Math.ceil(TAConfig.voidseerArea / 2); ++x) {
				for (int z = -TAConfig.voidseerArea / 2; z < (int) Math.ceil(TAConfig.voidseerArea / 2); ++z) {
					BlockPos loc = user.getPosition().add(x * 16, 0, z * 16);
					if (user.getEntityWorld().isBlockLoaded(loc, true)) {
						totalVis += AuraHelper.getVis(user.getEntityWorld(), loc);
						++validChunks;
					}
				}
			}
			
			if (totalVis >= amount) {
				float toRemove = amount / validChunks;
				for (int x = -TAConfig.voidseerArea / 2; x < (int) Math.ceil(TAConfig.voidseerArea / 2); ++x) {
					for (int z = -TAConfig.voidseerArea / 2; z < (int) Math.ceil(TAConfig.voidseerArea / 2); ++z) {
						BlockPos loc = user.getPosition().add(x * 16, 0, z * 16);
						if (user.getEntityWorld().isBlockLoaded(loc, true)) {
							amount -= AuraHelper.drainVis(user.getEntityWorld(), loc, Math.min(amount, toRemove), simulate);
							// epislon is used here as sometimes the amount may be really close to but not below 0
							if (amount <= 0.0F + EPSILON)
								return true;
						}
					}
				}
			}
			
			return amount <= 0.0F + EPSILON;
		}
	}
	
	@Override
	public float getConsumptionModifier(ItemStack stack, EntityPlayer user, boolean crafting) {
		float baseModifier = 1.0F;
		if (user != null)
			baseModifier -= CasterManager.getTotalVisDiscount(user);	
		
		if (stack.getItem() == this)
			baseModifier -= getCasterVisDiscount(stack);
		
		return Math.max(baseModifier, 0.1F);
	}
	
	@Override
	public Item getFocus(ItemStack stack) {
		ItemStack focusStack = getFocusStack(stack);
		if (focusStack != null && !focusStack.isEmpty())
			return focusStack.getItem();
		
		return null;
	}
	
	@Override
	public ItemStack getFocusStack(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("storedFocus"))
			return new ItemStack(stack.getTagCompound().getCompoundTag("storedFocus"));
		
		return null;
	}
	
	protected boolean isStoringFocus(ItemStack stack) {
		return stack.getItem() instanceof ICaster && ((ICaster) stack.getItem()).getFocus(stack) instanceof ItemFocus;
	}
		
	@Override
	public void setFocus(ItemStack stack, ItemStack focus) {
		if (focus == null || focus.isEmpty())
			stack.getTagCompound().removeTag("storedFocus");
		else
			stack.setTagInfo("storedFocus", focus.writeToNBT(new NBTTagCompound()));
		
	}
	
	@Override
	public float getCasterVisDiscount(ItemStack stack) {
		if (stack.getItem() == this) {
			switch (stack.getMetadata()) {
				case 0: return (float) TAConfig.gauntletVisDiscounts[0];
				case 1: return (float) TAConfig.gauntletVisDiscounts[1];
			}
		}
		
		return 0.0F;
	}
	
	@Override
	public float getCasterCooldownModifier(ItemStack stack) {
		if (stack.getItem() == this) {
			switch (stack.getMetadata()) {
				case 0: return (float) TAConfig.gauntletCooldownModifiers[0];
				case 1: return (float) TAConfig.gauntletCooldownModifiers[1];
			}
		}
		
		return 1.0F;
	}
	
	@Override
	public ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
		if (isStoringFocus(stack)) {
			FocusPackage fPackage = ItemFocus.getPackage(getFocusStack(stack));
			if (fPackage != null) {
				for (IFocusElement element : fPackage.nodes) {
					if (element instanceof IArchitect)
						return ((IArchitect) element).getArchitectBlocks(stack, world, pos, side, player);
				}
			}
		}
		
		return null;
	}
	
	@Override
	public RayTraceResult getArchitectMOP(ItemStack stack, World world, EntityLivingBase user) {
		if (isStoringFocus(stack)) {
			FocusPackage fPackage = ItemFocus.getPackage(getFocusStack(stack));
			if (fPackage != null && FocusEngine.doesPackageContainElement(fPackage, "thaumcraft.PLAN"));
				return ((IArchitect) FocusEngine.getElement("thaumcraft.PLAN")).getArchitectMOP(stack, world, user);
		}
		
		return null;
	}
	
	@Override
	public ItemStack getPickedBlock(ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return ItemStack.EMPTY;
		
		ItemStack ret = null;
		if (isStoringFocus(stack)) {
			ItemStack focus = getFocusStack(stack);
			FocusPackage fPackage = ItemFocus.getPackage(focus);
			if (fPackage != null && focus.hasTagCompound() && focus.getTagCompound().hasKey("pickedBlock", NBT.TAG_COMPOUND)) {
				for (IFocusElement element : fPackage.nodes) {
					if (element instanceof IFocusBlockPicker) {
						ret = ItemStack.EMPTY;
						try {
							ret = new ItemStack(focus.getTagCompound().getCompoundTag("pickedBlock"));
						}
						catch (Exception rip) {}
					}
				}
			}
		}
		
		return ret;
	}
	
	@Override
	public boolean showAxis(ItemStack stack, World world, EntityPlayer player, EnumFacing side, EnumAxis axis) {
		if (isStoringFocus(stack)) {
			FocusPackage fPackage = ItemFocus.getPackage(getFocusStack(stack));
			if (fPackage != null) {
				for (IFocusElement element : fPackage.nodes) {
					if (element instanceof IArchitect) {
						return ((IArchitect) element).showAxis(stack, world, player, side, axis);
					}
				}
			} 
		}
		
		return false;
	}
	
	@Override
	public int getWarp(ItemStack stack, EntityPlayer player) {
		return stack.getItem() == this && stack.getMetadata() == 1 ? 2 : 0;
	}
	
	@Override
	public int getDefaultDyedColorForMeta(int meta) {
		switch (meta) {
			case 0: return 0x008EFF;
			case 1: return 0x6A3880;
			default: return 0xFFFFFFFF;
		}
	}
	
	@Override
	public int getDyedColor(ItemStack stack) {
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		if (stack.getTagCompound().hasKey("color", NBT.TAG_INT))
			return stack.getTagCompound().getInteger("color");
		else {
			stack.getTagCompound().setInteger("color", getDefaultDyedColorForMeta(stack.getMetadata()));
			return stack.getTagCompound().getInteger("color");
		}
	}
	
	@Override
	public void setDyedColor(ItemStack stack, int color) {
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		stack.getTagCompound().setInteger("color", color);
	}
	
	@Override
	public boolean useBlockHighlight(ItemStack stack) {
		return false;
	}
	
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
	    if (oldStack.getItem() instanceof ICaster && newStack.getItem() instanceof ICaster && isStoringFocus(oldStack) && isStoringFocus(newStack)) {
	    	ItemStack oldFocus = ((ICaster) oldStack.getItem()).getFocusStack(oldStack);
	    	ItemStack newFocus = ((ICaster) newStack.getItem()).getFocusStack(newStack);
	    	return ((oldFocus == null && newFocus != null) || (oldFocus != null && newFocus == null) || ((ItemFocus) oldFocus.getItem()).getSortingHelper(oldFocus).hashCode() != 
	    			((ItemFocus) newFocus.getItem()).getSortingHelper(newFocus).hashCode());
	    }
	    else
	    	return oldStack.getItem() != newStack.getItem() || oldStack.getMetadata() != newStack.getMetadata();
	}
	
	@Override
	public EnumRarity getRarity(ItemStack stack) {
		switch (stack.getMetadata()) {
			case 0: return EnumRarity.RARE;
			case 1: return EnumRarity.EPIC;
			default: return EnumRarity.UNCOMMON;
		}
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if (!world.isRemote && entity.ticksExisted % 10 == 0 && entity instanceof EntityPlayerMP) {
			for (ItemStack held : entity.getHeldEquipment()) {
				if (held != null && !held.isEmpty() && held.getItem() instanceof ICaster) {
					// TODO replace with something that doesn't hijack internal thaumcraft stuff
					AuraChunk chunk = AuraHandler.getAuraChunk(world.provider.getDimension(), entity.chunkCoordX, entity.chunkCoordZ);
					if (chunk != null)
						TANetwork.INSTANCE.sendTo(new PacketAuraToClient(chunk != null ? chunk : new AuraChunk(null, (short) 0, 0.0F, 0.0F)), 
								(EntityPlayerMP) entity);
				}
			}
		}
	}
	
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
			float hitY, float hitZ, EnumHand hand) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof IInteractWithCaster && 
				((IInteractWithCaster) state.getBlock()).onCasterRightClick(world, player.getHeldItem(hand), player, pos, side, hand))
			return EnumActionResult.PASS;
		
		ItemStack stack = player.getHeldItem(hand);
		if (state.getBlock() == Blocks.CAULDRON && state.getValue(BlockCauldron.LEVEL) > 0 && 
				getDyedColor(stack) != getDefaultDyedColorForMeta(stack.getMetadata())) {
			setDyedColor(stack, getDefaultDyedColorForMeta(stack.getMetadata()));
			world.setBlockState(pos, state.withProperty(BlockCauldron.LEVEL, state.getValue(BlockCauldron.LEVEL) - 1));
			world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 0.5F, 1.0F);
			return EnumActionResult.SUCCESS;
		}
		
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof IInteractWithCaster && 
				((IInteractWithCaster) tile).onCasterRightClick(world, stack, player, pos, side, hand))
			return EnumActionResult.PASS;
		
		if (CasterTriggerRegistry.hasTrigger(state))
			return CasterTriggerRegistry.performTrigger(world, stack, player, pos, side, state) ? 
					EnumActionResult.SUCCESS : EnumActionResult.FAIL;
		
		if (isStoringFocus(stack)) {
			ItemStack focus = getFocusStack(stack);
			FocusPackage fPackage = ItemFocus.getPackage(focus);
			if (fPackage != null) {
				for (IFocusElement element : fPackage.nodes) {
					if (element instanceof IFocusBlockPicker && player.isSneaking() && world.getTileEntity(pos) == null) {
						if (!world.isRemote) {
							ItemStack toStore = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
							try {
								if (state.getBlock() != Blocks.AIR) {
									ItemStack toCopy = BlockUtils.getSilkTouchDrop(state);
									if (toCopy != null && !toCopy.isEmpty())
										toStore = toCopy.copy();
								}
							}
							catch (Exception oof) {}
							focus.getTagCompound().setTag("pickedBlock", toStore.writeToNBT(new NBTTagCompound()));
							return EnumActionResult.SUCCESS;
						}
						else {
							player.swingArm(hand);
							return EnumActionResult.PASS;
						}
					}
				}
			}  
		}
		
		return EnumActionResult.PASS;
	}
	
	protected boolean isCasterOnCooldown(EntityLivingBase entity) {
		try {
			return (Boolean) CASTER_IS_ON_COOLDOWN.invoke(null, entity);
		}
		catch (InvocationTargetException | IllegalAccessException ex) {
			FMLCommonHandler.instance().raiseException(ex, "Failed to invoke Thaumcraft's CasterManager#isOnCooldown", true);
			
			// this shouldn't return, but java gets angry if it's not here so yeah
			return true;
		}
	}
	
	protected void fixFoci(FocusPackage p) {
		for (int i = 0; i < p.nodes.size(); ++i) {
			if (p.nodes.get(i).getClass().equals(FocusEffectExchange.class))
				p.nodes.set(i, new FocusEffectExchangeCompat((FocusEffectExchange) p.nodes.get(i)));
		}
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (isStoringFocus(player.getHeldItem(hand)) && !isCasterOnCooldown(player)) {
			ItemStack focus = getFocusStack(player.getHeldItem(hand));
			CasterManager.setCooldown(player, (int) (((ItemFocus) focus.getItem()).getActivationTime(focus) * getCasterCooldownModifier(player.getHeldItem(hand))));
			FocusPackage core = ItemFocus.getPackage(focus);
	        if (player.isSneaking()) {
	            for (IFocusElement element : core.nodes) {
	            	if (element instanceof IFocusBlockPicker && player.isSneaking())
	            		return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	            }
	        }
	        
	        if (world.isRemote)
	        	return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	        
	        if (consumeVis(player.getHeldItem(hand), player, ((ItemFocus) focus.getItem()).getVisCost(focus), false, false)) {
	        	FocusPackage copy = core.copy(player);
	        	fixFoci(copy);
	        	FocusEngine.castFocusPackage(player, copy, true);
	    	    player.swingArm(hand);
	    	    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	        }
	       
	        return new ActionResult<ItemStack>(EnumActionResult.FAIL, player.getHeldItem(hand));
		}
		
		return super.onItemRightClick(world, player, hand);
	}
	
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (tab == TAItems.CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
			if (subItemNames.length > 0) {
				for (int i = 0; i < subItemNames.length; ++i) {
					ItemStack toAdd = new ItemStack(this, 1, i);
					toAdd.setTagCompound(new NBTTagCompound());
					setDyedColor(toAdd, getDefaultDyedColorForMeta(i));
					items.add(toAdd);
				}
			}
			else
				super.getSubItems(tab, items);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D() {
		return true;
	}	
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		if (isStoringFocus(stack)) {
			ItemStack focus = getFocusStack(stack);
			float visCost = ((ItemFocus) focus.getItem()).getVisCost(focus);
			if (visCost > 0.0F)
				tooltip.add(TextFormatting.ITALIC + "" + TextFormatting.AQUA + new TextComponentTranslation("tc.vis.cost").getFormattedText() + 
						" " + TextFormatting.RESET + VIS_FORMATTER.format(visCost) + " " + new TextComponentTranslation("item.Focus.cost1").getFormattedText());
			
			tooltip.add(TextFormatting.BOLD + "" + TextFormatting.ITALIC + "" + TextFormatting.GREEN + focus.getItem().getItemStackDisplayName(focus));
			((ItemFocus) focus.getItem()).addFocusInformation(focus, world, tooltip, flag);
		}
	}
	
}
