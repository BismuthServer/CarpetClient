package carpetclient.gui.entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;

public class SimpleButtonEntry extends BaseEntry<SimpleButtonEntry> {
    private ButtonWidget button;

    public SimpleButtonEntry(String btnText) {
        super(null);
        this.button = new ButtonWidget(0, 0, 0, 150, 20, btnText);
    }

    public ButtonWidget getButton() { return button; }

    @Override
    protected void draw(int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, float partialTicks) {
        this.button.x = x + listWidth / 2 - button.getWidth() / 2;
        this.button.y = y;
        this.button.render(Minecraft.getInstance(), mouseX, mouseY, partialTicks);
    }

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
