package carpetclient.gui.chunkgrid;

import carpetclient.coders.zerox53ee71ebe11e.Chunkdata;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Point;

/**
 * Chunk Grid draw method for drawing debug chunk grids.
 */
public class ChunkGrid {

    private int screenWidth = 0;
    private int screenHeight = 0;
    private int columnCount = 100;
    private int rowCount = 100;
    private int scale = 10;
    private int scrollIndex = 8;
    private int[] scrollValues = {1, 2, 3, 4, 6, 8, 10, 15, 20, 30, 40, 60, 100, 200};

    private Point selection = new Point(Integer.MAX_VALUE, 0);
    private Point playerLocation = new Point(Integer.MAX_VALUE, 0);


    /**
     * Main draw method for the squares representing chunks.
     *
     * @param view   The data that the chunks ares stored in.
     * @param thisX  Draw area X position
     * @param thisY  Draw area Y position
     * @param width  Draw area width
     * @param height Draw area height
     */
    public void draw(Chunkdata.MapView view, int thisX, int thisY, int width, int height) {
        screenWidth = width;
        screenHeight = height;
        rowCount = (int) Math.ceil((float) height / scale);
        columnCount = (int) Math.ceil((float) width / scale);

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.disableTexture();

        if (GuiChunkGrid.style.isGradient())
            GlStateManager.shadeModel(GL11.GL_SMOOTH);

        int originx = view.getLowerX();
        int origonz = view.getLowerZ();

        if (view != null) {
            buf.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (Chunkdata.ChunkView chunkdata : view) {
                int x = chunkdata.getX() - originx;
                int z = chunkdata.getZ() - origonz;
                int rx = x * scale;
                int ry = z * scale;
                int cellX = thisX + rx;
                int cellY = thisY + ry;

                int colors[] = chunkdata.getColors();
                int color = getColorFromArray(colors, colors.length - 1);
                drawBox(buf, cellX, cellY, x, z, color, scale);
                if (colors.length > 2) {
                    color = getColorFromArray(colors, colors.length - 2);
                    drawBox(buf, cellX + scale / 4, cellY + scale / 4, x, z, color, scale / 2);
                }
            }
            tess.end();
        }

        if (selection.getX() != Integer.MAX_VALUE) {
            drawSelectionBox(tess, buf, thisX, thisY, 0xfff7f006);
        }
        if (playerLocation.getX() != Integer.MAX_VALUE) {
            drawPlayerCross(tess, buf, thisX, thisY, 0xfff7f006);
        }

        GlStateManager.enableTexture();
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    private int getColorFromArray(int[] colors, int i) {
        int color = colors[i];
        if (color == Integer.MAX_VALUE)
            color = GuiChunkGrid.style.getBackgroundColor(); // retarded hackfix to fix background color being changable
        return color;
    }

    /**
     * Draws a box representing a chunk by color and location on screen
     *
     * @param buf
     * @param cellX
     * @param cellY
     * @param x
     * @param z
     * @param color
     * @param scal
     */
    private void drawBox(BufferBuilder buf, int cellX, int cellY, int x, int z, int color, int scal) {
        int alpha = (color & 0xff000000) >>> 24;
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0xff00) >> 8;
        int blue = (color & 0xff);
        float brightenFactor = GuiChunkGrid.style.isCheckerboard() ? 0.14f : 0.3f;
        if (red == 0 && green == 0 && blue == 0)
            brightenFactor = 0.2f;

        if (GuiChunkGrid.style.isCheckerboard() && (x + z) % 2 == 0) {
            color = brighten(color, brightenFactor);
        }

        int color1, color2;
        if (GuiChunkGrid.style.isGradient()) {
            color1 = brighten(color, brightenFactor);
            color2 = brighten(color1, brightenFactor);
        } else {
            color2 = color1 = color;
        }

        int alpha1 = (color1 & 0xff000000) >>> 24;
        int red1 = (color1 & 0xff0000) >> 16;
        int green1 = (color1 & 0xff00) >> 8;
        int blue1 = (color1 & 0xff);
        int alpha2 = (color2 & 0xff000000) >>> 24;
        int red2 = (color2 & 0xff0000) >> 16;
        int green2 = (color2 & 0xff00) >> 8;
        int blue2 = (color2 & 0xff);

        buf.vertex(cellX, cellY, 0).color(red, green, blue, alpha).nextVertex();
        buf.vertex(cellX, cellY + scal, 0).color(red1, green1, blue1, alpha1).nextVertex();
        buf.vertex(cellX + scal, cellY + scal, 0).color(red2, green2, blue2, alpha2).nextVertex();
        buf.vertex(cellX + scal, cellY, 0).color(red1, green1, blue1, alpha1).nextVertex();
    }

    /**
     * Draws a selection box for the selected chunk.
     *
     * @param tess
     * @param buf
     * @param thisX
     * @param thisY
     * @param color
     */
    private void drawSelectionBox(Tesselator tess, BufferBuilder buf, int thisX, int thisY, int color) {
        int alpha = (color & 0xff000000) >>> 24;
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0xff00) >> 8;
        int blue = (color & 0xff);

        int x = selection.getX();
        int z = selection.getY();
        int rx = x * scale;
        int ry = z * scale;
        int cellX = thisX + rx;
        int cellY = thisY + ry;

        if (cellX < thisX || cellY < thisY || cellX + scale > thisX + screenWidth || cellY + scale > thisY + screenHeight) {
            return;
        }

        buf.begin(GL11.GL_LINES, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(cellX, cellY, 0).color(red, green, blue, alpha).nextVertex();
        buf.vertex(cellX, cellY + scale, 0).color(red, green, blue, alpha).nextVertex();
        buf.vertex(cellX, cellY + scale, 0).color(red, green, blue, alpha).nextVertex();
        buf.vertex(cellX + scale, cellY + scale, 0).color(red, green, blue, alpha).nextVertex();
        buf.vertex(cellX + scale, cellY + scale, 0).color(red, green, blue, alpha).nextVertex();
        buf.vertex(cellX + scale, cellY, 0).color(red, green, blue, alpha).nextVertex();
        buf.vertex(cellX + scale, cellY, 0).color(red, green, blue, alpha).nextVertex();
        buf.vertex(cellX, cellY, 0).color(red, green, blue, alpha).nextVertex();
        tess.end();
    }

    private void drawPlayerCross(Tesselator tess, BufferBuilder buf, int thisX, int thisY, int color) {
        int alpha = (color & 0xff000000) >>> 24;
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0xff00) >> 8;
        int blue = (color & 0xff);

        int x = playerLocation.getX();
        int z = playerLocation.getY();
        int rx = x * scale;
        int ry = z * scale;
        int cellX = thisX + rx;
        int cellY = thisY + ry;

        if (cellX < thisX || cellY < thisY || cellX + scale > thisX + screenWidth || cellY + scale > thisY + screenHeight) {
            return;
        }

        buf.begin(GL11.GL_LINES, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(cellX, cellY, 0).color(red, green, blue, alpha).nextVertex();
        buf.vertex(cellX + scale, cellY + scale, 0).color(red, green, blue, alpha).nextVertex();
        buf.vertex(cellX, cellY + scale, 0).color(red, green, blue, alpha).nextVertex();
        buf.vertex(cellX + scale, cellY, 0).color(red, green, blue, alpha).nextVertex();
        tess.end();
    }

    /**
     * Screen pixel to grid x value.
     *
     * @param pixelX X pixel value.
     * @return the grid Y value.
     */
    public int getGridX(int pixelX) {
        if (scale == 0)
            return 0;
        return pixelX / scale;
    }

    /**
     * Screen pixel to grid y value.
     *
     * @param pixelY Y pixel value.
     * @return the grid Y value.
     */
    public int getGridY(int pixelY) {
        if (scale == 0)
            return 0;
        return pixelY / scale;
    }

    /**
     * Brightens any color for drawing on screen.
     *
     * @param col    Original color that needs brightened.
     * @param factor brighten factor.
     * @return the brightened color in integer number.
     */
    private static int brighten(int col, float factor) {
        int alpha = (col & 0xff000000) >>> 24;
        int red = (col & 0xff0000) >> 16;
        int green = (col & 0xff00) >> 8;
        int blue = col & 0xff;

        int mix = (int) ((red + green + blue) * factor / 3);
        red += mix;
        green += mix;
        blue += mix;
        int redOverflow = Integer.max(red - 255, 0);
        int greenOverflow = Integer.max(green - 255, 0);
        int blueOverflow = Integer.max(blue - 255, 0);
        red = Integer.min(red - redOverflow*3, 255);
        green = Integer.min(green - greenOverflow*3, 255);
        blue = Integer.min(blue - blueOverflow*3, 255);
        return (alpha << 24)
                | (red << 16)
                | (green << 8)
                | (blue);
    }

    /**
     * Size of the columns currently drawing on screens X value
     *
     * @return column numbers representing X chunk value.
     */
    public int sizeX() {
        return columnCount;
    }

    /**
     * Size of the row currently drawing on screen Y value
     *
     * @return row numbers representing Z chunk value.
     */
    public int sizeZ() {
        return rowCount;
    }

    /**
     * Sets the scale or zoom level of the drawing squares.
     *
     * @param width  width of the window in pixels.
     * @param height height of the window in pixels.
     * @param value  scrolling value added to old scroll value.
     */
    public void setScale(int width, int height, int value) {
        scale += value;
        scrollIndex += value;
        if (scrollIndex < 2 && !Screen.isControlDown()) {
            scrollIndex = 2;
        } else if (scrollIndex < 0) {
            scrollIndex = 0;
        } else if (scrollIndex >= scrollValues.length) {
            scrollIndex = scrollValues.length - 1;
        }
        scale = scrollValues[scrollIndex];
        rowCount = height / scale;
        columnCount = width / scale;
    }

    /**
     * Sets the selection box to draw lines around the selected rectangle.
     *
     * @param x X value for drawing on screen in square values.
     * @param y Y value for drawing on screen in square values.
     */
    public void setSelectionBox(int x, int y) {
        selection.setLocation(x, y);
    }

    /**
     * Set player location on the map.
     *
     * @param x X value for drawing on screen in square values.
     * @param y Y value for drawing on screen in square values.
     */
    public void playerChunk(int x, int y) {
        playerLocation.setLocation(x, y);
    }

    /**
     * Screen height in pixels.
     *
     * @return returns height pixel of this window.
     */
    public int height() {
        return screenHeight;
    }

    /**
     * Screen width in pixels.
     *
     * @return returns width pixel of this window.
     */
    public int width() {
        return screenWidth;
    }

    /**
     * Size conversion from window size to scaled value.
     *
     * @param window window size that is to be scaled.
     * @return the scaled value after the scaling is applied to it.
     */
    public int size(int window) {
        return (int) Math.ceil((float) window / scale);
    }
}
