package thecodex6824.thaumicaugmentation.common.container;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thecodex6824.thaumicaugmentation.api.augment.AugmentConfiguration;

public class AugmentSlot extends Slot {

    protected static final IInventory EMPTY_INV = new InventoryBasic("[Null]", true, 0);
    
    protected AugmentConfiguration config;
    
    public AugmentSlot(AugmentConfiguration config, int index, int xPosition, int yPosition) {
        super(EMPTY_INV, index, xPosition, yPosition);
        this.config = config;
    }
    
    public void changeConfiguration(AugmentConfiguration config) {
        this.config = config;
    }
    
    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return false;
    }
    
    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return false;
    }
    
    @Override
    public ItemStack decrStackSize(int amount) {
        return getStack();
    }
    
    @Override
    public boolean isSameInventory(Slot other) {
        return other instanceof AugmentSlot;
    }
    
    @Override
    public void onSlotChange(ItemStack oldStack, ItemStack newStack) {}
    
    @Override
    public void putStack(ItemStack stack) {
        if (config.isAugmentAcceptable(stack, getSlotIndex()))
            config.setAugment(stack, getSlotIndex());
    }
    
    @Override
    public ItemStack getStack() {
        return config.getAugment(getSlotIndex());
    }
    
}
