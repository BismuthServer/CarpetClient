package carpetclient.mixins;

import net.minecraft.client.render.Culler;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import carpetclient.CarpetClient;

/*
Mixing Override to disable light updates on the client.
Big thanks from nessie to help add this fix.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "notifyLightChanged", at = @At("HEAD"), cancellable = true)
    public void notifyLightSet(BlockPos pos, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderEntities", at = @At("TAIL"))
    private void postRenderEntities(Entity camera, Culler culler, float partialTicks, CallbackInfo ci) {
        CarpetClient.onPostRenderEntities(partialTicks);
    }
}
