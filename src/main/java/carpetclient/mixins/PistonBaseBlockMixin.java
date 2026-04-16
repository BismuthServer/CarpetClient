package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.Hotkeys;
import carpetclient.coders.EDDxample.PistonHelper;
import carpetclient.util.ITileEntityPiston;
import carpetclient.util.IWorldServer;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonBaseBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MovingBlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.piston.PistonMoveStructureResolver;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.property.BooleanProperty;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

/*
Mixen class
1.to make piston/sticky-piston properly rotate without visual glitches when doing "accurateBlockPlacement".
2.ghost block fix for sticky pistons
 */
@Mixin(PistonBaseBlock.class)
public abstract class PistonBaseBlockMixin extends FacingBlock {

    private List<BlockEntity> tileEntitiesList;

    private BlockPos blockpos; // For movableTE

    private int mixinEventParam; // For ghost blocks fix

    @Shadow
    private void checkExtended(World worldIn, BlockPos pos, BlockState state) {
    }

    @Shadow
    public static @Final
    BooleanProperty EXTENDED;

    @Shadow
    private @Final
    boolean sticky;

    @Shadow
    private boolean move(World worldIn, BlockPos pos, Direction direction, boolean extending) {
        return false;
    }

    protected PistonBaseBlockMixin(Material materialIn) {
        super(materialIn);
    }

    // Override this method to comment out a useless line.
    @Inject(method = "onPlaced", at = @At("HEAD"), cancellable = true)
    public void canPlaceOnOver(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci) {
        if (!worldIn.isClient) {
            this.checkExtended(worldIn, pos, state);
        }
        ci.cancel();
    }

    // Override to fix a client side visual affect when placing blocks in a different orientation.
    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    public void canPlaceOnOver(World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer, CallbackInfoReturnable<BlockState> cir) {
        if (Config.accurateBlockPlacement) {
            if (!Hotkeys.isKeyDown(Hotkeys.toggleBlockFacing.getKeyCode())) {
                facing = Direction.nearest(pos, placer).getOpposite();
            }
            if (!Hotkeys.isKeyDown(Hotkeys.toggleBlockFlip.getKeyCode())) {
                facing = facing.getOpposite();
            }
        } else {
            facing = Direction.nearest(pos, placer);
        }
        cir.setReturnValue(this.defaultState().set(FACING, facing).set(EXTENDED, Boolean.FALSE));
    }

    /*
     * This if statement checks if the the pulling block (block that is 2 blocks infront of the extended piston)
     * is a non-moving block and returns a meta value of 16 so it can tell the client to ignore pulling blocks
     * even if the client can pull them.
     */
    @Unique
	private int ignoreMovingBlockMeta(World worldIn, BlockPos pos, Direction enumfacing) {
        BlockPos blockpos = pos.add(enumfacing.getOffsetX() * 2, enumfacing.getOffsetY() * 2, enumfacing.getOffsetZ() * 2);
        BlockState iblockstate = worldIn.getBlockState(blockpos);
        Block block = iblockstate.getBlock();

        if (block == Blocks.MOVING_BLOCK) return 16;

        return 0;
    }

//    // Inject into block activated to show piston update order
//    @Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true)
//    public void pistonUpdateOrder(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {

    /**
     * Add block activation to get piston update order, code provided by EDDxample.
     *
     * @param worldIn
     * @param pos
     * @param state
     * @param playerIn
     * @param hand
     * @param facing
     * @param hitX
     * @param hitY
     * @param hitZ
     * @return
     */
    @Override
    public boolean use(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (!Config.pistonVisualizer.getValue()) return false;

        boolean flag = playerIn.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && playerIn.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.AIR;

        if (worldIn.isClient && flag) {
            boolean extending = !(Boolean) state.get(PistonBaseBlock.EXTENDED);
            if ((!pos.equals(PistonHelper.pistonPos) || !PistonHelper.activated) && (extending || sticky)) {
                PistonHelper.setPistonMovement(worldIn, state, pos, extending);
            } else {
                PistonHelper.activated = false;
            }
            if (worldIn.isClient) {
                return sticky || !(Boolean) state.get(PistonBaseBlock.EXTENDED);
            }
        }

        return flag;
    }

    // MovableTE
    @Redirect(method = "canMoveBlock", at = @At(value="INVOKE", target = "Lnet/minecraft/block/Block;hasBlockEntity()Z"))
    private static boolean canPushTE(Block block)
    {
        if (!Config.movableTileEntities)
            return block.hasBlockEntity();

        if (!block.hasBlockEntity())
            return !true;
        else
            return !(isPushableTileEntityBlock(block));
    }

    private static boolean isPushableTileEntityBlock(Block block)
    {
        //Making PISTON_EXTENSION (BlockPistonMoving) pushable would not work as its createNewTileEntity()-method returns null
        return block != Blocks.ENDER_CHEST && block != Blocks.ENCHANTING_TABLE && block != Blocks.END_GATEWAY
                && block != Blocks.END_PORTAL && block != Blocks.MOB_SPAWNER && block != Blocks.MOVING_BLOCK;
    }

    @Inject(method = "move", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Ljava/util/List;size()I", remap = false, ordinal = 4), locals = LocalCapture.CAPTURE_FAILHARD)
    private void doMoveTE(World worldIn, BlockPos pos, Direction direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                          PistonMoveStructureResolver blockpistonstructurehelper, List<BlockPos> list, List<BlockState> list1,
                          List<BlockPos> list2, int k,  BlockState[] aiblockstate, Direction enumfacing) {
        doMoveTE(worldIn, pos, direction, extending, cir, blockpistonstructurehelper, list, list1, list2, k, aiblockstate);
    }

    @Surrogate // EnumFacing local var only present in recompiled Minecraft
    private void doMoveTE(World worldIn, BlockPos pos, Direction direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                          PistonMoveStructureResolver blockpistonstructurehelper, List<BlockPos> list, List<BlockState> list1,
                          List<BlockPos> list2, int k,  BlockState[] aiblockstate) {
        if (!Config.movableTileEntities)
            return;

        tileEntitiesList = Lists.newArrayList();
        for (int i = 0; i < list.size(); i++)
        {
            BlockPos blockPos = list.get(i);
            BlockEntity tileEntity = worldIn.getBlockEntity(blockPos);
            tileEntitiesList.add(tileEntity);
            if (tileEntity != null)
            {
                worldIn.removeBlockEntity(blockPos);
                tileEntity.markDirty();
            }
        }
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "net/minecraft/world/World.setBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;)V", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void setTileEntityTE_NoShift(World worldIn, BlockPos pos, Direction direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                                 PistonMoveStructureResolver blockpistonstructurehelper, List<BlockPos> list, List<BlockState> list1,
                                 List<BlockPos> list2, int k,  BlockState[] aiblockstate, Direction enumfacing,
                                 int l, BlockPos blockpos3, BlockState iblockstate2)
    {
        this.blockpos = blockpos3;
    }

    // Only For dev environment
    @Inject(method = "move", at = @At(value = "INVOKE", target = "net/minecraft/world/World.setBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;)V", shift = At.Shift.AFTER, ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void setTileEntityTE(World worldIn, BlockPos pos, Direction direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                                 PistonMoveStructureResolver blockpistonstructurehelper, List<BlockPos> list, List<BlockState> list1,
                                 List<BlockPos> list2, int k,  BlockState[] aiblockstate, Direction enumfacing,
                                 int l, BlockPos blockpos3, BlockState iblockstate2)
    {
        if (!Config.movableTileEntities)
            return;

        this.blockpos = blockpos3;
        setTileEntityTE(worldIn, pos, direction, extending, cir, blockpistonstructurehelper, list, list1, list2, k, aiblockstate, l);
    }
    // Only for dev environment

    @Surrogate
    private void setTileEntityTE(World worldIn, BlockPos pos, Direction direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                          PistonMoveStructureResolver blockpistonstructurehelper, List<BlockPos> list, List<BlockState> list1,
                          List<BlockPos> list2, int k,  BlockState[] aiblockstate, int l)
    {
        if (!Config.movableTileEntities)
            return;

        BlockEntity e = worldIn.getBlockEntity(this.blockpos);
        if (!(e instanceof MovingBlockEntity))
            return;

        ((ITileEntityPiston) e).carpetClient$setCarriedBlockEntity(tileEntitiesList.get(l));
    }
    // End MovableTE

    @Redirect(method = "checkExtended", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V", ordinal = 1))
    private void sendDropBlockFlag(World world, BlockPos pos, Block blockIn, int eventID, int eventParam, World worldIn, BlockPos callpos, BlockState state)
    {
        int suppress_move = 0;

        if (Config.pistonGhostBlocksFix.equals("clientAndServer"))
        {
            final Direction enumfacing = state.get(FACING);

            final BlockPos blockpos = new BlockPos(callpos).offset(enumfacing, 2);
            final BlockState iblockstate = worldIn.getBlockState(blockpos);

            if (iblockstate.getBlock() == Blocks.MOVING_BLOCK)
            {
                final BlockEntity tileentity = worldIn.getBlockEntity(blockpos);

                if (tileentity instanceof MovingBlockEntity)
                {
                    final MovingBlockEntity tileentitypiston = (MovingBlockEntity) tileentity;
                    if (tileentitypiston.getFacing() == enumfacing && tileentitypiston.isExtending()
                                && (((ITileEntityPiston) tileentitypiston).carpetClient$getLastProgress() < 0.5F
                                            || tileentitypiston.getWorld().getTime() == ((ITileEntityPiston) tileentitypiston).carpetClient$getLastTicked()
                                            || !((IWorldServer) worldIn).carpetClient$haveBlockActionsProcessed()))
                    {
                        suppress_move = 16;
                    }
                }
            }
        }

        worldIn.addBlockEvent(pos, blockIn, eventID, eventParam | suppress_move);
    }

    @Inject(method = "doEvent", at = @At("HEAD"))
    private void setEventParam(BlockState state, World worldIn, BlockPos pos, int id, int param, CallbackInfoReturnable<Integer> cir)
    {
        this.mixinEventParam = param;
    }

    @ModifyVariable(method = "doEvent", name = "flag1", index = 11, at = @At(value = "LOAD", ordinal = 0))
    private boolean didServerDrop(boolean flag1)
    {
        if ((this.mixinEventParam & 16) == 16 && Config.pistonGhostBlocksFix.equals("clientAndServer"))
        {
            flag1 = true;
        }

        return flag1;
    }
}
