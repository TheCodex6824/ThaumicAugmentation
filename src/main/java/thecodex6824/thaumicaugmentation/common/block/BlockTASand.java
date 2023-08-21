/*
 *  Thaumic Augmentation
 *  Copyright (c) 2023 TheCodex6824.
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

import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.block.property.ITASandType;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;
import thecodex6824.thaumicaugmentation.common.util.IModelProvider;

public class BlockTASand extends BlockFalling implements ITASandType, IItemBlockProvider, IModelProvider<Block> {

    public BlockTASand() {
        super(Material.SAND);
        setDefaultState(getDefaultState().withProperty(ITASandType.SAND_TYPE, SandType.SAND_MERCURIAL));
        setHardness(0.5F);
        setSoundType(SoundType.SAND);
        setHarvestLevel("shovel", 0);
    }
    
    @Override
    public ItemBlock createItemBlock() {
        return new ItemMultiTexture(this, null, ITASandType.SAND_TYPE.getAllowedValues().stream().map(
                ITASandType.SAND_TYPE::getName).collect(Collectors.toList()).toArray(new String[ITASandType.SAND_TYPE.getAllowedValues().size()]));
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ITASandType.SAND_TYPE);
    }
    
    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.getValue(ITASandType.SAND_TYPE).getMapColor();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public int getDustColor(IBlockState state) {
    	return state.getValue(ITASandType.SAND_TYPE).getDustColor();
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ITASandType.SAND_TYPE, SandType.fromMeta(BitUtil.getBits(meta, 0, 4)));
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ITASandType.SAND_TYPE).getMeta();
    }
    
    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(ITASandType.SAND_TYPE).getMeta();
    }
    
    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction,
    		IPlantable plantable) {
    	
    	return Blocks.SAND.canSustainPlant(state, world, pos, direction, plantable);
    }
    
    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == TAItems.CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
            for (SandType type : SandType.values()) {
                items.add(new ItemStack(this, 1, type.getMeta()));
            }
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for (SandType type : SandType.values()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.getMeta(), new ModelResourceLocation(
                    getRegistryName().getNamespace() + ":" + type.getName(), "inventory"));
        }
    }
    
}
