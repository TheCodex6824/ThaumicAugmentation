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
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.item.block.ItemBlockNoImpetusNodeNBT;
import thecodex6824.thaumicaugmentation.common.tile.TileImpetusDiffuser;

public class BlockImpetusDiffuser extends BlockTABase implements IEnabledBlock, IItemBlockProvider {

    protected static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.125, 0.0, 0.125, 0.875, 0.8125, 0.875);
    
    public BlockImpetusDiffuser() {
        super(Material.IRON);
        setHardness(3.0F);
        setResistance(35.0F);
        setDefaultState(getDefaultState().withProperty(IEnabledBlock.ENABLED, true));
        setSoundType(SoundType.METAL);
    }
    
    @Override
    public ItemBlock createItemBlock() {
        return new ItemBlockNoImpetusNodeNBT(this);
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(IEnabledBlock.ENABLED).add(Properties.AnimationProperty).add(
                Properties.StaticProperty).build();
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(IEnabledBlock.ENABLED) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(IEnabledBlock.ENABLED, meta == 1);
    }
    
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return BOUNDING_BOX;
    }

    protected void update(IBlockState state, World world, BlockPos pos) {
        boolean powered = world.isBlockPowered(pos);
        if (powered == state.getValue(IEnabledBlock.ENABLED))
            world.setBlockState(pos, state.cycleProperty(IEnabledBlock.ENABLED), 3);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        update(state, world, pos);
        super.onBlockAdded(world, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        update(state, world, pos);
    }
    
    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileImpetusDiffuser();
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
        return EnumBlockRenderType.MODEL;
    }
    
}
