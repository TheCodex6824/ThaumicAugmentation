/**
 *  Thaumic Augmentation
 *  Copyright (c) 2019 TheCodex6824.
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

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLogic;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thecodex6824.thaumicaugmentation.api.block.property.IDimensionalFractureBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IDimensionalFractureBlock.BlockType;
import thecodex6824.thaumicaugmentation.common.tile.TileDimensionalFracture;
import thecodex6824.thaumicaugmentation.common.world.DimensionalFractureTeleporter;

public class BlockDimensionalFracture extends Block {

	public BlockDimensionalFracture() {
		super(new MaterialLogic(MapColor.BLACK));
		setBlockUnbreakable();
		setResistance(Float.MAX_VALUE / 16.0F);
		setLightLevel(1.0F);
		setLightOpacity(0);
		setDefaultState(getDefaultState().withProperty(IDimensionalFractureBlock.BLOCK_TYPE, BlockType.DUMMY));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, IDimensionalFractureBlock.BLOCK_TYPE);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(IDimensionalFractureBlock.BLOCK_TYPE, BlockType.fromMeta(meta));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(IDimensionalFractureBlock.BLOCK_TYPE).getMeta();
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return null;
	}

	@Override
	public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type) {
		return false;
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return state.getValue(IDimensionalFractureBlock.BLOCK_TYPE) == BlockType.MAIN ? EnumBlockRenderType.ENTITYBLOCK_ANIMATED : EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	@Override
	public boolean isTopSolid(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}

	protected void verifyChunk(World world, BlockPos pos) {
		IChunkProvider provider = world.getChunkProvider();
		if (!provider.isChunkGeneratedAt(pos.getX() >> 4, pos.getZ() >> 4)) {
			world.getChunk(pos.add(16, 0, 0));
			world.getChunk(pos.add(0, 0, 16));
			world.getChunk(pos.add(16, 0, 16));
			provider.provideChunk(pos.getX() >> 4, pos.getZ() >> 4);
		}
		else if (provider.getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4) == null)
			world.getChunk(pos);
	}

	@Override
	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
		if (!world.isRemote && state.getValue(IDimensionalFractureBlock.BLOCK_TYPE) == BlockType.MAIN && 
				entity.timeUntilPortal == 0 && world.getTileEntity(pos) instanceof TileDimensionalFracture) {
			TileDimensionalFracture tile = (TileDimensionalFracture) world.getTileEntity(pos);
			if (true) {//tile.isOpen()) {
				if (tile.isLinkInvalid()) {
					if (world.getTotalWorldTime() % 20 == 0 && entity instanceof EntityPlayer)
						((EntityPlayer) entity).sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.no_fracture_target"), true);
				}
				else if (tile.getLinkedPosition() != null) {
					World targetWorld = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(tile.getLinkedDimension());
					if (!tile.wasLinkLocated()) {
						BlockPos toComplete = tile.getLinkedPosition();
						verifyChunk(targetWorld, toComplete);
						for (int y = targetWorld.getActualHeight() - 1; y >= 0; --y) {
							BlockPos check = toComplete.add(0, y, 0);
							if (targetWorld.getBlockState(check).getBlock() == this && 
									targetWorld.getTileEntity(check) instanceof TileDimensionalFracture) {

								tile.setLinkedPosition(check);
								tile.setLinkLocated();
								break;
							}
						}

						if (!tile.wasLinkLocated()) {
							tile.setLinkInvalid();
							return;
						}
					}

					BlockPos target = tile.getLinkedPosition();
					verifyChunk(targetWorld, target);
					if (targetWorld.getBlockState(target).getBlock() != this)
						tile.setLinkInvalid();
					else {
						entity.changeDimension(targetWorld.provider.getDimension(), new DimensionalFractureTeleporter(target));
						entity.timeUntilPortal = entity.getPortalCooldown() < 100 ? 100 : entity.getPortalCooldown();
					}
				}
			}
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return state.getValue(IDimensionalFractureBlock.BLOCK_TYPE) == BlockType.MAIN;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileDimensionalFracture();
	}

}
