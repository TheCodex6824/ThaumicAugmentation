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

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.item.block.ItemBlockNoImpetusNodeNBT;
import thecodex6824.thaumicaugmentation.common.tile.TileImpetusGenerator;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;

public class BlockImpetusGenerator extends BlockTABase implements IDirectionalBlock, IEnabledBlock, IItemBlockProvider {

    protected static final AxisAlignedBB DOWN_BOX = new AxisAlignedBB(0.1875, 0.0, 0.1875, 0.8125, 1.0, 0.8125);
    protected static final AxisAlignedBB EAST_BOX = new AxisAlignedBB(0.0, 0.1875, 0.1875, 1.0, 0.8125, 0.8125);
    protected static final AxisAlignedBB NORTH_BOX = new AxisAlignedBB(0.1875, 0.1875, 0.0, 0.8125, 0.8125, 1.0);
    protected static final AxisAlignedBB SOUTH_BOX = new AxisAlignedBB(0.1875, 0.1875, 0.0, 0.8125, 0.8125, 1.0);
    protected static final AxisAlignedBB UP_BOX = new AxisAlignedBB(0.1875, 0.0, 0.1875, 0.8125, 1.0, 0.8125);
    protected static final AxisAlignedBB WEST_BOX = new AxisAlignedBB(1.0, 0.1875, 0.1875, 0.0, 0.8125, 0.8125);
    
    public BlockImpetusGenerator() {
        super(Material.IRON);
        setHardness(3.0F);
        setResistance(35.0F);
        setSoundType(SoundType.METAL);
        setDefaultState(getDefaultState().withProperty(IDirectionalBlock.DIRECTION, EnumFacing.UP).withProperty(
                IEnabledBlock.ENABLED, true));
    }
    
    @Override
    public ItemBlock createItemBlock() {
        return new ItemBlockNoImpetusNodeNBT(this);
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IDirectionalBlock.DIRECTION, IEnabledBlock.ENABLED);
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(IDirectionalBlock.DIRECTION,
                EnumFacing.byIndex(BitUtil.getBits(meta, 0, 3))).withProperty(
                IEnabledBlock.ENABLED, BitUtil.isBitSet(meta, 3));
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(IDirectionalBlock.DIRECTION).getIndex();
        meta = BitUtil.setBit(meta, 3, state.getValue(IEnabledBlock.ENABLED));
        return meta;
    }
    
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {

        EnumFacing direction = EnumFacing.getDirectionFromEntityLiving(pos, placer).getOpposite();
        if (placer.isSneaking())
            direction = direction.getOpposite();
        
        return getDefaultState().withProperty(IDirectionalBlock.DIRECTION, direction);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(IDirectionalBlock.DIRECTION, rot.rotate(state.getValue(IDirectionalBlock.DIRECTION)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(IDirectionalBlock.DIRECTION)));
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
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(IDirectionalBlock.DIRECTION)) {
            case DOWN:  return DOWN_BOX;
            case EAST:  return EAST_BOX;
            case NORTH: return NORTH_BOX;
            case SOUTH: return SOUTH_BOX;
            case WEST:  return WEST_BOX;
            case UP:
            default:    return UP_BOX;
        }
    }
    
    @Override
    public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param) {
        if (id != 0) {
            if (!world.isRemote)
                return true;
            else {
                TileEntity tile = world.getTileEntity(pos);
                if (tile != null)
                    return tile.receiveClientEvent(id, param);
                else
                    return false;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
    
    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileImpetusGenerator();
    }
    
    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
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
        return BlockRenderLayer.SOLID;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
    
}
