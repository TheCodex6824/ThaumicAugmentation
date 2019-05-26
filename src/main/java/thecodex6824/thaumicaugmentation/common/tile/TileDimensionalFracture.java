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

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import thaumcraft.common.entities.monster.EntityEldritchGuardian;

public class TileDimensionalFracture extends TileEntity implements ITickable {

    protected int linkedDim;
    protected BlockPos linkedTo;
    protected boolean linkLocated;
    protected boolean linkInvalid;
    protected boolean open;

    @Override
    public void update() {
        if (!world.isRemote && open && world.getTotalWorldTime() % 20 == 0 && world.getGameRules().getBoolean("doMobSpawning") && world.rand.nextInt(2000) < world.getDifficulty().getId()) {
            if (world.isBlockNormalCube(pos.down(), false) || world.isBlockNormalCube(pos.down(2), false)) {
                BlockPos position = pos.down();
                EntityEldritchGuardian guardian = new EntityEldritchGuardian(world);
                guardian.setLocationAndAngles(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5, world.rand.nextInt(360), 0);
                guardian.setAbsorptionAmount(guardian.getAbsorptionAmount() + 
                        (float) guardian.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() / 2);
                guardian.timeUntilPortal = guardian.getPortalCooldown();
                world.spawnEntity(guardian);
            }
        }
    }

    public void setLinkedPosition(BlockPos pos) {
        linkedTo = pos;
        markDirty();
    }

    public BlockPos getLinkedPosition() {
        return linkedTo;
    }

    public void setLinkedDimension(int dim) {
        linkedDim = dim;
        markDirty();
    }

    public int getLinkedDimension() {
        return linkedDim;
    }

    public void setLinkLocated() {
        linkLocated = true;
        markDirty();
    }

    public boolean wasLinkLocated() {
        return linkLocated;
    }

    public void setLinkInvalid() {
        linkInvalid = true;
        markDirty();
    }

    public boolean isLinkInvalid() {
        return linkInvalid;
    }

    public void setOpen(boolean o) {
        open = o;
        markDirty();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 1, writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 16384.0;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.down(), pos.up());
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (linkedTo != null) {
            compound.setInteger("linkedDim", linkedDim);
            compound.setIntArray("linkedPos", new int[] {linkedTo.getX(), linkedTo.getY(), linkedTo.getZ()});
            compound.setBoolean("linkLocated", linkLocated);
            compound.setBoolean("linkInvalid", linkInvalid);
            compound.setBoolean("open", open);
        }

        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("linkedPos", NBT.TAG_INT_ARRAY)) {
            linkedDim = compound.getInteger("linkedDim");
            int[] pos = compound.getIntArray("linkedPos");
            linkedTo = new BlockPos(pos[0], pos[1], pos[2]);
            linkLocated = compound.getBoolean("linkLocated");
            linkInvalid = compound.getBoolean("linkInvalid");
            open = compound.getBoolean("open");
        }

        super.readFromNBT(compound);
    }

}
