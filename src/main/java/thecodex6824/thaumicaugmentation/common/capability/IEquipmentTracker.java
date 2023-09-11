package thecodex6824.thaumicaugmentation.common.capability;

import it.unimi.dsi.fastutil.ints.IntCollection;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public interface IEquipmentTracker {

	public ItemStack getRecordedEquipmentStack(int index);
	
	public void recordEquipmentStack(int index, ItemStack stack);
	
	public IItemHandler getLiveEquipment();
	
	public boolean hasAugments();
	
	public IntCollection getAugmentSlots();
	
}
