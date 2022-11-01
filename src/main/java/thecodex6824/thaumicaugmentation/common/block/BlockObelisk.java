/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskPart;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskType;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.tile.TileObelisk;
import thecodex6824.thaumicaugmentation.common.tile.TileObeliskVisual;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;

import javax.annotation.Nullable;

public class BlockObelisk extends BlockTABase implements IObeliskType, IObeliskPart {

    public BlockObelisk() {
        super(Material.ROCK);
        setHardness(-1.0F);
        setResistance(6000000.0F);
        setDefaultState(getDefaultState().withProperty(IObeliskType.OBELISK_TYPE, ObeliskType.ELDRITCH).withProperty(
                IObeliskPart.OBELISK_PART, ObeliskPart.MIDDLE));
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IObeliskType.OBELISK_TYPE, IObeliskPart.OBELISK_PART, IDirectionalBlock.DIRECTION);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(IObeliskPart.OBELISK_PART).getMeta();
        meta |= (state.getValue(IObeliskType.OBELISK_TYPE).getMeta() << 2);
        return meta;
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState().withProperty(IObeliskPart.OBELISK_PART, ObeliskPart.fromMeta(BitUtil.getBits(meta, 0, 2)));
        return state.withProperty(IObeliskType.OBELISK_TYPE, ObeliskType.fromMeta(BitUtil.getBits(meta, 2, 4)));
    }
    
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state.getValue(IObeliskPart.OBELISK_PART) == ObeliskPart.CAP) {
            IBlockState down = world.getBlockState(pos.down());
            if (down.getPropertyKeys().contains(IObeliskPart.OBELISK_PART) && down.getValue(IObeliskPart.OBELISK_PART) == ObeliskPart.INNER)
                return state.withProperty(IDirectionalBlock.DIRECTION, EnumFacing.UP);
            else
                return state.withProperty(IDirectionalBlock.DIRECTION, EnumFacing.DOWN);
        }
        else if (state.getValue(IObeliskPart.OBELISK_PART) == ObeliskPart.INNER) {
            IBlockState down = world.getBlockState(pos.down());
            if (down.getPropertyKeys().contains(IObeliskPart.OBELISK_PART) && down.getValue(IObeliskPart.OBELISK_PART) == ObeliskPart.MIDDLE)
                return state.withProperty(IDirectionalBlock.DIRECTION, EnumFacing.DOWN);
            else
                return state.withProperty(IDirectionalBlock.DIRECTION, EnumFacing.UP);
        }
        
        return state;
    }
    
    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(TAItems.OBELISK_PLACER, 1, state.getValue(IObeliskType.OBELISK_TYPE).getMeta());
    }
    
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        IBlockState d = world.getBlockState(pos.down());
        IBlockState u = world.getBlockState(pos.up());
        ObeliskPart down = d.getBlock() == TABlocks.OBELISK ? d.getValue(IObeliskPart.OBELISK_PART) : null;
        ObeliskPart up = u.getBlock() == TABlocks.OBELISK ? u.getValue(IObeliskPart.OBELISK_PART) : null;
        ObeliskPart check = state.getValue(IObeliskPart.OBELISK_PART);
        if (check == ObeliskPart.CAP) {
            if (down != ObeliskPart.INNER && up != ObeliskPart.INNER)
                world.destroyBlock(pos, false);
        }
        else if (check == ObeliskPart.INNER) {
            if ((down != ObeliskPart.CAP || up != ObeliskPart.MIDDLE) && (down != ObeliskPart.MIDDLE || up != ObeliskPart.CAP))
                world.destroyBlock(pos, false);
        }
        else {
            if (down != ObeliskPart.INNER || up != ObeliskPart.INNER)
                world.destroyBlock(pos, false);
        }
    }
    
    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 0;
    }
    
    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(IObeliskPart.OBELISK_PART) == ObeliskPart.CAP ? 0 : 9;
    }
    
    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
    
    @Override
    public boolean isReplaceableOreGen(IBlockState state, IBlockAccess world, BlockPos pos,
            Predicate<IBlockState> target) {

        return false;
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }
    
    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return false;
    }
    
    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 0;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 0;
    }

    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.IGNORE;
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }
    
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
       return BlockFaceShape.UNDEFINED;
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
        return state.getValue(IObeliskPart.OBELISK_PART) != ObeliskPart.CAP;
    }
    
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
    
    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        if (state.getValue(IObeliskPart.OBELISK_PART) == ObeliskPart.MIDDLE)
            return new TileObelisk();
        else
            return new TileObeliskVisual();
    }
    
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
            EnumFacing side) {
        
        if (side.getAxis() == Axis.Y && state.getValue(IObeliskPart.OBELISK_PART) != ObeliskPart.CAP)
            return false;
        else
            return super.shouldSideBeRendered(state, world, pos, side);
    }
    
}
