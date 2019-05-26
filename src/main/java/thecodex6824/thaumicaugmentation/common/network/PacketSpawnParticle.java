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

package thecodex6824.thaumicaugmentation.common.network;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSpawnParticle implements IMessage {

    private EnumParticleTypes type;
    private double x, y, z;
    private double xSpeed, ySpeed, zSpeed;
    private int param;

    public PacketSpawnParticle() {}

    public PacketSpawnParticle(EnumParticleTypes particle, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int param) {
        type = particle;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.zSpeed = zSpeed;
        this.param = param;
    }

    public EnumParticleTypes getParticleType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getSpeedX() {
        return xSpeed;
    }

    public double getSpeedY() {
        return ySpeed;
    }

    public double getSpeedZ() {
        return zSpeed;
    }

    public int getParam() {
        return param;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        byte[] buffer = new byte[length];
        buf.readBytes(buffer);
        String name = new String(buffer, StandardCharsets.UTF_8);
        for (EnumParticleTypes t : EnumParticleTypes.values()) {
            if (t.name().equals(name))
                type = t;
        }

        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        xSpeed = buf.readDouble();
        ySpeed = buf.readDouble();
        zSpeed = buf.readDouble();
        param = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(type.name().length());
        buf.writeBytes(type.name().getBytes(StandardCharsets.UTF_8));
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(xSpeed);
        buf.writeDouble(ySpeed);
        buf.writeDouble(zSpeed);
        buf.writeInt(param);
    }

    public static class Handler implements IMessageHandler<PacketSpawnParticle, IMessage> {

        @Override
        public IMessage onMessage(PacketSpawnParticle message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft.getMinecraft().world.spawnParticle(message.getParticleType(), message.getX(), message.getY(), message.getZ(),
                        message.getSpeedX(), message.getSpeedY(), message.getSpeedZ(), message.getParam());
            });

            return null;
        }

    }

}
