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

import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.TALootTables;
import thecodex6824.thaumicaugmentation.api.block.property.IUrnType;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;

public class BlockTAUrn extends BlockTABase implements IUrnType, IItemBlockProvider {

    protected static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.125, 0.0625, 0.125, 0.875, 0.8125, 0.875);
    
    public BlockTAUrn() {
        super(Material.ROCK);
        setHardness(1.0F);
        setResistance(3.0F);
        setDefaultState(getDefaultState().withProperty(IUrnType.URN_TYPE, UrnType.URN_COMMON));
    }
    
    @Override
    public ItemBlock createItemBlock() {
        return new ItemMultiTexture(this, null, IUrnType.URN_TYPE.getAllowedValues().stream().map(
                IUrnType.URN_TYPE::getName).collect(Collectors.toList()).toArray(new String[IUrnType.URN_TYPE.getAllowedValues().size()]));
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IUrnType.URN_TYPE);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(IUrnType.URN_TYPE).getMeta();
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(IUrnType.URN_TYPE, UrnType.fromMeta(meta));
    }
    
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
    
    @Override
    public boolean isBlockNormalCube(IBlockState state) {
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
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }
    
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
    
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }
    
    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }
    
    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }
    
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {
        
        if (world instanceof WorldServer) {
            LootTable table = null;
            switch (state.getValue(IUrnType.URN_TYPE)) {
                case URN_COMMON: {
                    table = ((WorldServer) world).getLootTableManager().getLootTableFromLocation(
                            TALootTables.LOOT_COMMON);
                    break;
                }
                case URN_UNCOMMON: {
                    table = ((WorldServer) world).getLootTableManager().getLootTableFromLocation(
                            TALootTables.LOOT_UNCOMMON);
                    break;
                }
                case URN_RARE: {
                    table = ((WorldServer) world).getLootTableManager().getLootTableFromLocation(
                            TALootTables.LOOT_RARE);
                    break;
                }
                default: return;
            }
            
            LootContext context = new LootContext.Builder(((WorldServer) world)).build();
            drops.addAll(table.generateLootForPools(((WorldServer) world).rand, context));
        }
    }
    
    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        return SoundsTC.URN;
    }
    
    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == TAItems.CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
            for (UrnType type : UrnType.values())
                items.add(new ItemStack(this, 1, type.getMeta()));
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for (UrnType type : UrnType.values()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.getMeta(), new ModelResourceLocation(
                    getRegistryName().getNamespace() + ":" + type.getName(), "inventory"));
        }
    }
    
}
