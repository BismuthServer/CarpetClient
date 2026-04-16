package carpetclient.pluginchannel;


import carpetclient.coders.skyrising.PacketSplitter;
import carpetclient.coders.zerox53ee71ebe11e.Chunkdata;

import java.nio.charset.StandardCharsets;

import carpetclient.CarpetClient;
import carpetclient.bugfix.PistonFix;
import carpetclient.random.RandomtickDisplay;
import carpetclient.util.CustomCrafting;
import net.ornithemc.osl.core.api.util.NamespacedIdentifier;
import net.ornithemc.osl.networking.api.ChannelRegistry;
import net.ornithemc.osl.networking.api.PacketBuffer;
import net.ornithemc.osl.networking.api.StringChannelIdentifierParser;
import net.ornithemc.osl.networking.api.client.ClientConnectionEvents;
import net.ornithemc.osl.networking.api.client.ClientPlayNetworking;
import carpetclient.coders.EDDxample.ShowBoundingBoxes;
import carpetclient.coders.EDDxample.VillageMarker;
import carpetclient.rules.CarpetRules;
import carpetclient.rules.TickRate;

/*
Plugin channel class to implement a client server communication between carpet client and carpet server.
 */
public class CarpetPluginChannel {
    // liteloader channels carpet uses to check for carpet clients
    public static final NamespacedIdentifier REGISTER_CHANNEL = StringChannelIdentifierParser.fromString("REGISTER");
    public static final NamespacedIdentifier UNREGISTER_CHANNEL = StringChannelIdentifierParser.fromString("UNREGISTER");

    // carpet channels
    public static final NamespacedIdentifier CARPET_CLIENT_CHANNEL = StringChannelIdentifierParser.fromString("carpet:client");
    public static final NamespacedIdentifier CARPET_MINE_CHANNEL = StringChannelIdentifierParser.fromString("carpet:mine");

    public static final int GUI_ALL_DATA = 0;
    public static final int RULE_REQUEST = 1;
    public static final int VILLAGE_MARKERS = 2;
    public static final int BOUNDINGBOX_MARKERS = 3;
    public static final int TICKRATE_CHANGES = 4;
    public static final int CHUNK_LOGGER = 5;
    public static final int PISTON_UPDATES = 6;
    public static final int RANDOMTICK_DISPLAY = 7;
    public static final int CUSTOM_RECIPES = 8;

    public static void init() {
        ChannelRegistry.register(REGISTER_CHANNEL);
        ChannelRegistry.register(UNREGISTER_CHANNEL);

        ClientConnectionEvents.LOGIN.register(minecraft -> {
            ClientPlayNetworking.sendNoCheck(REGISTER_CHANNEL, buffer -> {
                String channels = String.join("\0000",
                    StringChannelIdentifierParser.toString(CARPET_CLIENT_CHANNEL)
                );

                buffer.writeBytes(channels.getBytes(StandardCharsets.UTF_8));
            });
        });
        ClientConnectionEvents.DISCONNECT.register(minecraft -> {
            ClientPlayNetworking.sendNoCheck(UNREGISTER_CHANNEL, buffer -> {
                String channels = String.join("\0000",
                        StringChannelIdentifierParser.toString(CARPET_CLIENT_CHANNEL)
                );

                buffer.writeBytes(channels.getBytes(StandardCharsets.UTF_8));
            });
        });

        ChannelRegistry.register(CARPET_CLIENT_CHANNEL);
        ChannelRegistry.register(CARPET_MINE_CHANNEL, false, true);

        ClientPlayNetworking.registerListener(CARPET_CLIENT_CHANNEL, (ctx, data) -> {
            ctx.ensureOnMainThread();

            data = PacketSplitter.receive(CARPET_CLIENT_CHANNEL, data);
            if (data != null) {
                handleData(data);
            }
        });
    }

    /**
     * Handler for the incoming pakets from the server.
     *
     * @param data Data that is recieved from the server.
     */
    private static void handleData(PacketBuffer data) {
        int type = data.readInt();

        if (GUI_ALL_DATA == type) {
            CarpetRules.setAllRules(data);
        }
        if (RULE_REQUEST == type) {
            CarpetRules.ruleData(data);
        }
        if (VILLAGE_MARKERS == type) {
            VillageMarker.villageUpdate(data);
        }
        if (BOUNDINGBOX_MARKERS == type) {
            ShowBoundingBoxes.getStructureComponent(data);
        }
        if (TICKRATE_CHANGES == type) {
            TickRate.setTickRate(data);
        }
        if (CHUNK_LOGGER == type) {
            Chunkdata.processPacket(data);
        }
        if (PISTON_UPDATES == type) {
            PistonFix.processPacket(data);
        }
        if (RANDOMTICK_DISPLAY == type) {
            RandomtickDisplay.processPacket(data);
        }
        if (CUSTOM_RECIPES == type) {
            CustomCrafting.addCustomRecipes(data);
        }
    }

    /**
     * Packet sending method to send data to the server.
     *
     * @param data The data that is being sent to the server.
     */
    public static void packatSender(PacketBuffer data) {
        PacketSplitter.send(CARPET_CLIENT_CHANNEL, data, false);
    }
}
