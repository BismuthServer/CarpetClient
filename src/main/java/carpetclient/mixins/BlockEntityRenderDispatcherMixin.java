package carpetclient.mixins;

import carpetclient.util.ITileEntityRenderDispatcher;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.platform.GLX;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin implements ITileEntityRenderDispatcher
{
    @Shadow public double cameraX;

    @Shadow public double cameraY;

    @Shadow public double cameraZ;

    @Shadow public World world;

    @Shadow public static double offsetX;
    @Shadow public static double offsetY;
    @Shadow public static double offsetZ;

    @Shadow public abstract void render(BlockEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage, float p_192854_10_);

    @Override
    public void carpetClient$renderTileEntityOffset(BlockEntity tileentityIn, float partialTicks, int destroyStage, double xOffset, double yOffset, double zOffset)
    {
        if (tileentityIn.squaredDistanceTo(this.cameraX - xOffset, this.cameraY - yOffset, this.cameraZ - zOffset) < tileentityIn.getSquaredViewDistance())
        {
            Lighting.turnOn();
            int i = this.world.getLightColor(tileentityIn.getPos(), 0);
            int j = i % 65536;
            int k = i / 65536;

            GLX.multiTexCoord2f(GLX.GL_TEXTURE1, (float)j, (float)k);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos blockpos = tileentityIn.getPos();
            this.render(tileentityIn, (double)blockpos.getX() - offsetX + xOffset, (double)blockpos.getY() - offsetY + yOffset, (double)blockpos.getZ() - offsetZ + zOffset, partialTicks, destroyStage, 1.0F);
        }
    }
}
