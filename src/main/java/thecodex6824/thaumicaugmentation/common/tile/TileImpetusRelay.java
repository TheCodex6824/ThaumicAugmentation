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

package thecodex6824.thaumicaugmentation.common.tile;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import thaumcraft.api.casters.IInteractWithCaster;
import thecodex6824.thaumicaugmentation.api.impetus.node.CapabilityImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.IImpetusNode;
import thecodex6824.thaumicaugmentation.api.impetus.node.prefab.ImpetusNode;
import thecodex6824.thaumicaugmentation.api.util.DimensionalBlockPos;

public class TileImpetusRelay extends TileEntity implements IInteractWithCaster {

    protected IImpetusNode node;
    
    public TileImpetusRelay() {
        node = new ImpetusNode(2, 2);
    }
    
    @Override
    public boolean onCasterRightClick(World world, ItemStack stack, EntityPlayer player, BlockPos pos, 
            EnumFacing face, EnumHand hand) {
        
        return CommonImpetusOperations.handleCasterInteract(this, world, stack, player, pos, face, hand);
    }
    
    @Override
    public void setPos(BlockPos posIn) {
        super.setPos(posIn);
        node.setLocation(new DimensionalBlockPos(pos, world.provider.getDimension()));
    }
    
    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        node.setLocation(new DimensionalBlockPos(pos, world.provider.getDimension()));
    }
    
    @Override
    public void onLoad() {
        node.init();
    }
    
    @Override
    public void onChunkUnload() {
        node.destroy();
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("node", node.serializeNBT());
        return super.writeToNBT(tag);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        node.deserializeNBT(nbt.getCompoundTag("node"));
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE)
            return true;
        else
            return super.hasCapability(capability, facing);
    }
    
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityImpetusNode.IMPETUS_NODE)
            return CapabilityImpetusNode.IMPETUS_NODE.cast(node);
        else
            return super.getCapability(capability, facing);
    }
    
}
