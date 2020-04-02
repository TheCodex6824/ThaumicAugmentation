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

package thecodex6824.thaumicaugmentation.api.warded.tile;

import java.lang.ref.WeakReference;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Constants.NBT;
import thecodex6824.thaumicaugmentation.api.event.WardedTilePermissionEvent;
import thecodex6824.thaumicaugmentation.api.item.IWardAuthenticator;
import thecodex6824.thaumicaugmentation.api.warded.WardHelper;

/**
 * Default implementation of {@link IWardedTile}.
 * @author TheCodex6824
 */
public class WardedTile implements IWardedTile, INBTSerializable<NBTTagCompound> {

    protected static final UUID DEFAULT_UUID = new UUID(0, 0);
    
    protected UUID owner;
    protected WeakReference<TileEntity> tile;
    
    public WardedTile(TileEntity t) {
        owner = DEFAULT_UUID;
        tile = new WeakReference<>(t);
    }
    
    @Override
    public UUID getOwner() {
        return owner;
    }
    
    @Override
    public void setOwner(UUID uuid) {
        owner = uuid;
    }
    
    @Override
    public String getUniqueTypeID() {
        return tile.get() != null ? TileEntity.getKey(tile.get().getClass()).toString() : "";
    }
    
    @Override
    public BlockPos getPosition() {
        return tile.get() != null ? tile.get().getPos() : BlockPos.ORIGIN;
    }
    
    protected boolean checkPermission(EntityLivingBase entity) {
        if (entity == null)
            return false;
        else if (owner.equals(entity.getUniqueID()))
            return true;
        else if (WardHelper.doesEntityHaveSpecialPermission(entity))
            return true;
        else if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            ItemStack stack = null;
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                stack = player.inventory.getStackInSlot(i);
                if (stack.getItem() instanceof IWardAuthenticator && 
                        ((IWardAuthenticator) stack.getItem()).permitsUsage(this, stack, player)) {

                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean hasPermission(EntityLivingBase player) {
        if (tile.get() != null) {
            TileEntity t = tile.get();
            WardedTilePermissionEvent event = new WardedTilePermissionEvent(t.getWorld(), t.getPos(), t.getWorld().getBlockState(t.getPos()), player, checkPermission(player));
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled()) {
                switch (event.getResult()) {
                    case ALLOW: return true;
                    case DENY: return false;
                    default: return event.isAllowed();
                }
            }
        }
        
        return false;
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setUniqueId("owner", owner);
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("owner", NBT.TAG_STRING))
            owner = UUID.fromString(nbt.getString("owner"));
        else
            owner = nbt.getUniqueId("owner");
    }
    
}
