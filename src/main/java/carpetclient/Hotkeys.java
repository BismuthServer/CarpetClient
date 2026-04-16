package carpetclient;

import carpetclient.gui.chunkgrid.GuiChunkGrid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.text.LiteralText;
import net.ornithemc.osl.keybinds.api.KeyBindingEvents;
import net.ornithemc.osl.networking.api.PacketBuffer;
import net.ornithemc.osl.networking.api.PacketBuffers;

import org.lwjgl.input.Keyboard;

import carpetclient.pluginchannel.CarpetPluginChannel;

/*
Hotkey class to implement hotkeys for the carpet client. Changeable in the hotkey menu ingame.
 */
public class Hotkeys {

    //    private static KeyBinding toggleMainMenu = new KeyBinding("Carpet Menu", Keyboard.KEY_M, "Carpet Client");
    public static KeyBinding toggleSnapAim = new KeyBinding("Snap Aim", Keyboard.KEY_F9, "Carpet Client");
    public static KeyBinding toggleSnapAimKeyLocker = new KeyBinding("Snap Aim Keylocker", Keyboard.KEY_LMENU, "Carpet Client");
    private static KeyBinding toggleBoundingBoxMarkers = new KeyBinding("Bounding Box Markers", Keyboard.KEY_F8, "Carpet Client");
    private static KeyBinding toggleVillageMarkers = new KeyBinding("Village Markers", Keyboard.KEY_F7, "Carpet Client");
    //    public static KeyBinding toggleRBP = new KeyBinding("Relaxed Block Placement", Keyboard.KEY_P, "Carpet Client");
    public static KeyBinding toggleBlockFlip = new KeyBinding("Block Rotation Flip", Keyboard.KEY_LMENU, "Carpet Client");
    public static KeyBinding toggleBlockFacing = new KeyBinding("Block Rotation Face", Keyboard.KEY_LCONTROL, "Carpet Client");
    public static KeyBinding chunkDebug = new KeyBinding("Chunk debug", Keyboard.KEY_F6, "Carpet Client");
    public static KeyBinding randomtickChunkUpdates = new KeyBinding("Randomtick display updates", Keyboard.KEY_U, "Carpet Client");

    public static void init() {
        KeyBindingEvents.REGISTER_KEYBINDS.register(registry -> {
//            registry.register(toggleMainMenu)
            registry.register(toggleSnapAim);
            registry.register(toggleSnapAimKeyLocker);
            registry.register(toggleBoundingBoxMarkers);
            registry.register(toggleVillageMarkers);
//            registry.register(toggleRBP);
            registry.register(toggleBlockFlip);
            registry.register(toggleBlockFacing);
            registry.register(chunkDebug);
            registry.register(randomtickChunkUpdates);
        });
    }

    public static void onTick(Minecraft minecraft) {
//        if (toggleRBP.isPressed()) {
//            Config.relaxedBlockPlacement = !Config.relaxedBlockPlacement;
//            minecraft.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("Relaxed block placement: " + (Config.relaxedBlockPlacement ? "ON" : "OFF")));
//        } else
        if (toggleSnapAim.consumeClick()) {
            Config.snapAim = !Config.snapAim;
            Util.printToChat("SnapAim: " + (Config.snapAim ? "ON" : "OFF") );
        } else if (toggleBoundingBoxMarkers.consumeClick()) {
            Config.boundingBoxMarkers = !Config.boundingBoxMarkers;
            if (Config.boundingBoxMarkers) {
                PacketBuffer sender = PacketBuffers.make();
                sender.writeInt(CarpetPluginChannel.BOUNDINGBOX_MARKERS);

                CarpetPluginChannel.packatSender(sender);
            }
            Util.printToChat("Bounding Box Markers: " + (Config.boundingBoxMarkers ? "ON" : "OFF") );
        } else if (toggleVillageMarkers.consumeClick()) {
            Config.villageMarkers = !Config.villageMarkers;
            PacketBuffer sender = PacketBuffers.make();
            sender.writeInt(CarpetPluginChannel.VILLAGE_MARKERS);
            sender.writeBoolean(Config.villageMarkers);

            CarpetPluginChannel.packatSender(sender);
            Util.printToChat("Village Markers: " + (Config.villageMarkers ? "ON" : "OFF") );
        } else if (chunkDebug.consumeClick()) {
            minecraft.openScreen(GuiChunkGrid.instance);
        } else if (randomtickChunkUpdates.consumeClick()) {
            Config.randomtickChunkUpdates = !Config.randomtickChunkUpdates;
            Util.printToChat("Randomtick display updates: " + (Config.randomtickChunkUpdates ? "ON" : "OFF") );
        }
    }

    public static boolean isKeyDown(int code){
        try{
            return Keyboard.isKeyDown(code);
        }catch(Exception e){
            Minecraft.getInstance().gui.getChat().addMessage(new LiteralText("Something went wrong with the hotkey. Reset it in the menu to get it working again."));
        }

        return false;
    }
}
