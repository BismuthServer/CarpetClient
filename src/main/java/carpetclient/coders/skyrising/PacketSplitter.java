/**
 * This is code provided by skyrising for the use of sending large packet sizes then Minecraft usually allows.
 * Ask skyrising for permition before using this code.
 */

package carpetclient.coders.skyrising;

import com.google.common.base.Charsets;
import io.netty.buffer.Unpooled;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.PacketByteBuf;
import net.ornithemc.osl.networking.api.client.ClientPlayNetworking;

import static carpetclient.pluginchannel.CarpetPluginChannel.CARPET_PLUGIN_CHANNEL;

public class PacketSplitter {
    public static final int MAX_TOTAL_PER_PACKET = 32767;
    public static final int MAX_PAYLOAD_PER_PACKET = MAX_TOTAL_PER_PACKET - 5;
    public static final int DEFAULT_MAX_RECEIVE_SIZE = Integer.MAX_VALUE;

    private static final Map<String, ReadingSession> readingSessions = new HashMap<>();

    public static boolean send(String channel, PacketByteBuf packet, boolean always) {
        int len = packet.writerIndex();
        packet.resetReaderIndex();
        for (int offset = 0; offset < len; offset += MAX_PAYLOAD_PER_PACKET) {
            int thisLen = Math.min(len - offset, MAX_PAYLOAD_PER_PACKET);
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer(thisLen));
            buf.resetWriterIndex();
            if (offset == 0) buf.writeVarInt(len);
            buf.writeBytes(packet, thisLen);
            if (always) {
                ClientPlayNetworking.doSend(channel, buf);
            } else {
                ClientPlayNetworking.send(channel, buf);
            }
        }
        packet.release();
        return true;
    }

    public static PacketByteBuf receive(String channel, PacketByteBuf data) {
        return receive(channel, data, DEFAULT_MAX_RECEIVE_SIZE);
    }

    public static PacketByteBuf receive(String channel, PacketByteBuf data, int maxLength) {
        return readingSessions.computeIfAbsent(channel, ReadingSession::new).receive(data, maxLength);
    }

    private static class ReadingSession {
        private final String key;
        private int expectedSize = -1;
        private PacketByteBuf received;

        private ReadingSession(String key) {
            this.key = key;
        }

        private PacketByteBuf receive(PacketByteBuf data, int maxLength) {
            if (expectedSize < 0) {
                expectedSize = data.readVarInt();
                if (expectedSize > maxLength) throw new IllegalArgumentException("Payload too large");
                received = new PacketByteBuf(Unpooled.buffer(expectedSize));
            }
            received.writeBytes(data.readBytes(data.readableBytes()));
            if (received.writerIndex() >= expectedSize) {
                readingSessions.remove(key);
                return received;
            }
            return null;
        }
    }
	public static PacketByteBuf getRegistrationData()
	{
		PacketByteBuf buff = new PacketByteBuf(Unpooled.buffer());
		String channelString = String.join("\u0000", CARPET_PLUGIN_CHANNEL);

		byte[] bytes = channelString.getBytes(Charsets.UTF_8);

		buff.writeBytes(bytes);
		return buff;
	}
}
