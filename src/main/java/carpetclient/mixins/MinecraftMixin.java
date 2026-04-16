package carpetclient.mixins;

import carpetclient.CarpetClient;
import carpetclient.Config;
import carpetclient.bugfix.PistonFix;
import carpetclient.mixinInterface.AMixinMinecraft;
import carpetclient.mixinInterface.AMixinTimer;
import carpetclient.rules.TickRate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TickTimer;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportCategory;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * Tick rate editing in Minecraft.java based on Cubitecks tick rate mod.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements IMinecraft, AMixinMinecraft {
    @Shadow
    private @Final
    TickTimer timer;
    @Shadow
    private boolean paused;
    @Shadow
    public ClientWorld world;
    @Shadow
    public LocalClientPlayerEntity player;
    @Shadow
    private float pauseTickDelta;
    @Shadow @Nullable
    public Screen screen;
    @Shadow @Nullable
    private IntegratedServer server;
    @Shadow
    private int width;
    @Shadow
    private int height;
    @Shadow
    private boolean fullscreen;

    // private float renderPartialTicksPausedWorld;
    @Unique
	private float renderPartialTicksPausedPlayer;

    @Shadow
    abstract public boolean isSingleplayer();

    @Override
    public float carpetClient$getRenderPartialTicksPausedPlayer() {
        return this.renderPartialTicksPausedPlayer;
    }

    /**
     * Reset logic for clipping through pistons.
     *
     * @param ci
     */
    @Inject(method = "tick", at = @At("HEAD"))
    public void fixingPistons(CallbackInfo ci) {
        PistonFix.resetBools();
    }

    /**
     * Reset logic for clipping through pistons.
     *
     * @param ci
     */
    @Inject(method = "runGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isSingleplayer()Z"))
    private void renderPartialTicksPausing(CallbackInfo ci) {
        boolean flag = this.isSingleplayer() && this.screen != null && this.screen.shouldPauseGame() && !this.server.isPublished();
        if (this.paused != flag) {
            if (this.paused) {
                // Will be done by vanilla
                // this.renderPartialTicksPaused = ((AMixinTimer) this.timer).getRenderPartialTicksWorld();
                this.renderPartialTicksPausedPlayer = ((AMixinTimer) this.timer).carpetClient$getRenderPartialTicksPlayer();
            } else {
                ((AMixinTimer) this.timer).carpetClient$setRenderPartialTicksWorld(this.pauseTickDelta);
                ((AMixinTimer) this.timer).carpetClient$setRenderPartialTicksPlayer(this.renderPartialTicksPausedPlayer);
            }

            // Will be done by vanilla
            // this.isGamePaused = flag;
        }
    }

    /**
     * Tick the player at the rate of player rate
     *
     * @param ci
     */
    @Inject(method = "tick", at = @At(value = "INVOKE_STRING",
        target="Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
        args = { "ldc=level" }, shift = At.Shift.AFTER))
    private void tickPlayer(CallbackInfo ci) {
        if (
            this.world != null && this.player != null && !this.paused &&
            ((AMixinTimer) this.timer).carpetClient$getElapsedTicksPlayer() > 0 &&
            !this.player.isRiding() && !this.player.removed
        ) {
            try {
                this.world.tickEntity(this.player);
            } catch (Throwable e) {
                CrashReport cr = CrashReport.of(e, "Ticking player");
                CrashReportCategory crcat = cr.addCategory("Player being ticked");
                this.player.populateCrashReport(crcat);
                throw new CrashException(cr);
            }
        }
    }

    /**
     * Modify constant in scroll mouse to fix the issue when slowing down.
     */
    @ModifyConstant(method = "handleMouseEvents", constant = @Constant(longValue = 200L))
    private long runTickMouseFix(long value) {
        if (TickRate.runTickRate) {
            return (long) Math.max(200F * (20.0f / Config.tickRate), 200L);
        } else {
            return 200L;
        }
    }

    @Inject(method = "onResolutionChanged()V", at = @At("TAIL"))
    private void onResolutionChanged(CallbackInfo ci) {
        CarpetClient.onViewportResized(new Window((Minecraft) (Object) this), this.width, this.height);
    }

    @Inject(method = "toggleFullscreen", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/Minecraft;updateDisplay()V"))
    private void onFullscreenToggled(CallbackInfo ci) {
        CarpetClient.onFullScreenToggled(this.fullscreen);
    }
}
