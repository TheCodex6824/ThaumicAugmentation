package thecodex6824.thaumicaugmentation.common.container;

import java.util.Iterator;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class CombinedItemStackHandler implements IItemHandler {

	protected ImmutableList<IItemHandler> handlers;
	
	public CombinedItemStackHandler(IItemHandler... handlers) {
		this.handlers = ImmutableList.copyOf(handlers);
	}
	
	public CombinedItemStackHandler(Iterator<IItemHandler> handlers) {
		this.handlers = ImmutableList.copyOf(handlers);
	}
	
	public CombinedItemStackHandler(Iterable<IItemHandler> handlers) {
		this(handlers.iterator());
	}
	
	protected static final class HandlerPair {
		
		public HandlerPair(IItemHandler handler, int slot) {
			this.handler = handler;
			this.slot = slot;
		}
		
		public final IItemHandler handler;
		public final int slot;
		
	}
	
	protected HandlerPair getHandler(int slot) {
		// this cannot be cached, because handlers can change the amount of slots they have
		int localSlot = slot;
		for (IItemHandler handler : handlers) {
			if (localSlot < handler.getSlots()) {
				return new HandlerPair(handler, localSlot);
			}
			
			localSlot -= handler.getSlots();
		}
		
		throw new IndexOutOfBoundsException();
	}
	
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		HandlerPair pair = getHandler(slot);
		return pair.handler.extractItem(pair.slot, amount, simulate);
	}
	
	@Override
	public int getSlotLimit(int slot) {
		HandlerPair pair = getHandler(slot);
		return pair.handler.getSlotLimit(pair.slot);
	}
	
	@Override
	public int getSlots() {
		return handlers.stream().reduce(0, (acc, h) -> acc + h.getSlots(), Integer::sum);
	}
	
	@Override
	public ItemStack getStackInSlot(int slot) {
		HandlerPair pair = getHandler(slot);
		return pair.handler.getStackInSlot(pair.slot);
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		HandlerPair pair = getHandler(slot);
		return pair.handler.insertItem(pair.slot, stack, simulate);
	}
	
}
