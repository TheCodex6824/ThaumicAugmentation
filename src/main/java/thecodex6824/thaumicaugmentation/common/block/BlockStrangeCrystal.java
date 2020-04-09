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

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.TAMaterials;
import thecodex6824.thaumicaugmentation.api.aspect.AspectUtil;
import thecodex6824.thaumicaugmentation.api.block.property.IDirectionalBlock;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType;
import thecodex6824.thaumicaugmentation.api.block.property.ITAStoneType.StoneType;
import thecodex6824.thaumicaugmentation.common.block.prefab.BlockTABase;
import thecodex6824.thaumicaugmentation.common.block.trait.IItemBlockProvider;
import thecodex6824.thaumicaugmentation.common.block.trait.IStoredBlockstate;

public class BlockStrangeCrystal extends BlockTABase implements IDirectionalBlock, IItemBlockProvider {

    protected static final AxisAlignedBB DOWN_BOX = new AxisAlignedBB(0.125, 0.25, 0.125, 0.875, 1.0, 0.875);
    protected static final AxisAlignedBB EAST_BOX = new AxisAlignedBB(0.0, 0.125, 0.125, 0.75, 0.875, 0.875);
    protected static final AxisAlignedBB NORTH_BOX = new AxisAlignedBB(0.125, 0.125, 0.25, 0.875, 0.875, 1.0);
    protected static final AxisAlignedBB SOUTH_BOX = new AxisAlignedBB(0.125, 0.125, 0.0, 0.875, 0.875, 0.75);
    protected static final AxisAlignedBB UP_BOX = new AxisAlignedBB(0.125, 0.0, 0.125, 0.875, 0.75, 0.875);
    protected static final AxisAlignedBB WEST_BOX = new AxisAlignedBB(0.25, 0.125, 0.125, 1.0, 0.875, 0.875);
    
    public BlockStrangeCrystal() {
        super(Material.GLASS);
        setHardness(0.25F);
        setDefaultState(getDefaultState().withProperty(IDirectionalBlock.DIRECTION, EnumFacing.UP));
    }
    
    @Override
    public ItemBlock createItemBlock() {
        return new ItemBlock(this) {
            @Override
            public IRarity getForgeRarity(ItemStack stack) {
                return TAMaterials.RARITY_ELDRITCH;
            }
        };
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(IDirectionalBlock.DIRECTION).add(
                IStoredBlockstate.BLOCKSTATE).build();
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(IDirectionalBlock.DIRECTION, EnumFacing.byIndex(meta));
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(IDirectionalBlock.DIRECTION).getIndex();
    }
    
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        BlockPos check = pos.offset(state.getValue(IDirectionalBlock.DIRECTION).getOpposite());
        IBlockState toStore = world.getBlockState(check);
        return ((IExtendedBlockState) state).withProperty(IStoredBlockstate.BLOCKSTATE,
                toStore.getBlock().isAir(toStore, world, check) ? 
                TABlocks.STONE.getDefaultState().withProperty(ITAStoneType.STONE_TYPE, StoneType.STONE_CRUSTED) :
                toStore);
    }
    
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {

        return getDefaultState().withProperty(IDirectionalBlock.DIRECTION, facing);
    }
    
    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        BlockPos test = pos.offset(side.getOpposite());
        return world.getBlockState(test).isSideSolid(world, test, side);
    }
    
    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }
    
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(IDirectionalBlock.DIRECTION)) {
            case DOWN:  return DOWN_BOX;
            case EAST:  return EAST_BOX;
            case NORTH: return NORTH_BOX;
            case SOUTH: return SOUTH_BOX;
            case WEST:  return WEST_BOX;
            case UP:
            default:    return UP_BOX;
        }
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(IDirectionalBlock.DIRECTION, rot.rotate(state.getValue(IDirectionalBlock.DIRECTION)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(IDirectionalBlock.DIRECTION)));
    }
    
    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 15;
    }
    
    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 0;
    }
    
    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return null;
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
    public boolean isTranslucent(IBlockState state) {
        return true;
    }
    
    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        return SoundsTC.CRYSTAL;
    }
    
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
    
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {
        
        Random rand = world instanceof World ? ((World) world).rand : RANDOM;
        for (int i = 0; i < rand.nextInt(3) + (fortune > 0 ? rand.nextInt(fortune) : 0) + 1; ++i)
            drops.add(ThaumcraftApiHelper.makeCrystal(AspectUtil.getRandomAspect(rand)));
        
        int special = 50;
        if (fortune > 0)
            special /= fortune;
        
        if (rand.nextInt(Math.max(special, 1)) == 0)
            drops.add(new ItemStack(ItemsTC.curio, 1, 0));
    }
    
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        EnumFacing dir = state.getValue(IDirectionalBlock.DIRECTION).getOpposite();
        if (!world.getBlockState(pos.offset(dir)).isSideSolid(world, pos.offset(dir), dir.getOpposite())) {
            world.setBlockToAir(pos);
            dropBlockAsItem(world, pos, state, 0);
        }
        else if (fromPos.equals(pos.offset(dir)))
            world.markBlockRangeForRenderUpdate(pos, pos);
    }
    
    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }
    
    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this), 1, 0);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        FXGeneric fx = new FXGeneric(world, pos.getX() + rand.nextFloat(),
                pos.getY() + rand.nextFloat(), pos.getZ() + rand.nextFloat(), 0.0, 0.0, 0.0);
        fx.setMaxAge(40 + rand.nextInt(21));
        fx.setRBGColorF(0.8F, 0.8F, 0.8F);
        fx.setAlphaF(0.0F, 0.6F, 0.6F, 0.0F);
        fx.setGridSize(64);
        fx.setParticles(512, 16, 1);
        fx.setScale(1.0F, 0.5F);
        fx.setLoop(true);
        fx.setNoClip(false);
        fx.setWind(0.001D);
        fx.setGravity(0.03F);
        fx.setRandomMovementScale(0.0025F, 0.0F, 0.0025F);
        ParticleEngine.addEffect(world, fx);
    }
    
}
