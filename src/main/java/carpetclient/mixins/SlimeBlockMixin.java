package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.coders.EDDxample.PistonHelper;
import net.minecraft.block.PistonBaseBlock;
import net.minecraft.block.SlimeBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SlimeBlock.class)
public class SlimeBlockMixin extends TransparentBlock {

    protected SlimeBlockMixin(Material materialIn, boolean ignoreSimilarityIn) {
        super(materialIn, ignoreSimilarityIn);
    }

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

        boolean isSticky = false;
        boolean extending = true;
        boolean flag = playerIn.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && playerIn.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.AIR;

        if (worldIn.isClient && flag) {
            pos = pos.offset(facing);
            state = new PistonBaseBlock(false).defaultState().set(PistonBaseBlock.FACING, facing.getOpposite()).set(PistonBaseBlock.EXTENDED, Boolean.FALSE);
            if ((!PistonHelper.activated || !pos.equals(PistonHelper.pistonPos)) && (extending || isSticky)) {
                PistonHelper.setPistonMovement(worldIn, state, pos, extending);
            } else {
                PistonHelper.activated = false;
            }
        }

        return flag;
    }
}
