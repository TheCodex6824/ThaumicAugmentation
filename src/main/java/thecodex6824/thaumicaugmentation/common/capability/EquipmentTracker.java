package thecodex6824.thaumicaugmentation.common.capability;

import java.util.ArrayList;
import java.util.Collections;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugmentableItem;

public abstract class EquipmentTracker implements IEquipmentTracker {

	protected ArrayList<ItemStack> equipment;
	protected IntOpenHashSet augmentSlots;
	
	public EquipmentTracker() {
		this(6);
	}
	
	public EquipmentTracker(int size) {
		equipment = new ArrayList<>(size);
		augmentSlots = new IntOpenHashSet();
		Collections.fill(equipment, ItemStack.EMPTY);
	}
	
	public EquipmentTracker(IInventory initialEquipment) {
		equipment = new ArrayList<>(initialEquipment.getSizeInventory());
		for (int i = 0; i < initialEquipment.getSizeInventory(); ++i) {
			recordEquipmentStack(i, initialEquipment.getStackInSlot(i));
		}
	}
	
	@Override
	public ItemStack getRecordedEquipmentStack(int index) {
		if (index >= equipment.size()) {
			return ItemStack.EMPTY;
		}
		
		return equipment.get(index);
	}
	
	@Override
	public void recordEquipmentStack(int index, ItemStack stack) {
		if (index >= equipment.size()) {
			equipment.ensureCapacity(index + 1);
			do {
				equipment.add(ItemStack.EMPTY);
			} while (equipment.size() < index + 1);
		}
		
		equipment.set(index, stack.copy());
		if (equipment.get(index).hasCapability(CapabilityAugmentableItem.AUGMENTABLE_ITEM, null)) {
			augmentSlots.add(index);
		}
		else {
			augmentSlots.remove(index);
		}
	}
	
	@Override
	public boolean hasAugments() {
		return !augmentSlots.isEmpty();
	}
	
	@Override
	public IntCollection getAugmentSlots() {
		return augmentSlots;
	}
	
}
