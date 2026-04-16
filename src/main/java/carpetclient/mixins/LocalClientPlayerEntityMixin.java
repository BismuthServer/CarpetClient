package carpetclient.mixins;

import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import carpetclient.Config;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LocalClientPlayerEntity.class, priority = 999)
public abstract class LocalClientPlayerEntityMixin extends ClientPlayerEntity {

    @Shadow
    protected abstract boolean canSurvive(BlockPos pos);

    public LocalClientPlayerEntityMixin(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Inject(method = "mobTick", at = @At(value = "FIELD",
            target = "Lnet/minecraft/network/packet/c2s/play/PlayerMovementActionC2SPacket$Action;START_FALL_FLYING:Lnet/minecraft/network/packet/c2s/play/PlayerMovementActionC2SPacket$Action;"))
    public void onElytraDeploy(CallbackInfo ci) {
        if (Config.elytraFix.getValue())
            setFlag(7, true);
    }

    /**
     * Clientside fix for player clipping through other players.
     *
     * @return
     */
    public boolean isPushable() {
        return Config.playerCollisions;
    }

    /**
     * Fixes when going into elytra and cliping into roofs.
     *
     * @param pos
     * @param cir
     */
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void adjustIsOpenBlockSpace(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!this.world.getBlockState(pos).isSolid());
    }

    private boolean isHeadspaceFree(BlockPos pos, int height) {
        for (int y = 0; y < height; ++y) {
            if (!canSurvive(pos.add(0, y, 0))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Fixes players not pusing out of blocks for the elytra fix.
     *
     * Also the client side part of no-clip for creative mode.
     *
     * @param x
     * @param y
     * @param z
     * @return
     *
     * @author Xcom
     * @reason needed for pushing player out of blocks in the head position
     */
    @SuppressWarnings("OverwriteAuthorRequired")
    @Overwrite
    public boolean pushAwayFrom(double x, double y, double z) {
        if (this.noClip) {
            return false;
        } else {
            BlockPos blockpos = new BlockPos(x, y, z);
            double d0 = x - (double) blockpos.getX();
            double d1 = z - (double) blockpos.getZ();
            int entHeight = Math.max((int) Math.ceil(this.height), 1);
            boolean inTranslucentBlock = !this.isHeadspaceFree(blockpos, entHeight);

            if (inTranslucentBlock) {
                int i = -1;
                double d2 = 9999.0D;

                if (this.isHeadspaceFree(blockpos.west(), entHeight) && d0 < d2) {
                    d2 = d0;
                    i = 0;
                }
                if (this.isHeadspaceFree(blockpos.east(), entHeight) && 1.0D - d0 < d2) {
                    d2 = 1.0D - d0;
                    i = 1;
                }
                if (this.isHeadspaceFree(blockpos.north(), entHeight) && d1 < d2) {
                    d2 = d1;
                    i = 4;
                }
                if (this.isHeadspaceFree(blockpos.south(), entHeight) && 1.0D - d1 < d2) {
                    i = 5;
                }

                float f = 0.1F;

                if (i == 0) {
                    this.velocityX = -f;
                }
                if (i == 1) {
                    this.velocityX = f;
                }
                if (i == 4) {
                    this.velocityZ = -f;
                }
                if (i == 5) {
                    this.velocityZ = f;
                }
            }
            return false;
        }
    }
}
