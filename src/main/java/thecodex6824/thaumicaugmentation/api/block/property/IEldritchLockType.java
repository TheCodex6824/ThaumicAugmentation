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

package thecodex6824.thaumicaugmentation.api.block.property;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import thecodex6824.thaumicaugmentation.api.TAItems;

public interface IEldritchLockType {

    public enum LockType implements IStringSerializable {
        
        LABYRINTH(0, () -> new ItemStack(TAItems.ELDRITCH_LOCK_KEY, 1, 0)),
        PRISON(1, () -> new ItemStack(TAItems.ELDRITCH_LOCK_KEY, 1, 1)),
        LIBRARY(2, () -> new ItemStack(TAItems.ELDRITCH_LOCK_KEY, 1, 2)),
        BOSS(3, () -> new ItemStack(TAItems.ELDRITCH_LOCK_KEY, 1, 3));
        
        private int meta;
        private Supplier<ItemStack> keyGen;
        
        private LockType(int m, Supplier<ItemStack> k) {
            meta = m;
            keyGen = k;
        }
        
        public int getMeta() {
            return meta;
        }
        
        public boolean isKey(ItemStack input) {
            return ItemStack.areItemStacksEqual(keyGen.get(), input);
        }
        
        public ItemStack getKey() {
            return keyGen.get();
        }
        
        @Override
        public String getName() {
            return name().toLowerCase();
        }
        
        @Nullable
        public static LockType fromMeta(int id) {
            for (LockType type : values()) {
                if (type.getMeta() == id)
                    return type;
            }
            
            return null;
        }
        
    }
    
    public static final PropertyEnum<LockType> LOCK_TYPE = PropertyEnum.create("ta_lock_type", LockType.class);
    
}
