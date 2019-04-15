package thecodex6824.thaumicaugmentation.common.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.IForgeRegistryEntry;

import thecodex6824.thaumicaugmentation.common.item.ItemKey;

public class AuthorizedKeyCreationRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
	
	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}
	
	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		boolean hasIronKey = false;
		boolean hasBrassKey = false;
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null && !stack.isEmpty()) {
				if (stack.getItem() instanceof ItemKey) {
					if (stack.getMetadata() == 0 && !hasIronKey && !stack.hasTagCompound())
						hasIronKey = true;
					else if (stack.getMetadata() == 1 && !hasBrassKey && stack.hasTagCompound() && 
							stack.getTagCompound().hasKey("boundTo", NBT.TAG_STRING))
						hasBrassKey = true;
					else
						return false;
				}
				else
					return false;
			}
		}
		
		return hasIronKey && hasBrassKey;
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack ironKey = null;
		ItemStack brassKey = null;
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null && !stack.isEmpty()) {
				if (stack.getItem() instanceof ItemKey) {
					if (stack.getMetadata() == 0 && ironKey == null)
						ironKey = stack;
					else if (stack.getMetadata() == 1 && brassKey == null)
						brassKey = stack;
					else
						return ItemStack.EMPTY;
				}
				else
					return ItemStack.EMPTY;
			}
		}
		
		if (ironKey != null && brassKey != null) {
			ItemStack output = ironKey.copy();
			((ItemKey) output.getItem()).setBoundTo(output, brassKey.getTagCompound().getString("boundToDisplay"), 
					brassKey.getTagCompound().getString("boundTo"));
			
			return output;
		}
		else
			return ItemStack.EMPTY;
	}
	
	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}
	
}
