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

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.block.property.IStarfieldGlassType;
import thecodex6824.thaumicaugmentation.common.item.block.ItemBlockStarfieldGlass;
import thecodex6824.thaumicaugmentation.common.tile.TileStarfieldGlass;

public class BlockStarfieldGlass extends BlockFortifiedGlass implements IStarfieldGlassType {

    public BlockStarfieldGlass() {
        super();
        setDefaultState(getDefaultState().withProperty(IStarfieldGlassType.GLASS_TYPE, GlassType.GLASS_RIFT));
    }
    
    @Override
    public ItemBlock createItemBlock() {
        return new ItemBlockStarfieldGlass();
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IStarfieldGlassType.GLASS_TYPE);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(IStarfieldGlassType.GLASS_TYPE).getMeta();
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(IStarfieldGlassType.GLASS_TYPE, GlassType.fromMeta(meta));
    }
    
    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(IStarfieldGlassType.GLASS_TYPE).getMeta();
    }
    
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
    
    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileStarfieldGlass();
    }
    
    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == TAItems.CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
            for (GlassType type : GlassType.values())
                items.add(new ItemStack(this, 1, type.getMeta()));
        }
    }
    
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {}
    
}
