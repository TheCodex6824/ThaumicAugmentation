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

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.property.Properties;
import thaumcraft.api.blocks.BlocksTC;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.TAMaterials;
import thecodex6824.thaumicaugmentation.api.block.property.IImpetusCellInfo;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.tile.TileImpetusMatrix;
import thecodex6824.thaumicaugmentation.common.tile.trait.IBreakCallback;

public class BlockImpetusMatrix extends BlockTABase implements IItemBlockProvider {

    public BlockImpetusMatrix() {
        super(Material.ROCK);
        setHardness(7.5F);
        setResistance(500.0F);
        setSoundType(SoundType.STONE);
    }
    
    @Override
    public ItemBlock createItemBlock() {
        return new ItemBlock(this) {
            @Override
            public IRarity getForgeRarity(ItemStack stack) {
                return TAMaterials.RARITY_ELDRITCH;
            }
        };
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(Properties.AnimationProperty).build();
    }
    
    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }
    
    @Override
    public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileImpetusMatrix)
            return ((TileImpetusMatrix) tile).getComparatorOutput();
        
        return 0;
    }
    
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileImpetusMatrix();
    }
    
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {
        
        drops.add(new ItemStack(BlocksTC.infusionMatrix));
    }
    
    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }
    
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (world.getBlockState(pos.down()).getBlock() == TABlocks.IMPETUS_MATRIX_BASE) {
            int cellCount = IImpetusCellInfo.getNumberOfCells(world.getBlockState(pos.down()).getValue(IImpetusCellInfo.CELL_INFO));
            if (cellCount > 0)
                spawnAsEntity(world, pos.down(), new ItemStack(TAItems.MATERIAL, cellCount, 3));
            
            world.setBlockState(pos.down(), BlocksTC.pedestalEldritch.getDefaultState());
        }
        if (world.getBlockState(pos.up()).getBlock() == TABlocks.IMPETUS_MATRIX_BASE) {
            int cellCount = IImpetusCellInfo.getNumberOfCells(world.getBlockState(pos.up()).getValue(IImpetusCellInfo.CELL_INFO));
            if (cellCount > 0)
                spawnAsEntity(world, pos.up(), new ItemStack(TAItems.MATERIAL, cellCount, 3));
            
            world.setBlockState(pos.up(), BlocksTC.pedestalEldritch.getDefaultState());
        }
        
        TileEntity t = world.getTileEntity(pos);
        if (t instanceof IBreakCallback)
            ((IBreakCallback) t).onBlockBroken();
        
        super.breakBlock(world, pos, state);
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
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }
    
}
