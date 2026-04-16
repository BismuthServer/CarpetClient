package carpetclient.gui;

import carpetclient.coders.Pokechu22.GuiConfigList;
import carpetclient.gui.config.ClientRootList;
import java.io.IOException;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class ConfigGUI extends Screen {
    private static int slotHeight = 24;
    private static String carpetServerVersion;
    public static void setServerVersion(String version) { carpetServerVersion = version;}

    private final Screen parent;
    private GuiConfigList list = null;

    public ConfigGUI(Screen parent) {
        this.parent = parent;
    }

    public void showList(GuiConfigList list) {
        this.list = list;

        this.list.setBounds(this.width, this.height, 39, this.height - 32);
        this.list.initGui();
    }

    public void init() {
        this.addButton(new ButtonWidget(100, this.width / 2 - 100, this.height - 29, "Back"));

        showList(new ClientRootList(this, minecraft, 24));
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 100) {
            list.onClose();
            if (list instanceof ClientRootList) {
                this.minecraft.openScreen(this.parent);
            } else {
                this.showList(new ClientRootList(this, minecraft, slotHeight));
            }
        }
    }

    // ===== RENDERING ===== //
    //region rendering
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        list.render(mouseX, mouseY, partialTicks);
        this.drawTooltip(mouseX, mouseY, partialTicks);

        final int startY = 8;
        this.drawCenteredString(this.textRenderer, "Carpet Client", width / 2, startY, 0xFFFFFF);
        this.drawCenteredString(this.textRenderer, String.format("Carpet server version: %s", carpetServerVersion), width / 2, startY + this.textRenderer.fontHeight, 0xFFFFFF);

        super.render(mouseX, mouseY, partialTicks);
    }

    public void drawTooltip(int mouseX, int mouseY, float partialTicks) {
        list.drawTooltip(mouseX, mouseY, partialTicks);
    }
    //endregion

    // ===== EVENTS ===== //
    //region events
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        list.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        list.mouseReleased(mouseX, mouseY, state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyPressed(char typedChar, int keyCode) {
        list.keyDown(typedChar, keyCode);
        super.keyPressed(typedChar, keyCode);
    }

    @Override
    public void handleMouse() {
        super.handleMouse();
        list.handleMouse();
    }
    //endregion
}
