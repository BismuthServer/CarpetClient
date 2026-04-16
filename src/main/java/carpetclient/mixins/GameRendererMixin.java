package carpetclient.mixins;

import carpetclient.CarpetClient;
import carpetclient.Config;
import carpetclient.mixinInterface.AMixinEntityRenderer;
import carpetclient.mixinInterface.AMixinMinecraft;
import carpetclient.mixinInterface.AMixinTimer;
import carpetclient.rules.TickRate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TickTimer;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.vertex.Tesselator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements AMixinEntityRenderer {
    @Shadow
    private @Final Minecraft minecraft;

    @Shadow
    public void renderWorld(float partialTicks, long finishTimeNano) {}

    /**
     * fixes the world being culled while noclipping
     */
    @Redirect(method = "render(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/living/player/LocalClientPlayerEntity;isSpectator()Z"))
    private boolean fixSpectator(LocalClientPlayerEntity player) {
        return player.isSpectator() || (Config.creativeModeNoClip.getValue() && player.isCreative());
    }

    @Inject(method = "render(FJ)V", at = @At("TAIL"))
    private void onPostRender(float partialTicks, long startTime, CallbackInfo ci) {
        CarpetClient.onPostRender(partialTicks);
    }

    /**
     * Get player partial tick for rendering
     */
    @Override
    public float partialTicksPlayer(float partialTicksWorld) {
        if (this.minecraft.player.isRiding())
            return partialTicksWorld;

        if (this.minecraft.isPaused())
            return ((AMixinMinecraft) this.minecraft).carpetClient$getRenderPartialTicksPausedPlayer();

        TickTimer timer = ((IMinecraft) this.minecraft).getTimer();
        return ((AMixinTimer) timer).carpetClient$getRenderPartialTicksPlayer();
    }

    /**
     * fix tick rate rendering glitch rendering player
     */
    @Redirect(method = "render(FJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderWorld(FJ)V"))
    private void tickratePlayerPartial(GameRenderer thisarg, float partialTicksWorld, long finishTimeNano) {
        if (!TickRate.runTickRate || this.minecraft.player.isRiding()) {
            if(!((IBufferBuilder) Tesselator.getInstance().getBuffer()).getBuilding()) {
                this.renderWorld(partialTicksWorld, finishTimeNano);
            }
            return;
        }

        // Normally, entities are rendered at partial ticks in between last tick
        // pos and current pos. However, players are affected here at player tick
        // rate and the world's partial ticks does not reflect that of the player,
        // causing very wrong velocities and "rubberbanding" every world tick.
        //
        // Because mods often hook rendering to create overlays (for example
        // bounding boxes or schematics), demanding the computation of entity
        // render position to switch between world and player partial ticks is
        // impractical for every mod out there. We do instead by computing where
        // the player is supposed to be.
        //
        // If the world tick is faster than player, we and assign the render
        // position to both last tick and current pos. The velocity is not kept
        // because that would cause the current pos to occationally glitch into
        // a block.
        //
        // If slower, velocity is kept and and the following vector equations
        // are satisfied:
        //
        //   worldlastpos  + (worldcurpos  - worldlastpos ) * worldpartialticks
        // = playerlastpos + (playercurpos - playerlastpos) * playerpartialticks
        //
        //   (worldcurpos  - worldlastpos ) / worldtickrate
        // = (playercurpos - playerlastpos) / playertickrate
        //
        // where player{last,cur}pos is what is given in the entity data and
        // world{last,cur}pos is what we are computing.

        double savedLastX = this.minecraft.player.prevX;
        double savedLastY = this.minecraft.player.prevY;
        double savedLastZ = this.minecraft.player.prevZ;
        double savedCurX = this.minecraft.player.x;
        double savedCurY = this.minecraft.player.y;
        double savedCurZ = this.minecraft.player.z;

        TickTimer timer = ((IMinecraft) this.minecraft).getTimer();
        float partialTicksPlayer = this.partialTicksPlayer(partialTicksWorld);
        float rateMultiplier = ((AMixinTimer) timer).carpetClient$getWorldTickRate() /
            ((AMixinTimer) timer).carpetClient$getPlayerTickRate();

        // I wish preprocessor macros are a thing :( I could use Tuple but they do references
        if (rateMultiplier < 1) {
            {
                double diffraw = this.minecraft.player.x - this.minecraft.player.prevX;
                double sum = this.minecraft.player.prevX + diffraw * partialTicksPlayer;
                double diff = diffraw * rateMultiplier;
                this.minecraft.player.prevX = sum - diff * partialTicksWorld;
                this.minecraft.player.x = diff + this.minecraft.player.prevX;
            }
            {
                double diffraw = this.minecraft.player.y - this.minecraft.player.prevY;
                double sum = this.minecraft.player.prevY + diffraw * partialTicksPlayer;
                double diff = diffraw * rateMultiplier;
                this.minecraft.player.prevY = sum - diff * partialTicksWorld;
                this.minecraft.player.y = diff + this.minecraft.player.prevY;
            }
            {
                double diffraw = this.minecraft.player.z - this.minecraft.player.prevZ;
                double sum = this.minecraft.player.prevZ + diffraw * partialTicksPlayer;
                double diff = diffraw * rateMultiplier;
                this.minecraft.player.prevZ = sum - diff * partialTicksWorld;
                this.minecraft.player.z = diff + this.minecraft.player.prevZ;
            }
        } else {
            this.minecraft.player.prevX = this.minecraft.player.x = this.minecraft.player.prevX
                + (this.minecraft.player.x - this.minecraft.player.prevX) * partialTicksPlayer;
            this.minecraft.player.prevY = this.minecraft.player.y = this.minecraft.player.prevY
                + (this.minecraft.player.y - this.minecraft.player.prevY) * partialTicksPlayer;
            this.minecraft.player.prevZ = this.minecraft.player.z = this.minecraft.player.prevZ
                + (this.minecraft.player.z - this.minecraft.player.prevZ) * partialTicksPlayer;
        }

        try {
            this.renderWorld(partialTicksWorld, finishTimeNano);
        } finally {
            this.minecraft.player.prevX = savedLastX;
            this.minecraft.player.prevY = savedLastY;
            this.minecraft.player.prevZ = savedLastZ;
            this.minecraft.player.x = savedCurX;
            this.minecraft.player.y = savedCurY;
            this.minecraft.player.z = savedCurZ;
        }
    }

    /**
     * fix tick rate rendering glitch rendering view bobbing
     */
    @ModifyArg(method = {"setupCamera", "renderItemInHand"}, index = 0,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;loadCustom(F)V"))
    private float tickratePlayerBobbing(float partialTicksWorld) {
        return partialTicksPlayer(partialTicksWorld);
    }
}
