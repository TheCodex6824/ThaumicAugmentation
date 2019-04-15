package thecodex6824.thaumicaugmentation.common.item;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import thecodex6824.thaumicaugmentation.api.item.IWardAuthenticator;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

public class ItemKey extends ItemTABase implements IWardAuthenticator {

	public ItemKey(String name) {
		super(name, "iron", "brass");
		setMaxStackSize(1);
	}
	
	@Override
	public boolean permitsUsage(World world, BlockPos pos, ItemStack stack, EntityPlayer user, String ownerID) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("boundTo", NBT.TAG_STRING) ?
				stack.getTagCompound().getString("boundTo").equals(ownerID) : false;
	}
	
	protected int generateKeyColor(String id) {
		UUID uuid = null;
		try {
			uuid = UUID.fromString(id);
		}
		catch (IllegalArgumentException ex) {}
		
		byte[] toHash = null;
		if (uuid != null)
			toHash = ByteBuffer.allocate(16).putLong(uuid.getLeastSignificantBits()).putLong(uuid.getMostSignificantBits()).array();
		else
			toHash = id.getBytes(StandardCharsets.UTF_8);
		
		// this hash function is probably terrible, but it's not for anything important so whatever
		int output = 15065339;
		for (byte b : toHash) {
			output ^= b;
			output *= 26016127;
		}
		
		return output;
	}
	
	public int getKeyColor(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("boundToColor", NBT.TAG_INT) ?
				stack.getTagCompound().getInteger("boundToColor") : 0;
	}
	
	public void setBoundTo(ItemStack stack, String display, String id) {
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		stack.getTagCompound().setString("boundTo", id.toString());
		stack.getTagCompound().setString("boundToDisplay", display);
		stack.getTagCompound().setInteger("boundToColor", generateKeyColor(id));
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		
		if (!world.isRemote) {
			ItemStack stack = player.getHeldItem(hand);
			if (!player.isSneaking() && !stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setString("boundTo", player.getUniqueID().toString());
				stack.getTagCompound().setString("boundToDisplay", player.getName());
				stack.getTagCompound().setInteger("boundToColor", generateKeyColor(player.getUniqueID().toString()));
				player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.key_bound"), true);
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
			}
			else if (player.isSneaking() && stack.hasTagCompound()) {
				stack.setTagCompound(null);
				player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.key_unbound"), true);
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
			}
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	}
	
	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return stack.getMetadata() == 1;
	}
	
	@Override
	public ItemStack getContainerItem(ItemStack itemStack) {
		return itemStack.getMetadata() == 1 ? itemStack.copy() : ItemStack.EMPTY;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("boundToDisplay")) {
			tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.bound_to", 
					stack.getTagCompound().getString("boundToDisplay")).getFormattedText());
		}
	}
	
}
