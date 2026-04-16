package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.util.ITileEntityPiston;
import carpetclient.util.ITileEntityRenderDispatcher;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MovingBlockEntity;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.MovingBlockRenderer;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MovingBlockRenderer.class)
public abstract class MovingBlockRendererMixin extends BlockEntityRenderer<MovingBlockEntity>
{
    @Shadow protected abstract boolean renderBlock(BlockPos pos, BlockState state, BufferBuilder buffer, World p_188186_4_, boolean checkSides);

    @Unique
	private MovingBlockEntity piston;
    @Unique
	private float partialTicks;
    @Unique
	private int destroyStage;

    @Inject(method = "render*",
            at = @At(value = "INVOKE",
                    shift = At.Shift.BEFORE,
                    target = "net/minecraft/client/render/block/entity/MovingBlockRenderer.renderBlock (Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;Lcom/mojang/blaze3d/vertex/BufferBuilder;Lnet/minecraft/world/World;Z)Z",
                    ordinal = 3),
            locals = LocalCapture.CAPTURE_FAILHARD
	)
    private void renderStateModelTE(MovingBlockEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci,
                                    BlockPos blockpos, BlockState iblockstate, Block block, Tesselator tesselator, BufferBuilder bufferbuilder, World world)
    {
        if (!Config.movableTileEntities)
            return;

        this.piston = te;
        this.partialTicks = partialTicks;
        this.destroyStage = destroyStage;
    }

    @Redirect(method = "render*",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/client/render/block/entity/MovingBlockRenderer.renderBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;Lcom/mojang/blaze3d/vertex/BufferBuilder;Lnet/minecraft/world/World;Z)Z",
                    ordinal = 3)
    )
    private boolean renderStateModelTE(net.minecraft.client.render.block.entity.MovingBlockRenderer renderer, BlockPos pos, BlockState state, BufferBuilder buffer, World world, boolean checkSides)
    {
        if (Config.movableTileEntities && !((IBufferBuilder) Tesselator.getInstance().getBuffer()).getBuilding())
        {
            BlockEntity carriedTileEntity = ((ITileEntityPiston) this.piston).carpetClient$getCarriedBlockEntity();
            if (carriedTileEntity != null)
            {
                if (BlockEntityRenderDispatcher.INSTANCE.getRenderer(carriedTileEntity) == null)
                {
                    return this.renderBlock(pos, state, buffer, world, checkSides);
                }
                else
                {
                    carriedTileEntity.setPos(this.piston.getPos());
                    ((ITileEntityRenderDispatcher) BlockEntityRenderDispatcher.INSTANCE).carpetClient$renderTileEntityOffset(carriedTileEntity, this.partialTicks, this.destroyStage, this.piston.getRenderOffsetX(this.partialTicks), this.piston.getRenderOffsetY(this.partialTicks), this.piston.getRenderOffsetZ(this.partialTicks));
                    return true;
                }
            }
        }

        return this.renderBlock(pos, state, buffer, world, checkSides);
    }
}
