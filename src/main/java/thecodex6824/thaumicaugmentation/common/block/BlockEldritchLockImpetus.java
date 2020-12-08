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

import com.google.common.base.Predicate;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
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
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IEldritchLock;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.block.trait.ILockClosed;
import thecodex6824.thaumicaugmentation.common.tile.TileEldritchLock;

public class BlockEldritchLockImpetus extends BlockTABase implements IHorizontallyDirectionalBlock, IEldritchLock, IItemBlockProvider {

    public BlockEldritchLockImpetus() {
        super(Material.ROCK);
        setHardness(-1.0F);
        setResistance(6000000.0F);
        setDefaultState(getDefaultState().withProperty(IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.NORTH));
    }
    
    @Override
    public ItemBlock createItemBlock() {
        return new ItemBlock(this) {
            @Override
            @SideOnly(Side.CLIENT)
            public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip,
                    ITooltipFlag flagIn) {
                
                if (!TAConfig.disableCreativeOnlyText.getValue()) {
                    tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.creative_only").setStyle(
                            new Style().setColor(TextFormatting.DARK_PURPLE)).getFormattedText());
                }
                
                tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.eldritch_lock_type", new TextComponentTranslation(
                        "thaumicaugmentation.text.impetus")).getFormattedText());
            }
        };
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IHorizontallyDirectionalBlock.DIRECTION,
                ILockClosed.CLOSED);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(IHorizontallyDirectionalBlock.DIRECTION).getHorizontalIndex();
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.byHorizontalIndex(meta));
    }
    
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        boolean closed = true;
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEldritchLock)
            closed = ((TileEldritchLock) tile).isClosed();
        
        return state.withProperty(ILockClosed.CLOSED, closed);
    }
    
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {

        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(
                IHorizontallyDirectionalBlock.DIRECTION, placer.getHorizontalFacing().getOpposite());
    }
    
    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, rot.rotate(state.getValue(IHorizontallyDirectionalBlock.DIRECTION)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(IHorizontallyDirectionalBlock.DIRECTION)));
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
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {}
    
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
    
    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEldritchLock();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        String var = IHorizontallyDirectionalBlock.DIRECTION.getName() + "=" + EnumFacing.EAST.getName() + 
                "," + ILockClosed.CLOSED.getName() + "=true";
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(
                getRegistryName().toString(), var));
    }
    
}
