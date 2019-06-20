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

package thecodex6824.thaumicaugmentation.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.network.PacketFractureLocatorUpdate;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;
import thecodex6824.thaumicaugmentation.common.util.FractureLocatorSearchManager;

public class ItemFractureLocator extends ItemTABase {
    
    public ItemFractureLocator() {
        super();
        setMaxStackSize(1);
    }
    
    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (!world.isRemote && entity instanceof EntityPlayerMP) {
            if (FractureLocatorSearchManager.canPlayerRequestLocation((EntityPlayer) entity)) {
                BlockPos nearest = FractureLocatorSearchManager.findNearestFracture(world, entity.getPosition());
                if (nearest != null)
                    TANetwork.INSTANCE.sendTo(new PacketFractureLocatorUpdate(nearest), (EntityPlayerMP) entity);
                else
                    TANetwork.INSTANCE.sendTo(new PacketFractureLocatorUpdate(), (EntityPlayerMP) entity);
                
                FractureLocatorSearchManager.resetPlayerLocationTime((EntityPlayer) entity);
            }
        }
        else if (world.isRemote && world.getTotalWorldTime() % 20 == 0) {
            if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("found")) {
                int[] pos = stack.getTagCompound().getIntArray("pos");
                if (pos.length == 3)
                    System.out.printf("%d, %d, %d%n", pos[0], pos[1], pos[2]);
            }
            else
                System.out.println("Not found");
        }
    }
    
}
