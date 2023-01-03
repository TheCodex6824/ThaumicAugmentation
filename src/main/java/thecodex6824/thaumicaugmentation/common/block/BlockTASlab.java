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

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.block.property.ITASlabType;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;
import thecodex6824.thaumicaugmentation.common.util.IModelProvider;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class BlockTASlab extends BlockSlab implements ITASlabType {
    
    public static class Half extends BlockTASlab implements IModelProvider<Block>, IItemBlockProvider {
        @Override
        public boolean isDouble() {
            return false;
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void registerModels() {
            for (SlabType type : SlabType.values()) {
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.getMeta(), new ModelResourceLocation(
                        getRegistryName().getNamespace() + ":" + type.getName() + "_slab", "inventory"));
            }
        }
        
        @Override
        public ItemBlock createItemBlock() {
            return new ItemSlab(TABlocks.SLAB, (BlockSlab) TABlocks.SLAB, (BlockSlab) TABlocks.SLAB_DOUBLE) {
                @Override
                public String getTranslationKey(ItemStack stack) {
                    SlabType type = SlabType.fromMeta(stack.getMetadata());
                    if (type == null)
                        type = SlabType.ANCIENT_TILE;
                    
                    return "tile." + ThaumicAugmentationAPI.MODID + "." + type.getName() + "_slab";
                }
            };
        }
    }
    
    public static class Double extends BlockTASlab {
        @Override
        public boolean isDouble() {
            return true;
        }
    }
    
    public BlockTASlab() {
        super(Material.ROCK);
        setHardness(2.0F);
        setResistance(10.0F);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 0);
        IBlockState state = getDefaultState().withProperty(ITASlabType.SLAB_TYPE, SlabType.ANCIENT_TILE).withProperty(ITASlabType.DOUBLE, isDouble());
        if (!isDouble())
            setDefaultState(state.withProperty(HALF, EnumBlockHalf.BOTTOM));
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        if (!isDouble())
            return new BlockStateContainer(this, ITASlabType.SLAB_TYPE, HALF, ITASlabType.DOUBLE);
        else
            return new BlockStateContainer(this, ITASlabType.SLAB_TYPE, ITASlabType.DOUBLE);
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (isDouble())
            return getDefaultState().withProperty(ITASlabType.SLAB_TYPE, SlabType.fromMeta(meta));
        else
            return getDefaultState().withProperty(ITASlabType.SLAB_TYPE, SlabType.fromMeta(BitUtil.getBits(meta, 0, 3))).withProperty(
                    HALF, BitUtil.isBitSet(meta, 3) ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        if (isDouble())
            return state.getValue(ITASlabType.SLAB_TYPE).getMeta();
        else {
            int meta = state.getValue(ITASlabType.SLAB_TYPE).getMeta();
            meta = BitUtil.setBit(meta, 3, state.getValue(HALF) == EnumBlockHalf.TOP);
            return meta;
        }
    }
    
    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(ITASlabType.SLAB_TYPE).getMeta();
    }
    
    @Override
    public boolean getUseNeighborBrightness(IBlockState state) {
        return !isDouble();
    }
    
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(ITASlabType.SLAB_TYPE).getDrop().getItem();
    }
    
    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
            EntityPlayer player) {
        
        return state.getValue(ITASlabType.SLAB_TYPE).getDrop();
    }
    
    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.getValue(ITASlabType.SLAB_TYPE).getMapColor();
    }
    
    @Override
    public Material getMaterial(IBlockState state) {
        return state.getValue(ITASlabType.SLAB_TYPE).getMaterial();
    }
    
    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        return state.getValue(ITASlabType.SLAB_TYPE).getSoundType();
    }
    
    @Override
    public String getTranslationKey(int meta) {
        SlabType type = SlabType.fromMeta(meta);
        if (type == null)
            type = SlabType.ANCIENT_TILE;
        
        return type.getDrop().getItem().getTranslationKey();
    }
    
    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return SlabType.fromMeta(stack.getMetadata());
    }
    
    @Override
    public IProperty<?> getVariantProperty() {
        return ITASlabType.SLAB_TYPE;
    }
    
}
