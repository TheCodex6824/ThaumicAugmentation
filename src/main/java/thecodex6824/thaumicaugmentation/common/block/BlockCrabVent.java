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

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.tile.TileCrabVent;

public class BlockCrabVent extends BlockTABase implements IDirectionalBlock, IItemBlockProvider {

    protected static final AxisAlignedBB DOWN_BOX = new AxisAlignedBB(0.0, 0.75, 0.0, 1.0, 1.0, 1.0);
    protected static final AxisAlignedBB EAST_BOX = new AxisAlignedBB(0.0, 0.0, 0.0, 0.25, 1.0, 1.0);
    protected static final AxisAlignedBB NORTH_BOX = new AxisAlignedBB(0.0, 0.0, 0.75, 1.0, 1.0, 1.0);
    protected static final AxisAlignedBB SOUTH_BOX = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 0.25);
    protected static final AxisAlignedBB UP_BOX = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.25, 1.0);
    protected static final AxisAlignedBB WEST_BOX = new AxisAlignedBB(0.75, 0.0, 0.0, 1.0, 1.0, 1.0);
    
    public BlockCrabVent() {
        super(Material.ROCK);
        setHardness(10.0F);
        setResistance(30.0F);
        setHarvestLevel("pickaxe", 0);
        setDefaultState(getDefaultState().withProperty(IDirectionalBlock.DIRECTION, EnumFacing.UP));
    }
    
    @Override
    public ItemBlock createItemBlock() {
        return new ItemBlock(this) {
            @Override
            @SideOnly(Side.CLIENT)
            public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip,
                    ITooltipFlag flagIn) {
                
                tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.creative_only").setStyle(
                        new Style().setColor(TextFormatting.DARK_PURPLE)).getFormattedText());
            }
        };
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IDirectionalBlock.DIRECTION);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(IDirectionalBlock.DIRECTION).getIndex();
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(IDirectionalBlock.DIRECTION, EnumFacing.byIndex(meta));
    }
    
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {

        return getDefaultState().withProperty(IDirectionalBlock.DIRECTION, facing);
    }
    
    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        BlockPos test = pos.offset(side.getOpposite());
        return world.getBlockState(test).isSideSolid(world, test, side);
    }
    
    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.DESTROY;
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
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(IDirectionalBlock.DIRECTION, rot.rotate(state.getValue(IDirectionalBlock.DIRECTION)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(IDirectionalBlock.DIRECTION)));
    }
    
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
    
    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileCrabVent();
    }
    
    @Override
    public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param) {
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
    
    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return null;
    }
    
    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }
    
    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }
    
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
    
    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }
    
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
    
    @Override
    public boolean isTranslucent(IBlockState state) {
        return true;
    }
    
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
    
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {}
    
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        EnumFacing dir = state.getValue(IDirectionalBlock.DIRECTION).getOpposite();
        if (!world.getBlockState(pos.offset(dir)).isSideSolid(world, pos.offset(dir), dir.getOpposite())) {
            world.setBlockToAir(pos);
            dropBlockAsItem(world, pos, state, 0);
        }
    }
    
}
