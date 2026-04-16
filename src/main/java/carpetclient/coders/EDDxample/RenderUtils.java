package carpetclient.coders.EDDxample;

import java.awt.Color;
import net.minecraft.client.render.platform.GLX;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
/*
Code from EDDxample used to render markers ingame.
 */
public class RenderUtils {
    public static void prepareOpenGL(boolean b) {
        if (b) {
            GlStateManager.disableFog();
            GlStateManager.disableLighting();
            GlStateManager.disableTexture();
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            setLightmapDisabled(true);
        } else {
            setLightmapDisabled(false);
            GlStateManager.lineWidth(1.0F);
            GlStateManager.enableLighting();
            GlStateManager.enableTexture();
            GlStateManager.enableDepthTest();
            GlStateManager.depthMask(true);
            GlStateManager.enableFog();
        }
    }

    public static void setLightmapDisabled(boolean disabled) {
        GlStateManager.activeTexture(GLX.GL_TEXTURE1);

        if (disabled) {
            GlStateManager.disableTexture();
        } else {
            GlStateManager.enableTexture();
        }

        GlStateManager.activeTexture(GLX.GL_TEXTURE0);
    }

    private static void drawDot(double Ax, double Ay, double Az, Color color) {
        final int RED = color.getRed(), GREEN = color.getGreen(), BLUE = color.getBlue();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder vertexbuffer = tesselator.getBuffer();
        vertexbuffer.begin(0, DefaultVertexFormat.POSITION_COLOR);
        vertexbuffer.vertex(Ax, Ay, Az).color(RED, GREEN, BLUE, 255).nextVertex();
        tesselator.end();
    }

    public static void drawline(double d0, double d1, double d2, double Ax, double Ay, double Az, double Bx, double By, double Bz, Color color) {
        final int RED = color.getRed(), GREEN = color.getGreen(), BLUE = color.getBlue();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder vertexbuffer = tesselator.getBuffer();
        vertexbuffer.begin(3, DefaultVertexFormat.POSITION_COLOR);
        vertexbuffer.vertex(Ax - d0, Ay - d1, Az - d2).color(RED, GREEN, BLUE, 255).nextVertex();
        vertexbuffer.vertex(Bx - d0, By - d1, Bz - d2).color(RED, GREEN, BLUE, 255).nextVertex();
        tesselator.end();
    }

    public static void drawSphere(double radius, int sphereDensity, double d0, double d1, double d2, double Ax, double Ay, double Az, Color color, int mode) {
        if (mode == 2) sphereDensity /= 2;
        final double dPhi = 2 * Math.PI / sphereDensity;

        float prevX1 = 0, prevY1 = 0, prevZ1 = 0;

        float dy = 0.02f;

        for (double phi = 0.0; phi < 2 * Math.PI; phi += dPhi) {
            double sinPhi = Math.sin(phi);
            double dTheta = Math.PI / (1 + (int) (sphereDensity * Math.abs(sinPhi / 2)));

            float prevX = 0, prevY = 0, prevZ = 0;

            if (mode == 3)// Flat Circle
            {
                float dx = (float) (radius * Math.cos(phi));
                float dz = (float) (radius * Math.sin(phi));

                if (phi != 0)
                    drawline(d0, d1, d2, Ax + prevX1, Ay + prevY1, Az + prevZ1, Ax + dx, Ay + dy, Az + dz, color);
                prevX1 = dx;
                prevY1 = dy;
                prevZ1 = dz;
                continue;
            }

            for (double theta = 0.0; theta < Math.PI + dTheta; theta += dTheta) {
                float dx = (float) (radius * sinPhi * Math.cos(theta));
                float dz = (float) (radius * sinPhi * Math.sin(theta));
                dy = (float) (radius * Math.cos(phi));


                //MODE 1 = DOTS, MODE 2 = LINES
                if (mode == 1) drawDot(Ax + dx - d0, Ay + dy - d1, Az + dz - d2, color);
                else if (mode == 2 && theta != 0)
                    drawline(d0, d1, d2, Ax + prevX, Ay + prevY, Az + prevZ, Ax + dx, Ay + dy, Az + dz, color);

                prevX = dx;
                prevY = dy;
                prevZ = dz;
            }
        }
    }

    public static void drawBox(double dx, double dy, double dz, BlockPos p, Color color) {
        drawBox(dx, dy, dz, p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1, color);
    }

    public static void drawBox(double d0, double d1, double d2, double x, double y, double z, double x1, double y1, double z1, Color color) {
        int C = color.getRed(), M = color.getGreen(), Y = color.getBlue();
        WorldRenderer.renderOutlineShape(x - d0, y - d1, z - d2, x1 - d0, y1 - d1, z1 - d2, C / 255, M / 255, Y / 255, 1F);
    }

    public static void drawGhostBox(double dx, double dy, double dz, BlockPos p, Color color) {
        drawGhostBox(dx, dy, dz, p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1, color);
    }

    public static void drawGhostBox(double dx, double dy, double dz, double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        int C = color.getRed(), M = color.getGreen(), Y = color.getBlue();
        Box aabb = new Box(x1, y1, z1, x2, y2, z2).moved(-dx, -dy, -dz).expand(0.002);
        WorldRenderer.renderShape(aabb, C / 255, M / 255, Y / 255, 0.2F);
    }
}
