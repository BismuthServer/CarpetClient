package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mixin(World.class)
public class WorldMixin {

    @Shadow @Final public List<BlockEntity> blockEntities;
    @Shadow @Final public List<BlockEntity> tickingBlockEntities;
    @Shadow @Final private List<BlockEntity> removedBlockEntities;

    /**
     * Ignoring entity's when placing  block clientside.
     * @param world
     * @param bb
     * @param entityIn
     * @param blockIn
     * @param pos
     * @param skipCollisionCheck
     * @param sidePlacedOn
     * @param placer
     * @return
     */
    @Redirect(method = "canPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isUnobstructed(Lnet/minecraft/util/math/Box;Lnet/minecraft/entity/Entity;)Z"))
    private boolean ignoreEntitysWhenPlacingBlocks(World world,
												   Box bb, @Nullable Entity entityIn, // sub vars
												   Block blockIn, BlockPos pos, boolean skipCollisionCheck, Direction sidePlacedOn, @Nullable Entity placer// main vars
    ){
        return Config.ignoreEntityWhenPlacing || world.isUnobstructed(bb, entityIn);
    }

    /**
     * Tile entity removal lag fix from forge.
     *
     * @param ci
     */
    @Inject(method = "tickEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;removedBlockEntities:Ljava/util/List;", ordinal = 0))
    private void tileEntityRemovalFix(CallbackInfo ci){
        if (!this.removedBlockEntities.isEmpty()) {
            Set<BlockEntity> remove = Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            remove.addAll(this.removedBlockEntities);
            this.tickingBlockEntities.removeAll(remove);
            this.blockEntities.removeAll(remove);
            this.removedBlockEntities.clear();
        }
    }

    /**
     * Prevent updateEntities targeting the player during world tick
     *
     * @param world
     * @param ent
     */
    @Redirect(method = "tickEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;tickEntity(Lnet/minecraft/entity/Entity;)V"))
    private void noPlayerUpdateDuringWorld(World world, Entity ent){
        if (!(ent instanceof LocalClientPlayerEntity) || ent.isRiding())
            world.tickEntity(ent);
    }
}
