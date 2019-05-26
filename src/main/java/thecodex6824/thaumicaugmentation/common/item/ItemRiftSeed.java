/**
 *	Thaumic Augmentation
 *	Copyright (c) 2019 TheCodex6824.
 *
 *  This file is part of Thaumic Augmentation.
 *
 *  Thaumic Augmentation is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Thaumic Augmentation is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Thaumic Augmentation.  If not, see <https://www.gnu.org/licenses/>.
 */

package thecodex6824.thaumicaugmentation.common.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.common.entities.EntityFluxRift;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;

public class ItemRiftSeed extends ItemTABase {

	public ItemRiftSeed() {
		super("flux");
		setMaxStackSize(1);
		addPropertyOverride(new ResourceLocation("size"), new IItemPropertyGetter() {
			@Override
			public float apply(ItemStack stack, World world, EntityLivingBase entity) {
				if (stack.hasTagCompound())
					return stack.getTagCompound().getInteger("riftSize") / 100.0F;
				else
					return 0.2F;
			}
		});
	}

	protected <T extends Entity> List<T> getEntitiesInRange(Class<T> entityClass, World world, Vec3d pos, double radius) {
		List<T> toReturn = world.getEntitiesWithinAABB(entityClass, new AxisAlignedBB(pos.x, pos.y, pos.z, 
				pos.x, pos.y, pos.z).grow(radius));

		return toReturn;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {

		if (!world.isRemote) {
			BlockPos offset = pos.offset(facing);
			Vec3d position = new Vec3d(offset.getX() + 0.5, offset.getY() + 0.5, offset.getZ() + 0.5);
			if (getEntitiesInRange(EntityFluxRift.class, world, position, 32.0).isEmpty()) {
				EntityFluxRift rift = new EntityFluxRift(world);
				rift.setRiftSeed(world.rand.nextInt());
				rift.setLocationAndAngles(position.x, position.y, position.z, world.rand.nextInt(360), 0.0F);
				rift.setRiftStability(0.0F);
				rift.setRiftSize(player.getHeldItem(hand).getTagCompound().getInteger("riftSize"));
				world.spawnEntity(rift);
				if (!player.capabilities.isCreativeMode)
					player.getHeldItem(hand).shrink(1);
			}
			else
				player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.rift_too_close"), true);

			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (tab == TAItems.CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
			ItemStack fluxSeed = new ItemStack(this, 1, 0);
			fluxSeed.setTagCompound(new NBTTagCompound());
			fluxSeed.getTagCompound().setInteger("riftSize", 10);
			items.add(fluxSeed);
			ItemStack maxSeed = fluxSeed.copy();
			maxSeed.getTagCompound().setInteger("riftSize", 100);
			maxSeed.getTagCompound().setBoolean("grown", true);
			items.add(maxSeed);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound()) {
			tooltip.add(new TextComponentTranslation(
					"thaumicaugmentation.text.rift_seed_size", stack.getTagCompound().getInteger("riftSize")).getFormattedText());

			if (stack.getTagCompound().getBoolean("grown"))
				tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.rift_seed_grown").getFormattedText());
		}
	}

}
