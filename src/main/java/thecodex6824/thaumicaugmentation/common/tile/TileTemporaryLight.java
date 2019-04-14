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

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileTemporaryLight extends TileEntity implements ITickable {
	
	private static final int DELAY = 10;
	
	protected int ticksLeft;
	protected int lightLevel;
	
	public TileTemporaryLight() {
		super();
		ticksLeft = 1;
		lightLevel = 0;
	}
	
	@Override
	public void update() {
		if (!world.isRemote && world.getTotalWorldTime() % DELAY == 0) {
			ticksLeft -= DELAY;
			if (ticksLeft <= 0)
				world.setBlockToAir(pos);
		}
	}
	
	public void setLightLevel(int newLevel) {
		lightLevel = newLevel;
		markDirty();
		world.checkLight(pos);
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
	}
	
	public int getLightLevel() {
		return lightLevel;
	}
	
	public void setTicksRemaining(int ticks) {
		ticksLeft = ticks;
		markDirty();
	}
	
	public int getTicksRemaining() {
		return ticksLeft;
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, getBlockMetadata(), writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
		world.checkLight(pos);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("ticksLeft", ticksLeft);
		compound.setInteger("lightLevel", lightLevel);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		ticksLeft = compound.getInteger("ticksLeft");
		lightLevel = compound.getInteger("lightLevel");
		super.readFromNBT(compound);
	}
	
}
