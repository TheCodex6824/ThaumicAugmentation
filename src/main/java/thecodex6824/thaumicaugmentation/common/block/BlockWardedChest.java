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

package thecodex6824.thaumicaugmentation.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import thaumcraft.api.casters.ICaster;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.client.gui.GUIHandler;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockWardedCustomModel;
import thecodex6824.thaumicaugmentation.common.block.trait.IAnimatedBlock;
import thecodex6824.thaumicaugmentation.common.tile.TileWarded;
import thecodex6824.thaumicaugmentation.common.tile.TileWardedChest;

public class BlockWardedChest extends BlockWardedCustomModel implements IHorizontallyDirectionalBlock, IAnimatedBlock {

	protected static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 0.875, 0.9375);
	
	public BlockWardedChest(String name) {
		super(name, Material.IRON, TileWardedChest.class);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOUNDING_BOX;
	}
	
	protected void dropContents(World world, BlockPos pos) {
		if (!world.isRemote) {
			TileWardedChest chest = (TileWardedChest) world.getTileEntity(pos);
			IItemHandler items = chest.getInventory();
			for (int i = 0; i < items.getSlots(); ++i) {
				ItemStack stack = items.getStackInSlot(i);
				if (stack != null && !stack.isEmpty())
					world.spawnEntity(new EntityItem(world, pos.getX() + 0.5 + world.rand.nextGaussian() / 2, 
							pos.getY() + 0.5 + Math.abs(world.rand.nextGaussian() / 2), pos.getZ() + 0.5 + world.rand.nextGaussian() / 2, stack));
			}
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		dropContents(world, pos);
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		if (!world.isRemote) {
			if (((TileWarded) world.getTileEntity(pos)).hasPermission(player)) {
				if (player.getHeldItem(hand).getItem() instanceof ICaster && !player.isSneaking())
					world.setBlockState(pos, state.withRotation(Rotation.CLOCKWISE_90));
				else 
					player.openGui(ThaumicAugmentation.instance, GUIHandler.TAInventory.WARDED_CHEST.getID(), world, 
							pos.getX(), pos.getY(), pos.getZ());
				
				return true;
			}
			else
				return false;
		}
		
		return true;
	}
	
}
