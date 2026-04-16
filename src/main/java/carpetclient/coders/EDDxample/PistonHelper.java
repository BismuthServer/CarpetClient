package carpetclient.coders.EDDxample;


import carpetclient.Config;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.piston.PistonMoveStructureResolver;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * A class to help visualize piston update order. Code provided by EDDxample.
 */
public class PistonHelper {

    private static final String gold = "\u00a76", red = "\u00a74===", green = "\u00a72Blocks", pushe = "\u00a76Pushes", pull = "\u00a76Pull";

    public static boolean validState, activated, extending;
    public static BlockPos pistonPos;
    private static BlockPos[] tobreak, tomove;

    /**
     * Sets the piston update order by first removing the piston head.
     *
     * @param worldIn   current player world
     * @param state     block state of the piston being activated
     * @param pos       location of the piston being activated
     * @param extending extending or retracting piston
     */
    public static void setPistonMovement(World worldIn, BlockState state, BlockPos pos, boolean extending) {
        Direction enumfacing = (Direction) state.get(FacingBlock.FACING);
        BlockState state1 = worldIn.getBlockState(pos.offset(enumfacing));
        PistonMoveStructureResolver ph = null;

        //Weird trick to remove the piston head
        if (!extending) {
            worldIn.setBlockState(pos, Blocks.BARRIER.defaultState(), 2);
            worldIn.removeBlock(pos);
            worldIn.removeBlock(pos.offset(enumfacing));
        }

        ph = new PistonMoveStructureResolver(worldIn, pos, enumfacing, extending);
        boolean canMove = ph.resolve();
        int storeLimit = Config.pushLimit;
        Config.pushLimit = Integer.MAX_VALUE;
        ph.resolve();
        PistonHelper.set(pos, ph.getToMove().toArray(new BlockPos[ph.getToMove().size()]), ph.getToBreak().toArray(new BlockPos[ph.getToBreak().size()]), canMove, extending);
        Config.pushLimit = storeLimit;
        PistonHelper.activated = true;

        //Weird trick to add the piston head back
        if (!extending) {
            worldIn.setBlockState(pos, state, 2);
            worldIn.setBlockState(pos.offset(enumfacing), state1, 2);
        }
    }

    /**
     * Set logic of the piston data.
     *
     * @param posIn
     * @param btm
     * @param btb
     * @param isValid
     * @param _extending
     */
    private static void set(BlockPos posIn, BlockPos[] btm, BlockPos[] btb, boolean isValid, boolean _extending) {
        pistonPos = posIn;
        tomove = btm;
        tobreak = btb;
        validState = isValid;
        extending = _extending;
    }

    /**
     * Draws the piston update order and other details.
     *
     * @param partialTicks tick sense last render
     */
    public static void draw(float partialTicks) {
        if (Config.pistonVisualizer.getValue() && activated) {
            final LocalClientPlayerEntity player = Minecraft.getInstance().player;
            final double d0 = player.prevX + (player.x - player.prevX) * partialTicks;
            final double d1 = player.prevY + (player.y - player.prevY) * partialTicks;
            final double d2 = player.prevZ + (player.z - player.prevZ) * partialTicks;
            final EntityRenderDispatcher rm = Minecraft.getInstance().getEntityRenderDispatcher();
            BlockPos pos;

            int count = 0;
            for (int i = 1; i <= tobreak.length; i++) {
                pos = tobreak[tobreak.length - i];
                if (pos != null) {
                    count++;
                    GameRenderer.renderNameTag(Minecraft.getInstance().textRenderer, "\u00a7c" + count, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.5f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.cameraYaw, rm.cameraPitch, rm.options.perspective == 2, false);
                }

            }
            int moved = -count;
            for (int i = 1; i <= tomove.length; i++) {
                pos = tomove[tomove.length - i];
                if (pos != null) {
                    count++;
                    GameRenderer.renderNameTag(Minecraft.getInstance().textRenderer, Integer.toString(count), (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.5f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.cameraYaw, rm.cameraPitch, rm.options.perspective == 2, false);
                }
            }
            moved += count;
            pos = pistonPos;
            if (validState) {
                if (extending) {
                    GameRenderer.renderNameTag(Minecraft.getInstance().textRenderer, pushe, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.cameraYaw, rm.cameraPitch, rm.options.perspective == 2, false);
                } else {
                    GameRenderer.renderNameTag(Minecraft.getInstance().textRenderer, pull, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.cameraYaw, rm.cameraPitch, rm.options.perspective == 2, false);
                }
                GameRenderer.renderNameTag(Minecraft.getInstance().textRenderer, green, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.2f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.cameraYaw, rm.cameraPitch, rm.options.perspective == 2, false);
            } else {
                if (extending) {
                    GameRenderer.renderNameTag(Minecraft.getInstance().textRenderer, pushe, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.cameraYaw, rm.cameraPitch, rm.options.perspective == 2, false);
                } else {
                    GameRenderer.renderNameTag(Minecraft.getInstance().textRenderer, pull, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.cameraYaw, rm.cameraPitch, rm.options.perspective == 2, false);
                }
                GameRenderer.renderNameTag(Minecraft.getInstance().textRenderer, red, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.2f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.cameraYaw, rm.cameraPitch, rm.options.perspective == 2, false);
            }
            GameRenderer.renderNameTag(Minecraft.getInstance().textRenderer, gold + (moved < 0 ? 0 : moved), (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.5f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.cameraYaw, rm.cameraPitch, rm.options.perspective == 2, false);
        }
    }
}

