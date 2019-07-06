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

package thecodex6824.thaumicaugmentation.api.config;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Manages syncing config values. Options must be added here to be automatically synced.
 * @author TheCodex6824
 */
public final class TAConfigManager {

    private TAConfigManager() {}
    
    private static ArrayList<ConfigOption<?>> config = new ArrayList<>();

    public static <T extends ConfigOption<?>> T addOption(T option) {
        config.add(option);
        return option;
    }

    public static void sync(Side logicalSide, ByteBuf buf) {
        for (ConfigOption<?> option : config) {
            if (option.shouldSyncValue(logicalSide))
                option.deserialize(buf);
        }
    }

    public static ByteBuf createSyncBuffer(Side targetSide) {
        ByteBuf buf = Unpooled.buffer();
        for (ConfigOption<?> option : config) {
            if (option.shouldSyncValue(targetSide))
                option.serialize(buf);
        }

        return buf;
    }

}
