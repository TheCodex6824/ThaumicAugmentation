package thecodex6824.thaumicaugmentation.common.container;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public interface IAugmentableItemSlotListener {

	public void onAugmentableItemSlotChanged(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack);
	
	public ItemStack getAugmentableItem();
	
}
