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

package thecodex6824.thaumicaugmentation.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import thecodex6824.thaumicaugmentation.common.container.ContainerWardedChest;
import thecodex6824.thaumicaugmentation.common.tile.TileWardedChest;

public class GUIHandler implements IGuiHandler {

    public enum TAInventory {

        WARDED_CHEST(0);

        private int id;

        private TAInventory(int id) {
            this.id = id;
        }

        public int getID() {
            return id;
        }
    }

    @Override
    public Container getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (TAInventory.values()[ID]) {
            case WARDED_CHEST: return new ContainerWardedChest(player.inventory, 
                (TileWardedChest) world.getTileEntity(new BlockPos(x, y, z)));
            default: return null;
        }
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (TAInventory.values()[ID]) {
            case WARDED_CHEST: return new GUIWardedChest(getServerGuiElement(ID, player, world, x, y, z), player.inventory);
            default: return null;
        }
    }

}
