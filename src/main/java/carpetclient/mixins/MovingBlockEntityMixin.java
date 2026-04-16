package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.bugfix.PistonFix;
import carpetclient.util.ITileEntityPiston;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityProvider;
import net.minecraft.block.entity.MovingBlockEntity;
import net.minecraft.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import java.util.List;

@Mixin(MovingBlockEntity.class)
public class MovingBlockEntityMixin extends BlockEntity implements ITileEntityPiston
{
    @Shadow private BlockState movedState;

    @Shadow private float lastProgress;

    @Unique
	private BlockEntity carriedTileEntity;

    public BlockEntity carpetClient$getCarriedBlockEntity()
    {
        return carriedTileEntity;
    }

    public void carpetClient$setCarriedBlockEntity(BlockEntity tileEntity)
    {
        this.carriedTileEntity = tileEntity;
        if (this.carriedTileEntity != null)
            this.carriedTileEntity.setPos(this.pos);
    }

    @Unique
	private long lastTicked;

    @Inject(method = "tick", at = @At("HEAD"))
    private void setLastTicked(CallbackInfo ci)
    {
        this.lastTicked = this.world.getTime();
    }

    @Override
    public long carpetClient$getLastTicked()
    {
        return this.lastTicked;
    }

    @Override
    public float carpetClient$getLastProgress()
    {
        return this.lastProgress;
    }

    /**
     * Updates player being moved to simulate regular game logic where players move before tile entitys.
     *
     * @param p_184322_1_
     * @param ci
     */
    @Inject(method = "moveEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/MovingBlockEntity;NOCLIP_DIR:Ljava/lang/ThreadLocal;", shift = At.Shift.AFTER, ordinal = 0))
    private void handleRecipeClickedd(float p_184322_1_, CallbackInfo ci) {
        PistonFix.movePlayer();
    }

    /**
     * force updates the player to move the player into the new chunk. Fix for MC-108469.
     *
     * @param p_184322_1_
     * @param ci
     * @param enumfacing
     * @param d0
     * @param list
     * @param axisalignedbb
     * @param list1
     * @param flag
     * @param i
     * @param entity
     * @param d1
     */
    @Inject(method = "moveEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/MovingBlockEntity;NOCLIP_DIR:Ljava/lang/ThreadLocal;", shift = At.Shift.AFTER, ordinal = 1), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void forceUpdate(float p_184322_1_, CallbackInfo ci, Direction enumfacing, double d0, List<Box> list, Box axisalignedbb, List<Entity> list1, boolean flag, int i, Entity entity, double d1) {
        if (!Config.clipThroughPistons.getValue()) return;

        world.tickEntity(entity, false);
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void readFromNBTTE(NbtCompound compound, CallbackInfo ci)
    {
        if (!Config.movableTileEntities)
            return;

        if (compound.contains("carriedTileEntity"))
        {
            Block block = this.movedState.getBlock();
            if (block instanceof BlockEntityProvider)
            {
                this.carriedTileEntity = ((BlockEntityProvider) block).createBlockEntity(this.world, block.getMetadataFromState(this.movedState));
            }

            if (carriedTileEntity != null)
            {
                this.carriedTileEntity.readNbt(compound.getCompound("carriedTileEntity"));
            }
        }
    }

    @Inject(method = "writeNbt", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void writeToNBTTE(NbtCompound compound, CallbackInfoReturnable<NbtCompound> ci)
    {
        if (!Config.movableTileEntities)
            return;

        if (carriedTileEntity != null)
        {
            compound.put("carriedTileEntity", this.carriedTileEntity.writeNbt(new NbtCompound()));
        }
    }

    @Inject(method = "finish", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/block/entity/MovingBlockEntity;markRemoved()V"), cancellable = true)
    private void clearPistonTileEntityTE(CallbackInfo ci)
    {
        if (!Config.movableTileEntities)
            return;

        if (this.world.getBlockState(this.pos).getBlock() == Blocks.MOVING_BLOCK)
        {
            this.placeBlock();
        }
        else if (!this.world.isClient && this.carriedTileEntity != null && this.world.getBlockState(this.pos).getBlock() == Blocks.AIR)
        {
            this.placeBlock();
            this.world.removeBlock(this.pos);
        }
        ci.cancel();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/block/entity/MovingBlockEntity;markRemoved()V"), cancellable = true)
    private void updateTE(CallbackInfo ci)
    {
        if (!Config.movableTileEntities)
            return;

        if (this.world.getBlockState(this.pos).getBlock() == Blocks.MOVING_BLOCK)
        {
            this.placeBlock();
        }
        ci.cancel();
    }

    private void placeBlock()
    {
        // workaround for the direction change caused by BlockDispenser.onBlockAdded();
        Block block = this.movedState.getBlock();
        if (block instanceof DispenserBlock || block instanceof FurnaceBlock)
        {
            this.world.setBlockState(this.pos, this.movedState, 18);
        }
        //workaround is just placing the block twice. This should not cause any problems, but is ugly code

        this.world.setBlockState(this.pos, this.movedState, 18);  //Flag 18 => No block updates, TileEntity has to be placed first

        if (!this.world.isClient)
        {
            if (carriedTileEntity != null)
            {
                this.world.removeBlockEntity(this.pos);
                carriedTileEntity.cancelRemoval();
                this.world.setBlockEntity(this.pos, carriedTileEntity);
            }

            //Update neighbors, comparators and observers now (same order as setBlockState would have if flag was set to 3 (default))
            //This should not change piston behavior for vanilla-pushable blocks at all

            this.world.onBlockChanged(pos, Blocks.MOVING_BLOCK, true);
            if (this.movedState.isAnalogSignalSource())
            {
                this.world.updateNeighborComparators(pos, this.movedState.getBlock());
            }
            this.world.updateObservers(pos, this.movedState.getBlock());
        }
        this.world.neighborChanged(this.pos, this.movedState.getBlock(), this.pos);
    }
}
