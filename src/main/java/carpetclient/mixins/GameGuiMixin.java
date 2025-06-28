package carpetclient.mixins;

import carpetclient.CarpetClient;
import carpetclient.Config;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.render.Window;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

/**
 * Removes scoreboard on the right, from nessie.
 */
@Mixin(GameGui.class)
public class GameGuiMixin {

    @Inject(method = "renderScoreboardObjective", at = @At("HEAD"), cancellable = true)
    private void toggleScoreboard(ScoreboardObjective objective, Window scaledRes, CallbackInfo ci) {
        if (Config.isScoreboardHidden.getValue()) {
            ci.cancel();
        }
    }
}
