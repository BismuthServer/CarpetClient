package carpetclient.mixins.optifine;

import carpetclient.mixinInterface.AMixinEntityRenderer;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    /**
     * fix tick rate rendering glitch rendering view bobbing, OptiFine additional patch
     *
     * method prototype is:
     * public void renderHand(float partialTicks, int pass, boolean renderItem, boolean renderOverlay, boolean renderTranslucent)
     *
     * Method name not obfuscated, so cannot be patched by MixinEntityRenderer::tickratePlayerBobbing
     * The signature is not declared here in @ModifyArg method parameter to silence compilation warning.
     *
     * Also, for some reason, if `remap = true` is omitted in @At then refmap don't
     * generate with entry for this injection point, causing silent(!) injection failure.
     */
    @ModifyArg(method = "renderItemInHand", index = 0, remap = false, require = 0,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;loadCustom(F)V", remap = true))
    private float tickratePlayerBobbingOptiFine(float partialTicksWorld) {
        return ((AMixinEntityRenderer) this).partialTicksPlayer(partialTicksWorld);
    }
}
