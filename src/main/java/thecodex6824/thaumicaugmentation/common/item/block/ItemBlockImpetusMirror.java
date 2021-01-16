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

package thecodex6824.thaumicaugmentation.common.item.block;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.common.lib.SoundsTC;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;
import thecodex6824.thaumicaugmentation.common.tile.TileImpetusMirror;
import thecodex6824.thaumicaugmentation.common.util.IModelProvider;

public class ItemBlockImpetusMirror extends ItemBlockNoImpetusNodeNBT implements IModelProvider<Item> {

    public ItemBlockImpetusMirror() {
        super(TABlocks.IMPETUS_MIRROR);
        setHasSubtypes(true);
        addPropertyOverride(new ResourceLocation(ThaumicAugmentationAPI.MODID, "linked"), new IItemPropertyGetter() {
            @Override
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                return stack.hasTagCompound() && stack.getTagCompound().hasKey("link", NBT.TAG_INT_ARRAY) ?
                        1.0F : 0.0F;
            }
        });
    }
    
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
            float hitY, float hitZ, EnumHand hand) {
        
        if (world.isRemote) {
            player.swingArm(hand);
            return EnumActionResult.SUCCESS;
        }
        else if (world.isBlockModifiable(player, pos)){
            TileEntity target = world.getTileEntity(pos);
            if (target instanceof TileImpetusMirror) {
                TileImpetusMirror mirror = (TileImpetusMirror) target;
                IImpetusNode node = mirror.getCapability(CapabilityImpetusNode.IMPETUS_NODE, null);
                DimensionalBlockPos ourPos = new DimensionalBlockPos(mirror.getPos(), mirror.getWorld().provider.getDimension());
                if (mirror.getLink().isInvalid() || node.getGraph().findNodeByPosition(mirror.getLink()) == null) {
                    ItemStack copy = player.getHeldItem(hand).copy();
                    copy.setCount(1);
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setIntArray("link", ourPos.toArray());
                    copy.setTagCompound(tag);
                    world.playSound(null, pos, SoundsTC.jar, SoundCategory.BLOCKS, 1.0F, 2.0F);
                    if (!player.isCreative())
                        player.getHeldItem(hand).shrink(1);
                    if (!player.inventory.addItemStackToInventory(copy))
                        world.spawnEntity(copy.getItem().createEntity(world, player, copy));
                    
                    return EnumActionResult.SUCCESS;
                }
                else {
                    player.sendStatusMessage(new TextComponentTranslation("thaumicaugmentation.text.mirror_already_linked"), true);
                    return EnumActionResult.FAIL;
                }
            }
        }
        
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }
    
    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, IBlockState newState) {
        
        boolean placed = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if (placed && !world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileImpetusMirror && stack.hasTagCompound() && stack.getTagCompound().hasKey("link", NBT.TAG_INT_ARRAY))
                ((TileImpetusMirror) tile).setLink(new DimensionalBlockPos(stack.getTagCompound().getIntArray("link")));
        }
        
        return placed;
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (!block.isReplaceable(world, pos))
            pos = pos.offset(facing);

        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty() && player.canPlayerEdit(pos, facing, stack) && world.mayPlace(this.block, pos, false, facing, player)) {
            int meta = getMetadata(stack.getMetadata());
            IBlockState placedState = this.block.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, player, hand);
            if (placeBlockAt(stack, player, world, pos, facing, hitX, hitY, hitZ, placedState)) {
                placedState = world.getBlockState(pos);
                SoundType soundtype = placedState.getBlock().getSoundType(placedState, world, pos, player);
                world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                stack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        }
        else
            return EnumActionResult.FAIL;
    }
    
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("link", NBT.TAG_INT_ARRAY)) {
            int[] data = stack.getTagCompound().getIntArray("link");
            if (data.length == 4) {
                String dimName;
                try {
                    dimName = DimensionManager.getProviderType(data[3]).getName();
                }
                catch (IllegalArgumentException ex) {
                    dimName = "Unknown";
                }
                
                tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.mirror_link", data[0], data[1], data[2], dimName).getFormattedText());
            }
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName().toString(), "inventory"));
    }
    
}
