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

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import thaumcraft.api.casters.ICaster;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.block.property.IArcaneTerraformerHalf;
import thecodex6824.thaumicaugmentation.api.block.property.IEnabledBlock;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.item.block.ItemBlockNoImpetusNodeNBT;
import thecodex6824.thaumicaugmentation.common.tile.TileArcaneTerraformer;
import thecodex6824.thaumicaugmentation.common.util.BitUtil;
import thecodex6824.thaumicaugmentation.init.GUIHandler;

public class BlockArcaneTerraformer extends BlockTABase implements IArcaneTerraformerHalf, IItemBlockProvider {

    protected static final AxisAlignedBB BOX_LOWER = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    protected static final AxisAlignedBB BOX_UPPER = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.55, 1.0);
    
    public BlockArcaneTerraformer() {
        super(Material.IRON);
        setHardness(1.5F);
        setResistance(15.0F);
        setSoundType(SoundType.METAL);
        setDefaultState(getDefaultState().withProperty(IArcaneTerraformerHalf.TERRAFORMER_HALF, ArcaneTerraformerHalf.LOWER));
    }
    
    @Override
    public ItemBlock createItemBlock() {
        return new ItemBlockNoImpetusNodeNBT(this);
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IArcaneTerraformerHalf.TERRAFORMER_HALF, IEnabledBlock.ENABLED);
    }
    
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(IArcaneTerraformerHalf.TERRAFORMER_HALF)) {
            case UPPER: return BOX_UPPER;
            case LOWER:
            default:
                return BOX_LOWER;
        }
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return BitUtil.setBit(0, 0, state.getValue(IArcaneTerraformerHalf.TERRAFORMER_HALF) == ArcaneTerraformerHalf.UPPER);
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(IArcaneTerraformerHalf.TERRAFORMER_HALF, meta == 0 ?
                ArcaneTerraformerHalf.LOWER : ArcaneTerraformerHalf.UPPER);
    }
    
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state.getValue(IArcaneTerraformerHalf.TERRAFORMER_HALF) == ArcaneTerraformerHalf.LOWER) {
            TileEntity te = world.getTileEntity(pos);
            if (te != null) {
                IItemHandler items = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (items != null && !items.getStackInSlot(0).isEmpty()) {
                    state = state.withProperty(IEnabledBlock.ENABLED, true);
                    return state;
                }
            }
            
            state = state.withProperty(IEnabledBlock.ENABLED, false);
        }
        
        return state;
    }
    
    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }
    
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return state.getValue(IArcaneTerraformerHalf.TERRAFORMER_HALF) == ArcaneTerraformerHalf.LOWER;
    }
    
    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileArcaneTerraformer();
    }
    
    protected void dropContentsAndCleanup(World world, BlockPos pos) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileArcaneTerraformer) {
                ((TileArcaneTerraformer) te).endTerraforming(true);
                IItemHandler items = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (items != null) {
                    for (int i = 0; i < items.getSlots(); ++i) {
                        ItemStack stack = items.getStackInSlot(i);
                        if (!stack.isEmpty())
                            world.spawnEntity(new EntityItem(world, pos.getX() + 0.5 + world.rand.nextGaussian() / 2, 
                                    pos.getY() + 0.5 + Math.abs(world.rand.nextGaussian() / 2), pos.getZ() + 0.5 + world.rand.nextGaussian() / 2, stack));
                    }
                }
            }
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(IArcaneTerraformerHalf.TERRAFORMER_HALF) == ArcaneTerraformerHalf.LOWER) {
            dropContentsAndCleanup(world, pos);
            IBlockState up = world.getBlockState(pos.up());
            if (up.getBlock() != this || up.getValue(IArcaneTerraformerHalf.TERRAFORMER_HALF) == ArcaneTerraformerHalf.UPPER)
                world.setBlockToAir(pos.up());
        }
        else {
            IBlockState down = world.getBlockState(pos.down());
            if (down.getBlock() != this || down.getValue(IArcaneTerraformerHalf.TERRAFORMER_HALF) == ArcaneTerraformerHalf.LOWER)
                world.setBlockToAir(pos.down());
        }
        
        super.breakBlock(world, pos, state);
    }
    
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (state.getValue(IArcaneTerraformerHalf.TERRAFORMER_HALF) == ArcaneTerraformerHalf.UPPER) {
            IBlockState down = world.getBlockState(pos.down());
            if (down.getBlock() != this || down.getValue(IArcaneTerraformerHalf.TERRAFORMER_HALF) != ArcaneTerraformerHalf.LOWER)
                world.setBlockToAir(pos);
        }
        else if (state.getValue(IArcaneTerraformerHalf.TERRAFORMER_HALF) == ArcaneTerraformerHalf.LOWER) {
            IBlockState up = world.getBlockState(pos.up());
            if (up.getBlock() != this || up.getValue(IArcaneTerraformerHalf.TERRAFORMER_HALF) != ArcaneTerraformerHalf.UPPER)
                world.setBlockToAir(pos);
        }
    }
    
    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.isAirBlock(pos.up()) && !world.isOutsideBuildHeight(pos.up());
    }
    
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
            ItemStack stack) {
 
        world.setBlockState(pos.up(), getDefaultState().withProperty(IArcaneTerraformerHalf.TERRAFORMER_HALF,
                ArcaneTerraformerHalf.UPPER));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (state.getValue(IArcaneTerraformerHalf.TERRAFORMER_HALF) == ArcaneTerraformerHalf.UPPER)
            pos = pos.down();
        
        if (!world.isRemote && world.getTileEntity(pos) instanceof TileArcaneTerraformer) {
            if (!(player.getHeldItem(hand).getItem() instanceof ICaster) || !player.isSneaking()) {
                player.openGui(ThaumicAugmentation.instance, GUIHandler.TAInventory.ARCANE_TERRAFORMER.getID(), world, 
                        pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
            else
                return false;
        }

        return true;
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
        return state.getValue(IArcaneTerraformerHalf.TERRAFORMER_HALF) == ArcaneTerraformerHalf.LOWER ?
                EnumBlockRenderType.MODEL : EnumBlockRenderType.INVISIBLE;
    }
    
}
