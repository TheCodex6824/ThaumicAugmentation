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
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.block.property.IAltarBlock;
import thecodex6824.thaumicaugmentation.api.block.property.IObeliskType;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.tile.TileAltar;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;

public class BlockCapstone extends BlockTABase implements IItemBlockProvider, IObeliskType, IAltarBlock {

    public BlockCapstone() {
        super(Material.ROCK);
        setHardness(-1.0F);
        setResistance(6000000.0F);
        setDefaultState(getDefaultState().withProperty(IObeliskType.OBELISK_TYPE, ObeliskType.ELDRITCH).withProperty(
                IAltarBlock.ALTAR, false));
    }
    
    @Override
    public ItemBlock createItemBlock() {
        return new ItemMultiTexture(this, null, new String[] {
                "capstone_eldritch",
                "capstone_ancient",
                "altar_eldritch",
                "altar_ancient"
        }) {
            
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
        return new BlockStateContainer(this, IObeliskType.OBELISK_TYPE, IAltarBlock.ALTAR);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(IObeliskType.OBELISK_TYPE).getMeta();
        meta |= state.getValue(IAltarBlock.ALTAR) ? 2 : 0;
        return meta;
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState().withProperty(IObeliskType.OBELISK_TYPE, ObeliskType.fromMeta(BitUtil.getBits(meta, 0, 1)));
        return state.withProperty(IAltarBlock.ALTAR, BitUtil.isBitSet(meta, 1));
    }
    
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return state.getValue(IAltarBlock.ALTAR);
    }
    
    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        if (state.getValue(IAltarBlock.ALTAR))
            return new TileAltar();
        else
            return null;
    }
    
    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 0;
    }
    
    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
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
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }
    
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {}
    
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == TAItems.CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
            for (int i = 0; i < 4; ++i)
                items.add(new ItemStack(this, 1, i));
        }
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends Comparable<T>> String getPropName(IProperty<T> prop, Comparable<?> comp) {
        return prop.getName((T) comp);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for (int i = 0; i < 4; ++i) {
            IBlockState state = getStateFromMeta(i);
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {
                if (builder.length() != 0)
                    builder.append(",");

                IProperty<?> property = entry.getKey();
                builder.append(property.getName());
                builder.append("=");
                builder.append(getPropName(property, entry.getValue()));
            }

            if (builder.length() == 0)
                builder.append("normal");

            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), i, new ModelResourceLocation(
                    getRegistryName().toString(), builder.toString()));
        }
    }
    
}
