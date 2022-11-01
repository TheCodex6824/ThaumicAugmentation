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

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.block.property.IStarfieldGlassType;
import thecodex6824.thaumicaugmentation.common.block.trait.IRenderableSides;
import thecodex6824.thaumicaugmentation.common.item.block.ItemBlockStarfieldGlass;
import thecodex6824.thaumicaugmentation.common.tile.TileStarfieldGlass;

import javax.annotation.Nullable;

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
        return new BlockStateContainer.Builder(this).add(IStarfieldGlassType.GLASS_TYPE).add(
                IRenderableSides.SIDES).build();
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
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState ext = (IExtendedBlockState) super.getExtendedState(state, world, pos);
        IBlockState clean = ext.getClean();
        ImmutableList.Builder<EnumFacing> faces = ImmutableList.builder();
        MutableBlockPos work = new MutableBlockPos();
        for (EnumFacing face : EnumFacing.VALUES) {
            if (state.shouldSideBeRendered(world, pos, face))
                faces.add(face);
            else {
                for (EnumFacing otherFace : EnumFacing.VALUES) {
                    if (otherFace != face.getOpposite()) {
                        // check for corners to prevent seams
                        work.setPos(pos.offset(otherFace));
                        IBlockState check = world.getBlockState(work);
                        if (check == clean && check.shouldSideBeRendered(world, work, face)) {
                            faces.add(face);
                            break;
                        }
                    }
                }
            }
        }
        
        return ext.withProperty(IRenderableSides.SIDES, faces.build());
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
