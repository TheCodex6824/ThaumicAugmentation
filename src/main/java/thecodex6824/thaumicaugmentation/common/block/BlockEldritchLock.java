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

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
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
import thecodex6824.thaumicaugmentation.api.TASounds;
import thecodex6824.thaumicaugmentation.api.block.property.IEldritchLockType;
import thecodex6824.thaumicaugmentation.api.block.property.IHorizontallyDirectionalBlock;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.block.trait.ILockClosed;
import thecodex6824.thaumicaugmentation.common.tile.TileEldritchLock;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;

public class BlockEldritchLock extends BlockTABase implements IHorizontallyDirectionalBlock, IEldritchLockType, IItemBlockProvider {

    public BlockEldritchLock() {
        super(Material.ROCK);
        setHardness(-1.0F);
        setResistance(6000000.0F);
        setDefaultState(getDefaultState().withProperty(IEldritchLockType.LOCK_TYPE, LockType.LABYRINTH).withProperty(
                IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.NORTH));
    }
    
    @Override
    public ItemBlock createItemBlock() {
        return new ItemMultiTexture(this, null, new String[] {
                "",
                "",
                "",
                ""
        }) {
            
            @Override
            @SideOnly(Side.CLIENT)
            public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip,
                    ITooltipFlag flagIn) {
                
                LockType type = LockType.fromMeta(stack.getMetadata());
                if (type != null) {
                tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.eldritch_lock_type", new TextComponentTranslation(
                        type.getKey().getTranslationKey() + ".name")).getFormattedText());
                }
                
                tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.creative_only").setStyle(
                        new Style().setColor(TextFormatting.DARK_PURPLE)).getFormattedText());
            }
            
            @Override
            public String getTranslationKey(ItemStack stack) {
                return block.getTranslationKey();
            }
            
        };
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IEldritchLockType.LOCK_TYPE, IHorizontallyDirectionalBlock.DIRECTION,
                ILockClosed.CLOSED);
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(IEldritchLockType.LOCK_TYPE).getMeta();
        meta = BitUtil.setBits(meta, 2, 4, state.getValue(IHorizontallyDirectionalBlock.DIRECTION).getHorizontalIndex());
        return meta;
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState().withProperty(IEldritchLockType.LOCK_TYPE, LockType.fromMeta(BitUtil.getBits(meta, 0, 2)));
        return state.withProperty(IHorizontallyDirectionalBlock.DIRECTION, EnumFacing.byHorizontalIndex(BitUtil.getBits(meta, 2, 4)));
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
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        
        if (world.isRemote)
            return true;
        else {
            ItemStack held = player.getHeldItem(hand);
            if (state.getValue(IEldritchLockType.LOCK_TYPE).isKey(held)) {
                TileEntity tile = world.getTileEntity(pos);
                if (tile instanceof TileEldritchLock) {
                    ((TileEldritchLock) tile).open();
                    if (!player.isCreative())
                        held.shrink(1);
                    
                    world.playSound(null, pos, TASounds.RIFT_ENERGY_ZAP, SoundCategory.BLOCKS, 1.0F, 0.9F);
                    return true;
                }
            }
        }
        
        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }
    
    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(IEldritchLockType.LOCK_TYPE).getMeta();
    }
    
    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
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
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == TAItems.CREATIVE_TAB || tab == CreativeTabs.SEARCH) {
            for (int i = 0; i < LockType.values().length; ++i)
                items.add(new ItemStack(this, 1, i));
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for (LockType t : LockType.values()) {
            String var = IHorizontallyDirectionalBlock.DIRECTION.getName() + "=" + EnumFacing.EAST.getName() + 
                    "," + ILockClosed.CLOSED.getName() + "=true," + IEldritchLockType.LOCK_TYPE.getName() + "=" + t.getName();
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), t.getMeta(), new ModelResourceLocation(
                    getRegistryName().toString(), var));
        }
    }
    
}
