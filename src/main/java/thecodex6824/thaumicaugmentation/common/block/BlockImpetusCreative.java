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
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
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
import thecodex6824.thaumicaugmentation.api.TAConfig;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.block.property.ICreativeImpetusBlock;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.item.block.ItemBlockNoImpetusNodeNBTMultiTexture;
import thecodex6824.thaumicaugmentation.common.tile.TileCreativeImpetusSink;
import thecodex6824.thaumicaugmentation.common.tile.TileCreativeImpetusSource;

public class BlockImpetusCreative extends BlockTABase implements ICreativeImpetusBlock, IItemBlockProvider {

    public BlockImpetusCreative() {
        super(Material.IRON);
        setHardness(3.0F);
        setResistance(35.0F);
        setSoundType(SoundType.METAL);
        setDefaultState(getDefaultState().withProperty(ICreativeImpetusBlock.BLOCK_TYPE, BlockType.SOURCE));
    }
    
    @Override
    public ItemBlock createItemBlock() {
        return new ItemBlockNoImpetusNodeNBTMultiTexture(this, ICreativeImpetusBlock.BLOCK_TYPE.getAllowedValues().stream().map(
                ICreativeImpetusBlock.BLOCK_TYPE::getName).collect(Collectors.toList()).toArray(new String[ICreativeImpetusBlock.BLOCK_TYPE.getAllowedValues().size()])) {
            @Override
            @SideOnly(Side.CLIENT)
            public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip,
                    ITooltipFlag flagIn) {
                
                if (!TAConfig.disableCreativeOnlyText.getValue()) {
                    tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.creative_only").setStyle(
                            new Style().setColor(TextFormatting.DARK_PURPLE)).getFormattedText());
                }
            }
        };
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ICreativeImpetusBlock.BLOCK_TYPE);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ICreativeImpetusBlock.BLOCK_TYPE).getMeta();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ICreativeImpetusBlock.BLOCK_TYPE, BlockType.fromMeta(meta));
    }
    
    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(ICreativeImpetusBlock.BLOCK_TYPE).getMeta();
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
        switch (state.getValue(ICreativeImpetusBlock.BLOCK_TYPE)) {
            case SINK: return new TileCreativeImpetusSink();
            default: return new TileCreativeImpetusSource();
        }
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
    
    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == TAItems.CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
            for (BlockType type : BlockType.values())
                items.add(new ItemStack(this, 1, type.getMeta()));
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for (BlockType type : BlockType.values()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.getMeta(), new ModelResourceLocation(
                    getRegistryName().getNamespace() + ":" + type.getName(), "inventory"));
        }
    }
    
}
