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

package thecodex6824.thaumicaugmentation.common.capability;

import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import thecodex6824.thaumicaugmentation.api.item.IWardAuthenticator;
import thecodex6824.thaumicaugmentation.api.warded.tile.IWardedTile;

public class WardAuthenticatorKey implements IWardAuthenticator, INBTSerializable<NBTTagCompound> {

    protected static final UUID DEFAULT_UUID = new UUID(0, 0);
    
    protected UUID owner;
    protected String name;
    
    public WardAuthenticatorKey() {
        owner = DEFAULT_UUID;
        name = "";
    }
    
    public void setOwner(UUID newOwner) {
        owner = newOwner;
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public void setOwnerName(String newName) {
        name = newName;
    }
    
    public String getOwnerName() {
        return name;
    }
    
    public void reset() {
        owner = DEFAULT_UUID;
        name = "";
    }
    
    @Override
    public boolean permitsUsage(IWardedTile tile, ItemStack stack, EntityLivingBase user) {
        return user.getUniqueID().equals(owner);
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        owner = nbt.getUniqueId("owner");
        name = nbt.getString("name");
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setUniqueId("owner", owner);
        tag.setString("name", name);
        return tag;
    }
    
}
