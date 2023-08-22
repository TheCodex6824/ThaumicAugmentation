package thecodex6824.thaumicaugmentation.common.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;

public class PacketSyncTagCompound implements IMessage {

	protected NBTTagCompound tag;
    
    public PacketSyncTagCompound() {}
    
    public PacketSyncTagCompound(NBTTagCompound toSend) {
        tag = toSend;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            byte[] buffer = new byte[buf.readInt()];
            buf.readBytes(buffer);
            ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
            tag = CompressedStreamTools.readCompressed(stream);
        }
        catch (IOException ex) {
            ThaumicAugmentation.getLogger().warn("Unable to deserialize %s: %s", getClass().getSimpleName(), ex.getMessage());
        }
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(tag, stream);
            byte[] data = stream.toByteArray();
            buf.writeInt(data.length);
            buf.writeBytes(data);
        }
        catch (IOException ex) {
            ThaumicAugmentation.getLogger().warn("Unable to serialize %s: %s", getClass().getSimpleName(), ex.getMessage());
        }
    }
    
    public NBTTagCompound getTag() {
        return tag;
    }
	
}
