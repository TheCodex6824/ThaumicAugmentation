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

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants.NBT;
import thecodex6824.thaumicaugmentation.api.TABlocks;
import thecodex6824.thaumicaugmentation.api.ThaumicAugmentationAPI;
import thecodex6824.thaumicaugmentation.api.tile.CapabilityRiftJar;
import thecodex6824.thaumicaugmentation.api.tile.IRiftJar;
import thecodex6824.thaumicaugmentation.api.util.FluxRiftReconstructor;
import thecodex6824.thaumicaugmentation.client.renderer.item.RenderItemBlockRiftJar;
import thecodex6824.thaumicaugmentation.common.entity.EntityItemBlockRiftJar;
import thecodex6824.thaumicaugmentation.common.util.IModelProvider;

public class ItemBlockRiftJar extends ItemBlock implements IModelProvider<Item> {

    public ItemBlockRiftJar() {
        super(TABlocks.RIFT_JAR);
        setMaxStackSize(1);
        setHasSubtypes(true);
    }
    
    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, IBlockState newState) {
        
        boolean placed = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if (placed && !world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null && stack.hasTagCompound()) {
                IRiftJar rift = tile.getCapability(CapabilityRiftJar.RIFT_JAR, null);
                if (rift != null) {
                    rift.setRift(new FluxRiftReconstructor(stack.getTagCompound().getInteger("seed"),
                            stack.getTagCompound().getInteger("size")));
                }
            }
        }
        
        return placed;
    }
    
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("seed", NBT.TAG_INT) &&
                stack.getTagCompound().getInteger("size") > 0;
    }
    
    @Override
    @Nullable
    public Entity createEntity(World world, Entity location, ItemStack stack) {
        EntityItemBlockRiftJar item = new EntityItemBlockRiftJar(world, location.posX, location.posY, location.posZ, stack);
        item.setDefaultPickupDelay();
        item.setNoDespawn();
        item.motionX = location.motionX;
        item.motionY = location.motionY;
        item.motionZ = location.motionZ;
        if (location instanceof EntityItem) {
            item.setThrower(((EntityItem) location).getThrower());
            item.setOwner(((EntityItem) location).getOwner());
        }
        
        return item;
    }
    
    @Override
    public String getTranslationKey(ItemStack stack) {
        return hasCustomEntity(stack) ? super.getTranslationKey(stack) : "tile.thaumicaugmentation.rift_jar_empty";
    }
    
    @Override
    public void registerModels() {
        setTileEntityItemStackRenderer(new RenderItemBlockRiftJar());
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(
                "ta_special:renderer_builtin:" + ThaumicAugmentationAPI.MODID + ":rift_jar", "inventory"));
    }
    
}
