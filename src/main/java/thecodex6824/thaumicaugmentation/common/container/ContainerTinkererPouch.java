package thecodex6824.thaumicaugmentation.common.container;

import java.lang.ref.WeakReference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerTinkererPouch extends Container {

	protected WeakReference<EntityPlayer> thePlayer;
	
	public ContainerTinkererPouch(EntityPlayer player) {
		thePlayer = new WeakReference<EntityPlayer>(player);
		ItemStack pouchStack = player.inventory.getCurrentItem();
		IItemHandler item = pouchStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        for (int i = 0; i < item.getSlots(); ++i) {
            addSlotToContainer(new SlotItemHandler(item, i, 8 + i % 9 * 18, 40 + i / 9 * 18));
        }
        
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, 8 + x * 18, 134 + y * 18));
            }
        }
        
        for (int x = 0; x < 9; ++x) {
            addSlotToContainer(new Slot(player.inventory, x, 8 + x * 18, 192));
        }
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return player.equals(thePlayer.get());
	}
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack otherStack = slot.getStack();
            stack = otherStack.copy();

            int containerSlots = inventorySlots.size() - player.inventory.mainInventory.size();
            if (index < containerSlots) {
                if (!this.mergeItemStack(otherStack, containerSlots, inventorySlots.size(), true))
                    return ItemStack.EMPTY;
            }
            else if (!this.mergeItemStack(otherStack, 0, containerSlots, false))
                return ItemStack.EMPTY;

            if (otherStack.getCount() == 0)
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();

            if (otherStack.getCount() == stack.getCount())
                return ItemStack.EMPTY;

            slot.onTake(player, otherStack);
        }

        return stack;
    }
	
}
