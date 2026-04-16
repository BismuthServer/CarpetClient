package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.living.player.PlayerAbilities;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends Entity {
    @Shadow public abstract boolean isCreative();

    @Shadow public PlayerAbilities abilities;

    public PlayerEntityMixin(World world) {
        super(world);
    }

    /**
     * Allows creative players to no clip
     * @param player
     * @return
     */
    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/player/PlayerEntity;isSpectator()Z"))
    private boolean updateNoClipping(PlayerEntity player) {
        return player.isSpectator() || (Config.creativeModeNoClip.getValue() && player.isCreative() && player.abilities.flying);
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
        if (type == MoverType.SELF || !(Config.creativeModeNoClip.getValue() && this.isCreative() && this.abilities.flying)) {
            super.move(type, x, y, z);
        }
    }
}
