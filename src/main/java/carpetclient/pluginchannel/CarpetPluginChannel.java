package carpetclient.pluginchannel;


import carpetclient.coders.skyrising.PacketSplitter;
import carpetclient.coders.zerox53ee71ebe11e.Chunkdata;
import carpetclient.bugfix.PistonFix;
import carpetclient.random.RandomtickDisplay;
import carpetclient.util.CustomCrafting;
import com.google.common.collect.ImmutableList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.ornithemc.osl.networking.api.client.ClientConnectionEvents;
import net.ornithemc.osl.networking.api.client.ClientPlayNetworking;
import carpetclient.coders.EDDxample.ShowBoundingBoxes;
import carpetclient.coders.EDDxample.VillageMarker;
import carpetclient.rules.CarpetRules;
import carpetclient.rules.TickRate;
import net.ornithemc.osl.networking.impl.interfaces.mixin.IClientPlayNetworkHandler;

import java.util.Collections;
import java.util.Objects;

/*
Plugin channel class to implement a client server communication between carpet client and carpet server.
 */
public class CarpetPluginChannel {
    public static final String CARPET_CHANNEL_NAME = "carpet:client";
	public static final ImmutableList<String> CARPET_PLUGIN_CHANNEL = ImmutableList.of(CARPET_CHANNEL_NAME);
	protected static final String CHANNEL_REGISTER = "REGISTER";
	protected static final String CHANNEL_UNREGISTER = "UNREGISTER";

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
		ClientConnectionEvents.LOGIN.register(handler -> {
			((IClientPlayNetworkHandler) Objects.requireNonNull(handler.getNetworkHandler())).osl$networking$registerServerChannels(Collections.singleton(CARPET_CHANNEL_NAME));
			Objects.requireNonNull(handler.getNetworkHandler()).sendPacket(new CustomPayloadC2SPacket(CHANNEL_REGISTER, PacketSplitter.getRegistrationData()));
		});
		ClientConnectionEvents.DISCONNECT.register(handler -> Objects.requireNonNull(handler.getNetworkHandler()).sendPacket(new CustomPayloadC2SPacket(CHANNEL_UNREGISTER, PacketSplitter.getRegistrationData())));
		ClientPlayNetworking.registerListener(CARPET_CHANNEL_NAME, (minecraft, handler, data) -> {
			data = PacketSplitter.receive(CARPET_CHANNEL_NAME, data);
			if (data == null) return false;
			handleData(data);
			return true;
		});
    }

    /**
     * Handler for the incoming pakets from the server.
     *
     * @param data Data that is recieved from the server.
     */
    private static void handleData(PacketByteBuf data) {
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
    public static void packatSender(PacketByteBuf data) {
        PacketSplitter.send(CARPET_CHANNEL_NAME, data, false);
    }
}
