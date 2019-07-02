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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thaumcraft.api.casters.ICaster;
import thaumcraft.api.casters.IInteractWithCaster;
import thecodex6824.thaumicaugmentation.api.warded.CapabilityWardedTile;
import thecodex6824.thaumicaugmentation.api.warded.IWardedTile;
import thecodex6824.thaumicaugmentation.api.warded.WardedTile;

public abstract class TileWarded extends TileEntity implements IInteractWithCaster, ICapabilityProvider {

    protected IWardedTile ward;

    public TileWarded() {
        super();
        ward = new WardedTile(this);
    }

    @Override
    public boolean onCasterRightClick(World world, ItemStack stack, EntityPlayer player, BlockPos pos, EnumFacing facing,
            EnumHand hand) {
        if (!world.isRemote) {
            if (ward.hasPermission(player)) {
                if (stack.getItem() instanceof ICaster && player.isSneaking())
                    world.destroyBlock(pos, !player.isCreative());

                return true;
            }
            else
                return false;
        }

        return true;
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityWardedTile.WARDED_TILE ? true : super.hasCapability(capability, facing);
    }
    
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityWardedTile.WARDED_TILE)
            return CapabilityWardedTile.WARDED_TILE.cast(ward);
        else
            return super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString("owner", ward.getOwner());
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        ward.setOwner(compound.getString("owner"));
        super.readFromNBT(compound);
    }

}
