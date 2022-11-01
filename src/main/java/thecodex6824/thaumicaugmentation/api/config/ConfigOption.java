/*
 *  Thaumic Augmentation
 *  Copyright (c) 2022 TheCodex6824.
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

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.relauncher.Side;

/*
 * Base class for the config option system.
 * @author TheCodex6824
 */
public abstract class ConfigOption<T> {

    protected boolean enforceServer;

    /*
     * Creates a new config option, optionally forcing the value to be synced from the server to the client.
     * This should not be used as the only method of preventing cheating - modified clients can choose to just
     * ignore the sent values.
     * @param enforceServer If the server side value should be sent to clients
     */
    public ConfigOption(boolean enforceServer) {
        this.enforceServer = enforceServer;
    }

    /*
     * Returns if this value should be synced to the passed side.
     * @param s The side to check
     * @return If this value should be synced TO the passed side
     */
    public boolean shouldSyncValue(Side s) {
        if (s == Side.CLIENT)
            return enforceServer;

        return false;
    }
    
    /*
     * Serializes this option into the passed buffer.
     * @param buf The buffer to write into
     */
    public abstract void serialize(ByteBuf buf);

    /*
     * Deserializes this option from the passed buffer.
     * @param buf The buffer to read from
     */
    public abstract void deserialize(ByteBuf buf);

    /*
     * Returns the value contained in this config option.
     * @return The stored value
     */
    public abstract T getValue();

    /*
     * Directly sets the value stored in this config option.
     * @param value The new value
     */
    public abstract void setValue(T value);

    /*
     * Sets the value of this config option, but only if the passed side is
     * not using values provided by the server.
     * @param value The new value to set
     * @param logicalSide The side to check
     */
    public void setValue(T value, Side logicalSide) {
        if (!shouldSyncValue(logicalSide))
            setValue(value);
    }

}
