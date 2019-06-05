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

import java.util.Random;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.fx.FXDispatcher;

public class PacketParticleEffect implements IMessage {

    // if something ever needs > 256 doubles (!), this can be increased
    public static int maxPacketData = 256;
    
    public static enum ParticleEffect {
        VIS_REGENERATOR(0),
        VOID_STREAKS(1);
        
        private int id;
        
        private ParticleEffect(int i) {
            id = i;
        }
        
        public int getID() {
            return id;
        }
        
        @Nullable
        public static ParticleEffect fromID(int id) {
            for (ParticleEffect effect : values()) {
                if (effect.getID() == id)
                    return effect;
            }
            
            return null;
        }
    }
    
    private double[] data;
    private ParticleEffect effect;
    
    public PacketParticleEffect() {}
    
    public PacketParticleEffect(ParticleEffect particle, double... d) {
        effect = particle;
        data = d;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        effect = ParticleEffect.fromID(buf.readInt());
        int length = buf.readInt();
        data = new double[length > maxPacketData ? maxPacketData : length];
        for (int i = 0; i < data.length; ++i)
            data[i] = buf.readDouble();
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(effect.getID());
        buf.writeInt(data.length > maxPacketData ? maxPacketData : data.length);
        for (int i = 0; i < Math.min(data.length, maxPacketData); ++i)
            buf.writeDouble(data[i]);
    }
    
    public double[] getData() {
        return data;
    }
    
    public ParticleEffect getEffect() {
        return effect;
    }
    
    public static class Handler implements IMessageHandler<PacketParticleEffect, IMessage> {

        @Override
        public IMessage onMessage(PacketParticleEffect message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Random rand = Minecraft.getMinecraft().world.rand;
                switch (message.effect) {
                    case VIS_REGENERATOR: {
                        double[] d = message.getData();
                        if (d.length == 3) {
                            for (int i = 0; i < rand.nextInt(3) + 3; ++i) {
                                double x = d[0] + rand.nextGaussian() / 4, y = d[1] + rand.nextDouble() / 2, z = d[2] + rand.nextGaussian() / 4;
                                double vX = rand.nextGaussian() / 4, vY = rand.nextDouble() / 2, vZ = rand.nextGaussian() / 4;
                                FXDispatcher.INSTANCE.drawVentParticles(x, y, z, vX, vY, vZ, Aspect.AURA.getColor());
                            }
                        }
                    }
                    case VOID_STREAKS: {
                        double[] d = message.getData();
                        if (d.length == 7) {
                            double x1 = d[0], y1 = d[1], z1 = d[2];
                            double x2 = d[3], y2 = d[4], z2 = d[5];
                            float scale = (float) d[6];
                            FXDispatcher.INSTANCE.voidStreak(x1, y1, z1, x2, y2, z2, rand.nextInt(), scale);
                        }
                    }
                }
            });

            return null;
        }

    }
    
}
