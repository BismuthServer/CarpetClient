package carpetclient.gui.entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.jetbrains.annotations.NotNull;


public class ButtonEntry extends StandardRowEntry<ButtonEntry> {
    private ButtonWidget button;

    private String btnText;

    public ButtonEntry(String title, String btnText, boolean reset) {
        super(title, false, reset, null);
        initEntry(btnText);
    }

    public ButtonEntry(String title, String btnText, boolean reset, @NotNull String infoStr) {
        super(title, true, reset, infoStr);
        initEntry(btnText);
    }

    private void initEntry(String btnText) {
        this.btnText = btnText;

        this.button = new ButtonWidget(0, 0, 0, 100, 20, btnText);
    }

    public ButtonWidget getButton() { return button; }

    @Override
    protected void draw(int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, float partialTicks) {
        this.button.x = x + listWidth / 2;
        this.button.y = y;
        this.button.render(Minecraft.getInstance(), mouseX, mouseY, partialTicks);
    }

    public void setDisplayString(String value)
    {
        this.button.message = value;
    }

    public String getDisplayString() {
        return btnText;
    }

    protected boolean isResetEnabled() { return true; }

    @Override
    protected boolean mouseDown(int x, int y, int button) {
        if (this.button.mouseClicked(Minecraft.getInstance(), x, y)) {
            this.button.playClickSound(Minecraft.getInstance().getSoundManager());
            this.performAction();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void mouseUp(int x, int y, int button) {
        this.button.mouseReleased(x, y);
    }
}
