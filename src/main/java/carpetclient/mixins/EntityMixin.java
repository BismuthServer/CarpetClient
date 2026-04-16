package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
A Mixins class to override a method to change the players aim.
 */
@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public float yaw;
    @Shadow
    private Entity mount;

    @Shadow
    public World world;
    @Unique
	private float storedRotationYaw;

    @Shadow
    public double velocityX;
    @Shadow
    public double velocityY;
    @Shadow
    public double velocityZ;

    @Shadow
    public void setShape(Box bb) {
    }

    @Shadow
    public Box getShape() {
        return null;
    }

    @Shadow
    public void setPositionFromShape() {
    }

//    /*
//    Override to change the behavior of player aiming.
//     */
//    @Overwrite
//    public void turn(float yaw, float pitch) {
//        float f = this.rotationPitch;
//        float f1 = this.rotationYaw;
//        this.rotationYaw = (float) ((double) this.rotationYaw + (double) yaw * 0.15D);
//        this.rotationPitch = (float) ((double) this.rotationPitch - (double) pitch * 0.15D);
//        this.rotationPitch = MathHelper.clamp(this.rotationPitch, -90.0F, 90.0F);
//        this.prevRotationPitch += this.rotationPitch - f;
//
//        snapAim(this.ridingEntity != null, yaw);
//        this.prevRotationYaw += this.rotationYaw - f1;
//
//        if (this.ridingEntity != null) {
//            this.ridingEntity.applyOrientationToEntity((Entity) (Object) this);
//        }
//    }

    /*
    Injection to modify the behavior of player aiming.
     */
    @Inject(method = "updateLocalPlayerCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;lastPitch:F", opcode = Opcodes.PUTFIELD))
    public void post(float yaw, float pitch, CallbackInfo ci) {
        snapAim(this.mount, yaw);
    }

    /*
    Fixes snapaim inaccuracy when smapping to different angles.
     */
    @Inject(method = "updateVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;sin(F)F"), cancellable = true)
    public void snapFix(float strafe, float up, float forward, float friction, CallbackInfo ci) {
        if (Config.snapAim && (Object) this instanceof LocalClientPlayerEntity) {
            float f1 = (float) Math.sin(this.yaw * 0.01745329251994329576923690768489D);
            float f2 = (float) Math.cos(this.yaw * 0.01745329251994329576923690768489D);
            f1 = round(f1, 8);
            f2 = round(f2, 8);
            this.velocityX += (double) (strafe * f2 - forward * f1);
            this.velocityY += (double) up;
            this.velocityZ += (double) (forward * f2 + strafe * f1);
            ci.cancel();
        }
    }

    private float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = (int) Math.abs(value);
        if (value < 0) tmp *= -1;
        return (float) tmp / factor;
    }

    /**
     * Updates the value stored rotation yaw so no jurking actions is done when turning on the rotation.
     */
    public void updateStoredRotationYaw() {
        storedRotationYaw = yaw;
    }

    /**
     * Overriding the aim of the player to snap to angles of 45 degrees
     *
     * @param riding Gets the type of entity the player is riding.
     * @param yaw    The angle in which the player will turn, sent in from the turn method.
     */
    private void snapAim(Entity riding, float yaw) {
        if (!((Object) this instanceof LocalClientPlayerEntity)) {
            return;
        }
        boolean inBoat = false;

        if (riding != null && riding instanceof BoatEntity) {
            inBoat = true;
        }

        if (inBoat || !Config.snapAim) {
            updateStoredRotationYaw();
            return;
        }

        this.storedRotationYaw = (float) ((double) this.storedRotationYaw + (double) yaw * 0.15D);

        float r = storedRotationYaw - storedRotationYaw % 45;
        float r2 = Math.abs(r - storedRotationYaw);
        if (r2 < 5) {
            this.yaw = r;
        } else if (r2 > 40) {
            if (storedRotationYaw < 0)
                this.yaw = r - 45;
            else
                this.yaw = r + 45;
        } else {
            this.yaw = storedRotationYaw;
        }
    }

    /**
     * Return out of the piston anti-cheat logic to prevent player from being pushed to many times in the same gametick on the client.
     *
     * @return
     */
    @Redirect(method = "move", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/MoverType;PISTON:Lnet/minecraft/entity/MoverType;", opcode = Opcodes.GETSTATIC))
    public MoverType redirectMoveType() {
        if (world.isClient && Config.clipThroughPistons.getValue()) {
            return MoverType.SHULKER;
        }
        return MoverType.PISTON;
    }

    /**
     * Fixes step height being 1.0 instead of 0.6 when being pushed by pistons.
     *
     * @param owner
     * @param type
     * @param x
     * @param y
     * @param z
     * @return
     */
    @Redirect(method = "move", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;stepHeight:F", opcode = Opcodes.GETFIELD, ordinal = 4))
    private float getStepHeight1(Entity owner, MoverType type, double x, double y, double z) {
        if (Config.clipThroughPistons.getValue() && owner instanceof PlayerEntity && type == MoverType.PISTON) return 1.0f;
        return owner.stepHeight;
    }
}
