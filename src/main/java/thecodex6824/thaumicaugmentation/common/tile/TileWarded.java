/**
 *	Thaumic Augmentation
 *	Copyright (c) 2019 TheCodex6824.
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
import net.minecraftforge.common.MinecraftForge;
import thaumcraft.api.casters.ICaster;
import thaumcraft.api.casters.IInteractWithCaster;
import thecodex6824.thaumicaugmentation.api.event.WardedBlockPermissionEvent;
import thecodex6824.thaumicaugmentation.api.tile.IWardedTile;

public abstract class TileWarded extends TileEntity implements IInteractWithCaster, IWardedTile {

	protected String owner;
	
	public TileWarded() {
		super();
		owner = "";
	}
	
	@Override
	public void setOwner(String uuid) {
		owner = uuid;
		markDirty();
	}
	
	@Override
	public String getOwner() {
		return owner;
	}
	
	@Override
	public boolean hasPermission(EntityPlayer player) {
		WardedBlockPermissionEvent event = new WardedBlockPermissionEvent(world, pos, world.getBlockState(pos), player);
		MinecraftForge.EVENT_BUS.post(event);
		if (!event.isCanceled()) {
			switch (event.getResult()) {
				case ALLOW: return true;
				case DENY: return false;
				default: return owner.equals(player.getUniqueID().toString());
			}
		}
		else
			return false;
	}
	
	@Override
	public boolean onCasterRightClick(World world, ItemStack stack, EntityPlayer player, BlockPos pos, EnumFacing facing,
			EnumHand hand) {
		if (!world.isRemote) {
			if (hasPermission(player)) {
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
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString("owner", owner);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		owner = compound.getString("owner");
		super.readFromNBT(compound);
	}
	
}
