package thecodex6824.thaumicaugmentation.api.augment;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface IAugmentConfigurationStorageSerializable extends IAugmentConfigurationStorage, INBTSerializable<NBTTagCompound> {

	public NBTTagCompound serializeConfigsForSingleItem(ItemStack input);
	
	public void deserializeConfigsForSingleItem(NBTTagCompound configsTag);
	
}
