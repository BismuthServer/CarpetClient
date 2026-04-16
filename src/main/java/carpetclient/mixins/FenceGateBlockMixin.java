package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
Mixen class to implement relaxed block placement, placement of fences in mid air.
 */
@Mixin(FenceGateBlock.class)
public class FenceGateBlockMixin extends HorizontalFacingBlock {

    protected FenceGateBlockMixin(Material materialIn) {
        super(materialIn);
    }

    // Override to place fence gates mid air
    @Inject(method = "canBePlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    public void canPlaceOnOver(World worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue((worldIn.getBlockState(pos.down()).getMaterial().isSolid() || Config.relaxedBlockPlacement) && super.canBePlaced(worldIn, pos));
    }
}
