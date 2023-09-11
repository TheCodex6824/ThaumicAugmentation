package thecodex6824.thaumicaugmentation.common.capability;

import baubles.api.BaubleType;
import baubles.api.cap.BaubleItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thecodex6824.thaumicaugmentation.common.event.EquipmentSyncEventHandler;

public class CustomSyncBaubleItem extends BaubleItem {

	public CustomSyncBaubleItem(BaubleType type) {
		super(type);
	}
	
	@Override
	public void onEquipped(ItemStack stack, EntityLivingBase player) {
		super.onEquipped(stack, player);
		EquipmentSyncEventHandler.onEquipmentChange(player);
	}
	
	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase player) {
		super.onUnequipped(stack, player);
		EquipmentSyncEventHandler.onEquipmentChange(player);
	}
	
	public NBTTagCompound getSyncNBT(boolean forOwner) {
		return new NBTTagCompound();
	}
	
	public void readSyncNBT(NBTTagCompound tag) {}
	
}
