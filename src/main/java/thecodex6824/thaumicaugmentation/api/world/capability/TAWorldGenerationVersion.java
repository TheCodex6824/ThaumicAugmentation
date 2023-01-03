package thecodex6824.thaumicaugmentation.api.world.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Constants.NBT;

public class TAWorldGenerationVersion implements ITAWorldGenerationVersion, INBTSerializable<NBTTagCompound> {

	protected int version;
	
	@Override
	public int getVersion() {
		return version;
	}
	
	@Override
	public void setVersion(int version) {
		this.version = version;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("version", NBT.TAG_INT)) {
			version = nbt.getInteger("version");
		}
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("version", version);
		return tag;
	}
	
}
