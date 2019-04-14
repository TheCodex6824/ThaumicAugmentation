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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTACustomModel;
import thecodex6824.thaumicaugmentation.common.block.trait.IAnimatedBlock;
import thecodex6824.thaumicaugmentation.common.tile.TileVisRegenerator;

public class BlockVisRegenerator extends BlockTACustomModel implements IEnabledBlock, IAnimatedBlock {

	protected static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1.0, 0.6875, 1.0);
	
	public BlockVisRegenerator(String name) {
		super(name, Material.WOOD, TileVisRegenerator.class);
		setHardness(2.0F);
		setResistance(20.0F);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOUNDING_BOX;
	}
	
}
