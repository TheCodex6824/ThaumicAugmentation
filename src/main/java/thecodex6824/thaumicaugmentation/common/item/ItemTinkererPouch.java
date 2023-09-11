package thecodex6824.thaumicaugmentation.common.item;

import java.lang.reflect.Field;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.IForgeRegistryEntry;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.items.casters.ItemFocusPouch;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.TAMaterials;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.common.capability.CustomSyncBaubleItem;
import thecodex6824.thaumicaugmentation.common.capability.provider.CapabilityProviderAugmentFocusBag;
import thecodex6824.thaumicaugmentation.common.integration.IntegrationHandler;
import thecodex6824.thaumicaugmentation.common.util.IModelProvider;
import thecodex6824.thaumicaugmentation.init.GUIHandler.TAInventory;
import vazkii.botania.api.item.IPhantomInkable;

@Optional.Interface(iface = "vazkii.botania.api.item.IPhantomInkable", modid = IntegrationHandler.BOTANIA_MOD_ID)
public class ItemTinkererPouch extends ItemFocusPouch implements IModelProvider<Item>, IPhantomInkable {

	private static final int NUM_FOCI_SLOTS = 18;
	private static final int NUM_AUGMENT_SLOTS = 9;
	
	public ItemTinkererPouch() {
		// ItemFocusPouch sets the registry name, and forge intentionally crashes if we call setRegistryName again
		// thank you forge, very cool
		try {
			Field nameField = IForgeRegistryEntry.Impl.class.getDeclaredField("registryName");
			nameField.setAccessible(true);
			nameField.set(this, null);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
        // clean up after TC's base item class doing stuff
        setRegistryName("tinkerer_pouch");
        setTranslationKey(ThaumicAugmentationAPI.MODID + ".tinkerer_pouch");
        setCreativeTab(TAItems.CREATIVE_TAB);
        ConfigItems.ITEM_VARIANT_HOLDERS.remove(this);
    }
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		CapabilityProviderAugmentFocusBag provider = new CapabilityProviderAugmentFocusBag(new ItemStackHandler(NUM_FOCI_SLOTS + NUM_AUGMENT_SLOTS) {
			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				return slot < NUM_FOCI_SLOTS ? stack.getItem() instanceof ItemFocus :
					stack.hasCapability(CapabilityAugment.AUGMENT, null);
			}
		}, new CustomSyncBaubleItem(BaubleType.BELT) {
			@Override
			public NBTTagCompound getSyncNBT(boolean forOwner) {
				if (forOwner) {
					IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
					if (inv instanceof ItemStackHandler) {
						return ((ItemStackHandler) inv).serializeNBT();
					}
				}
				
				return new NBTTagCompound();
			}
			
			@Override
			public void readSyncNBT(NBTTagCompound tag) {
				if (!tag.isEmpty()) {
					IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
					if (inv instanceof ItemStackHandler) {
						((ItemStackHandler) inv).deserializeNBT(tag);
					}
				}
			}
		});
        
        if (nbt != null && nbt.hasKey("Parent", NBT.TAG_COMPOUND)) {
            provider.deserializeNBT(nbt.getCompoundTag("Parent"));
        }
        
        return provider;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (!world.isRemote && !player.isSneaking()) {
			player.openGui(ThaumicAugmentation.instance, TAInventory.TINKERER_POUCH.getID(), world,
					(int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ));
			return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
		}
		
		return super.onItemRightClick(world, player, hand);
	}
	
	@Override
	@Deprecated
	public NonNullList<ItemStack> getInventory(ItemStack stack) {
		IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSlots(), ItemStack.EMPTY);
		for (int i = 0; i < inv.getSlots(); ++i) {
			ret.set(i, inv.getStackInSlot(i).copy());
		}
		
		return ret;
	}
	  
	@Override
	@Deprecated
	public void setInventory(ItemStack stack, NonNullList<ItemStack> stackList) {
		IItemHandlerModifiable inv = (IItemHandlerModifiable) stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		for (int i = 0; i < Math.min(NUM_FOCI_SLOTS, stackList.size()); ++i) {
			inv.setStackInSlot(i, stackList.get(i).copy());
		}
	}

	private IBauble getBaubleCap(ItemStack stack) {
	    return stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null);
	}
	  
	@Override
	public BaubleType getBaubleType(ItemStack stack) {
		return getBaubleCap(stack).getBaubleType(stack);
	}
	  
	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase player) {
		getBaubleCap(stack).onWornTick(stack, player);
	}

	@Override
	public void onEquipped(ItemStack stack, EntityLivingBase player) {
		getBaubleCap(stack).onEquipped(stack, player);
	}
	  
	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase player) {
		getBaubleCap(stack).onUnequipped(stack, player);
	}

	@Override
	public boolean canEquip(ItemStack stack, EntityLivingBase player) {
		return getBaubleCap(stack).canEquip(stack, player);
	}
	  
	@Override
	public boolean canUnequip(ItemStack stack, EntityLivingBase player) {
		return getBaubleCap(stack).canUnequip(stack, player);
	}
	
	@Override
	public boolean willAutoSync(ItemStack stack, EntityLivingBase player) {
		return getBaubleCap(stack).willAutoSync(stack, player);
	}
	
	@Override
    public IRarity getForgeRarity(ItemStack stack) {
        return TAMaterials.RARITY_ARCANE;
    }
    
    @Override
    @Optional.Method(modid = IntegrationHandler.BOTANIA_MOD_ID)
    public boolean hasPhantomInk(ItemStack stack) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound().getBoolean("phantomInk");
        }
        
        return false;
    }
    
    @Override
    @Optional.Method(modid = IntegrationHandler.BOTANIA_MOD_ID)
    public void setPhantomInk(ItemStack stack, boolean ink) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        
        stack.getTagCompound().setBoolean("phantomInk", ink);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
        		new ModelResourceLocation(getRegistryName().toString(), "inventory"));
    }
	
}
