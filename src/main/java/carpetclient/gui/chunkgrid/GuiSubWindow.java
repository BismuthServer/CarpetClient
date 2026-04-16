package carpetclient.gui.chunkgrid;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import java.io.IOException;
import java.util.List;

/**
 * Window for displaying events selected by the user.
 */
public abstract class GuiSubWindow extends Screen {

    protected String title;
    protected Screen parentScreen;
    protected Screen backgroundScreen;
    private List<String> stackTrace;

    public GuiSubWindow(String title, Screen parentScreen, Screen backgroundScreen, List<String> stackTrace) {
        this.title = title;
        this.parentScreen = parentScreen;
        this.backgroundScreen = backgroundScreen;
        this.stackTrace = stackTrace;
    }

    @Override
    public void init(Minecraft mc, int width, int height) {
        super.init(mc, width, height);
        backgroundScreen.init(mc, width, height);
    }

    protected void layoutButtons(ButtonWidget... buttons) {
        int buttonX = getSubWindowLeft() + 5;
        int buttonY = getSubWindowBottom() - (20 + getFooterHeight()) / 2;
        int buttonWidth = (getSubWindowRight() - getSubWindowLeft() - 5 * (buttons.length + 1)) / buttons.length;

        for (ButtonWidget b : buttons) {
            b.x = buttonX;
            b.y = buttonY;
            b.setWidth(buttonWidth);
            buttonX += 5 + buttonWidth;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        backgroundScreen.render(-1, -1, partialTicks); // Spoof mouse coords so buttons don't highlight
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);

        GuiElement.fill(getSubWindowLeft(), getSubWindowTop(), getSubWindowRight(), getSubWindowBottom(), 0xc0b0b0ff);

        GlStateManager.disableTexture();
        GlStateManager.lineWidth(2);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(getSubWindowRight(), getSubWindowBottom() - getFooterHeight(), 0).color(255, 255, 255, 255).nextVertex();
        buf.vertex(getSubWindowLeft(), getSubWindowBottom() - getFooterHeight(), 0).color(255, 255, 255, 255).nextVertex();
        buf.vertex(getSubWindowLeft(), getSubWindowTop(), 0).color(255, 255, 255, 255).nextVertex();
        buf.vertex(getSubWindowRight(), getSubWindowTop(), 0).color(255, 255, 255, 255).nextVertex();
        buf.vertex(getSubWindowRight(), getSubWindowBottom(), 0).color(255, 255, 255, 255).nextVertex();
        buf.vertex(getSubWindowLeft(), getSubWindowBottom(), 0).color(255, 255, 255, 255).nextVertex();
        buf.vertex(getSubWindowLeft(), getSubWindowBottom() - getFooterHeight(), 0).color(255, 255, 255, 255).nextVertex();
        tess.end();
        GlStateManager.lineWidth(1);
        GlStateManager.enableTexture();

        textRenderer.draw(title, (getSubWindowLeft() + getSubWindowRight()) / 2 - textRenderer.getWidth(title) / 2, getSubWindowTop() + 6, 0);

        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (lastClickedButton == null) {
            if (mouseButton == 0) {
                minecraft.openScreen(parentScreen);
                if (parentScreen instanceof GuiChunkGrid) {
                    ((GuiChunkGrid) parentScreen).consumeLeftClick();
                }
            } else if (mouseButton == 1) {
                if (stackTrace != null && !(this instanceof GuiShowStackTrace)) {
                    minecraft.openScreen(new GuiShowStackTrace(this, backgroundScreen, stackTrace));
                }
            }
        }
    }

    @Override
    protected void keyPressed(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE)
            minecraft.openScreen(parentScreen);
        else
            super.keyPressed(typedChar, keyCode);
    }

    @Override
    public boolean shouldPauseGame() {
        return backgroundScreen.shouldPauseGame();
    }

    protected int getSubWindowLeft() {
        return (int) (width * 0.1);
    }

    protected int getSubWindowRight() {
        return (int) (width * 0.9);
    }

    protected int getSubWindowTop() {
        return (int) (height * 0.1);
    }

    protected int getSubWindowBottom() {
        return (int) (height * 0.9);
    }

    protected int getFooterHeight() {
        return 30;
    }
}
