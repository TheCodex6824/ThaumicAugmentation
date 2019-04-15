package thecodex6824.thaumicaugmentation.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IWardAuthenticator {

	public boolean permitsUsage(World world, BlockPos pos, ItemStack stack, EntityPlayer user, String ownerID);
	
}
