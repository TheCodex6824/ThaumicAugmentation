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
import net.minecraft.util.math.BlockPos;
import thecodex6824.thaumicaugmentation.api.ward.tile.IWardedTile;

public class WardAuthenticatorThaumiumKey extends WardAuthenticatorKey {

    protected UUID owner;
    protected String type;
    protected String typeName;
    protected BlockPos loc;
    
    public WardAuthenticatorThaumiumKey() {
        super();
        type = "";
        typeName = "";
        loc = new BlockPos(0, 0, 0);
    }
    
    public String getBoundType() {
        return type;
    }
    
    public void setBoundType(String newType) {
        type = newType;
    }
    
    public String getBoundTypeName() {
        return typeName;
    }
    
    public void setBoundTypeName(String newName) {
        typeName = newName;
    }
    
    public BlockPos getBoundPosition() {
        return loc;
    }
    
    public void setBoundPosition(BlockPos position) {
        loc = position.toImmutable();
    }
    
    @Override
    public boolean permitsUsage(IWardedTile tile, ItemStack stack, EntityLivingBase user) {
        return super.permitsUsage(tile, stack, user) && tile.getUniqueTypeID().equals(type) &&
                tile.getPosition().equals(loc);
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        type = nbt.getString("type");
        typeName = nbt.getString("typeName");
        int[] coords = nbt.getIntArray("pos");
        if (coords.length == 3)
            loc = new BlockPos(coords[0], coords[1], coords[2]);
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setString("type", type);
        tag.setString("typeName", typeName);
        tag.setIntArray("pos", new int[] {loc.getX(), loc.getY(), loc.getZ()});
        return tag;
    }
    
}
