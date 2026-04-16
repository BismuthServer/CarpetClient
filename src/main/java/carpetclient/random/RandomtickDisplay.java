package carpetclient.random;

import carpetclient.Config;
import carpetclient.pluginchannel.CarpetPluginChannel;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.ornithemc.osl.networking.api.PacketBuffer;

import java.io.IOException;
import java.util.ArrayList;

public class RandomtickDisplay {
    private static final String gold = "\u00a76", red = "\u00a74", green = "\u00a72", pushe = "\u00a76Pushes", pull = "\u00a76Pull";
    private static ArrayList<BlockPos> chunks = new ArrayList<>();

    public static void processPacket(PacketBuffer data) {
        NbtCompound nbt;
        try {
            nbt = data.readNbtCompound();
        } catch (EncoderException e) {
            e.printStackTrace();
            return;
        }

        if (nbt != null && Minecraft.getInstance().world != null) {
            addDisplay(nbt);
        }
    }

    private static void addDisplay(NbtCompound nbt) {
        if (!Config.randomtickChunkUpdates) return;

        NbtList nbttaglist = nbt.getList("list", 10);

        chunks.clear();
        for (int i = 0; i < nbttaglist.size(); i++) {
            NbtCompound chunkData = nbttaglist.getCompound(i);
            int x = chunkData.getInt("x");
            int z = chunkData.getInt("z");
            chunks.add(new BlockPos(x * 16 + 8, 0, z * 16 + 8));
        }
    }

    public static void draw(float partialTicks) {
        if (!Config.randomtickingChunksVisualizer.getValue()) return;

        final LocalClientPlayerEntity player = Minecraft.getInstance().player;
        final double d0 = player.prevX + (player.x - player.prevX) * partialTicks;
        final double d1 = player.prevY + (player.y - player.prevY) * partialTicks;
        final double d2 = player.prevZ + (player.z - player.prevZ) * partialTicks;
        final EntityRenderDispatcher rm = Minecraft.getInstance().getEntityRenderDispatcher();

        int counter = 0;
        for (BlockPos pos : chunks) {
            GameRenderer.renderNameTag(Minecraft.getInstance().textRenderer, gold + counter, (float) (pos.getX() + 0.5f - d0), (float) (player.y + 0.2f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.cameraYaw, rm.cameraPitch, rm.options.perspective == 2, false);
            counter++;
        }
    }

    public static void startStopRecording(boolean start) {
        PacketBuffer sender = new PacketBuffer(Unpooled.buffer());
        sender.writeInt(CarpetPluginChannel.RANDOMTICK_DISPLAY);
        sender.writeBoolean(start);
        CarpetPluginChannel.packatSender(sender);
    }

    public static void reset() {
        Config.randomtickingChunksVisualizer.setValue(false);
    }
}
