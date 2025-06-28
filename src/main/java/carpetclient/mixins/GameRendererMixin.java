package carpetclient.mixins;

import carpetclient.CarpetClient;
import carpetclient.Config;
import carpetclient.gui.chunkgrid.GuiChunkGrid;
import carpetclient.mixinInterface.AMixinEntityRenderer;
import carpetclient.mixinInterface.AMixinMinecraft;
import carpetclient.mixinInterface.AMixinTimer;
import carpetclient.rules.TickRate;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.Tessellator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TickTimer;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Window;
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
	@Inject(method = "render(FJ)V", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/GameGui.render (F)V"))
	public void render(float tickDelta, long startTime, CallbackInfo ci) {
		if (this.minecraft.world != null && this.minecraft.player != null) {
			if (GuiChunkGrid.instance.getMinimapType() != 0) {
				Window window = new Window(this.minecraft);
				GuiChunkGrid.instance.renderMinimap(window.getWidth(), window.getHeight());
			}
		}
	}
	@Inject(method = "render(IFJ)V",at=@At(value = "INVOKE_STRING", target = "net/minecraft/util/profiler/Profiler.swap (Ljava/lang/String;)V", args = "ldc=litParticles"))
	public void render(int i, float f, long l, CallbackInfo ci) {
		CarpetClient.onPostRenderEntities(f);
	}
	@Inject(method = "render(IFJ)V", at = @At(value = "FIELD", target = "net/minecraft/client/render/GameRenderer.newCamPitch : Z"))
	private void onPostRender(int anaglyphRenderPass, float tickDelta, long renderTimeLimit, CallbackInfo ci) {
		CarpetClient.onPostRender(tickDelta);
	}

    /**
     * Get player partial tick for rendering
     */
    @Override
    public float partialTicksPlayer(float partialTicksWorld) {
        if (this.minecraft.player.hasVehicle())
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
        if (!TickRate.runTickRate || this.minecraft.player.hasVehicle()) {
            if(!((IBufferBuilder) Tessellator.getInstance().getBuilder()).getBuilding()) {
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

        double savedLastX = this.minecraft.player.prevTickX;
        double savedLastY = this.minecraft.player.prevTickY;
        double savedLastZ = this.minecraft.player.prevTickZ;
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
                double diffraw = this.minecraft.player.x - this.minecraft.player.prevTickX;
                double sum = this.minecraft.player.prevTickX + diffraw * partialTicksPlayer;
                double diff = diffraw * rateMultiplier;
                this.minecraft.player.prevTickX = sum - diff * partialTicksWorld;
                this.minecraft.player.x = diff + this.minecraft.player.prevTickX;
            }
            {
                double diffraw = this.minecraft.player.y - this.minecraft.player.prevTickY;
                double sum = this.minecraft.player.prevTickY + diffraw * partialTicksPlayer;
                double diff = diffraw * rateMultiplier;
                this.minecraft.player.prevTickY = sum - diff * partialTicksWorld;
                this.minecraft.player.y = diff + this.minecraft.player.prevTickY;
            }
            {
                double diffraw = this.minecraft.player.z - this.minecraft.player.prevTickZ;
                double sum = this.minecraft.player.prevTickZ + diffraw * partialTicksPlayer;
                double diff = diffraw * rateMultiplier;
                this.minecraft.player.prevTickZ = sum - diff * partialTicksWorld;
                this.minecraft.player.z = diff + this.minecraft.player.prevTickZ;
            }
        } else {
            this.minecraft.player.prevTickX = this.minecraft.player.x = this.minecraft.player.prevTickX
                + (this.minecraft.player.x - this.minecraft.player.prevTickX) * partialTicksPlayer;
            this.minecraft.player.prevTickY = this.minecraft.player.y = this.minecraft.player.prevTickY
                + (this.minecraft.player.y - this.minecraft.player.prevTickY) * partialTicksPlayer;
            this.minecraft.player.prevTickZ = this.minecraft.player.z = this.minecraft.player.prevTickZ
                + (this.minecraft.player.z - this.minecraft.player.prevTickZ) * partialTicksPlayer;
        }

        try {
            this.renderWorld(partialTicksWorld, finishTimeNano);
        } finally {
            this.minecraft.player.prevTickX = savedLastX;
            this.minecraft.player.prevTickY = savedLastY;
            this.minecraft.player.prevTickZ = savedLastZ;
            this.minecraft.player.x = savedCurX;
            this.minecraft.player.y = savedCurY;
            this.minecraft.player.z = savedCurZ;
        }
    }

    /**
     * fix tick rate rendering glitch rendering view bobbing
     */
    @ModifyArg(method = {"setupCamera", "renderItemInHand"}, index = 0,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;applyViewBobbing(F)V"))
    private float tickratePlayerBobbing(float partialTicksWorld) {
        return partialTicksPlayer(partialTicksWorld);
    }
}
