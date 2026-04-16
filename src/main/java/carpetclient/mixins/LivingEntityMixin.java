package carpetclient.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import carpetclient.Config;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.world.World;

@Mixin(value = LivingEntity.class, priority = 999)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = "moveRelative", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isClient:Z", ordinal = 1))
    public boolean redirectOnElytraLanded(World world) {
        if (Config.elytraFix.getValue())
            return world.isClient && !((Entity) this instanceof LocalClientPlayerEntity);
        else
            return world.isClient;
    }
}
