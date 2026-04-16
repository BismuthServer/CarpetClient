package carpetclient.util;

import carpetclient.Config;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.*;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.property.IntegerProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class RedstoneHelper {

    public static void blockClicked(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, InteractionHand hand, Direction facing) {
        System.out.println("Test power at: " + new RedstoneHelper().isWirePowered(worldIn, pos));
    }

    private void calcWireOrder() {
//        if (blockPowered()) {
//            calcDown();
//            calcUp();
//        } else {
//            calcUp();
//            calcDown();
//        }
    }

    private void calcUp(World worldIn, BlockPos pos) {

    }

    private void calcDown() {
    }

    private boolean blockPowered(BlockState state) {
        if (state.getBlock().getMetadataFromState(state) != 0) {
            return true;
        }
        return false;
    }

    private boolean isWirePowered(WorldView worldIn, BlockPos pos) {
        for (Direction enumfacing : Direction.values()) {
            if (isPowerSourceAt(worldIn, pos, enumfacing)) return true;
        }

        return false;
    }

    private boolean isPowerSourceAt(WorldView worldIn, BlockPos pos, Direction side) {
        BlockPos blockpos = pos.offset(side);
        BlockState iblockstate = worldIn.getBlockState(blockpos);
        boolean normalCube = iblockstate.isSolid();

        if (canConnectTo(iblockstate, side, false)) {
            return true;
        } else if (normalCube) {
            for (Direction enumfacing : Direction.values()) {
                if (!enumfacing.equals(side.getOpposite()) && canConnectTo(worldIn.getBlockState(blockpos.offset(enumfacing)), enumfacing, normalCube)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected static boolean canConnectTo(BlockState blockState, @Nullable Direction side, boolean normalCube) {
        Block block = blockState.getBlock();

        if (block == Blocks.REDSTONE_WIRE) {
            return false;
        } else if (Blocks.REPEATER.isSameDiode(blockState)) {
            Direction enumfacing = (Direction) blockState.get(RepeaterBlock.FACING);
            return enumfacing == side;
        } else if (Blocks.COMPARATOR.isSameDiode(blockState)) {
            Direction enumfacing = (Direction) blockState.get(RepeaterBlock.FACING);
            return enumfacing == side;
        } else if (Blocks.OBSERVER == blockState.getBlock()) {
            return side == blockState.get(ObserverBlock.FACING);
        } else {
            return !normalCube && blockState.isSignalSource();
        }
    }

    public static void draw(float partialTicks) {
        if (Config.pistonVisualizer.getValue()) {
            final LocalClientPlayerEntity player = Minecraft.getInstance().player;
            final double d0 = player.prevX + (player.x - player.prevX) * partialTicks;
            final double d1 = player.prevY + (player.y - player.prevY) * partialTicks;
            final double d2 = player.prevZ + (player.z - player.prevZ) * partialTicks;
            final EntityRenderDispatcher rm = Minecraft.getInstance().getEntityRenderDispatcher();
            BlockPos pos;

//            int count = 0;
//            for (int i = 1; i <= tobreak.length; i++) {
//                pos = tobreak[tobreak.length - i];
//                if (pos != null) {
//                    count++;
//                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, "\u00a7c" + count, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.5f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//                }
//
//            }
//            int moved = -count;
//            for (int i = 1; i <= tomove.length; i++) {
//                pos = tomove[tomove.length - i];
//                if (pos != null) {
//                    count++;
//                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, Integer.toString(count), (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.5f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//                }
//            }
//            moved += count;
//            pos = pistonPos;
//            if (validState) {
//                if (extending) {
//                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, pushe, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//                } else {
//                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, pull, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//                }
//                EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, green, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.2f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//            } else {
//                if (extending) {
//                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, pushe, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//                } else {
//                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, pull, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//                }
//                EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, red, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.2f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//            }
//            EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, gold + (moved < 0 ? 0 : moved), (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.5f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
        }
    }

    ////////////////// vanilla code //////////////////////

    HashMap<BlockPos, BlockState> blocks = new HashMap<>();

    public class SimulatedRedstoneWire extends Block {

        private final Set<BlockPos> blocksNeedingUpdate = Sets.<BlockPos>newHashSet();
        public final IntegerProperty POWER = RedstoneWireBlock.POWER;
        private boolean canProvidePower = true;

        public SimulatedRedstoneWire(Material blockMaterialIn, MapColor blockMapColorIn) {
            super(blockMaterialIn, blockMapColorIn);
        }

        private BlockState getBlockState(BlockPos pos) {
            return blocks.get(pos);
        }

        public void setBlockState(BlockPos pos, BlockState newState) {
            if (this.isOutsideBuildHeight(pos)) {
                return;
            } else {
                blocks.put(pos, newState);
                updateObservingBlocksAt(pos, newState.getBlock());
            }
        }

        public void updateObservingBlocksAt(BlockPos pos, Block blockType) {
            observedNeighborChanged(pos.west(), blockType, pos);
            observedNeighborChanged(pos.east(), blockType, pos);
            observedNeighborChanged(pos.down(), blockType, pos);
            observedNeighborChanged(pos.up(), blockType, pos);
            observedNeighborChanged(pos.north(), blockType, pos);
            observedNeighborChanged(pos.south(), blockType, pos);
        }

        private void observedNeighborChanged(BlockPos west, Block blockType, BlockPos pos) {
            System.out.println("observer gets updated here");
        }

        public void neighborChanged(BlockState state, BlockPos pos, Block blockIn, BlockPos fromPos) {
            if (this.canPlaceBlockAt(pos)) {
                this.updateSurroundingRedstone(pos, state);
            } else {
//                this.dropBlockAsItem(worldIn, pos, state, 0);
//                worldIn.setBlockToAir(pos);
            }
        }

        public boolean canPlaceBlockAt(BlockPos pos) {
            return getBlockState(pos.down()).isFullBlock() || getBlockState(pos.down()).getBlock() == Blocks.GLOWSTONE;
        }

        private BlockState updateSurroundingRedstone(BlockPos pos, BlockState state) {
            state = this.calculateCurrentChanges(pos, pos, state);
            List<BlockPos> list = Lists.newArrayList(this.blocksNeedingUpdate);
            this.blocksNeedingUpdate.clear();

            for (BlockPos blockpos : list) {
                notifyNeighborsOfStateChange(blockpos, this);
            }

            return state;
        }

        public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType) {
            neighborChanged2(pos.west(), blockType, pos);
            neighborChanged2(pos.east(), blockType, pos);
            neighborChanged2(pos.down(), blockType, pos);
            neighborChanged2(pos.up(), blockType, pos);
            neighborChanged2(pos.north(), blockType, pos);
            neighborChanged2(pos.south(), blockType, pos);
        }

        private void neighborChanged2(BlockPos newPos, Block blockType, BlockPos fromPos) {
            neighborChanged(getBlockState(newPos), newPos, blockType, fromPos);
        }

        public boolean isBlockPowered(BlockPos pos) {
            if (this.getRedstonePower(pos.down(), Direction.DOWN) > 0) {
                return true;
            } else if (this.getRedstonePower(pos.up(), Direction.UP) > 0) {
                return true;
            } else if (this.getRedstonePower(pos.north(), Direction.NORTH) > 0) {
                return true;
            } else if (this.getRedstonePower(pos.south(), Direction.SOUTH) > 0) {
                return true;
            } else if (this.getRedstonePower(pos.west(), Direction.WEST) > 0) {
                return true;
            } else {
                return this.getRedstonePower(pos.east(), Direction.EAST) > 0;
            }
        }

        public int getRedstonePower(BlockPos pos, Direction facing) {
            BlockState iblockstate = this.getBlockState(pos);
//            if(iblockstate.getBlock() instanceof BlockRedstoneWire) return 0;
            return iblockstate.isSolid() ? getStrongPower(pos) : iblockstate.getBlock().getSignal(iblockstate, null, pos, facing);
        }

        public int getStrongPower(BlockPos pos, Direction facing)
        {
            return getBlockState(pos).getBlock().getDirectSignal(getBlockState(pos), null, pos, facing);
        }

        public int getStrongPower(BlockPos pos)
        {
            int i = 0;
            i = Math.max(i, this.getStrongPower(pos.down(), Direction.DOWN));

            if (i >= 15)
            {
                return i;
            }
            else
            {
                i = Math.max(i, this.getStrongPower(pos.up(), Direction.UP));

                if (i >= 15)
                {
                    return i;
                }
                else
                {
                    i = Math.max(i, this.getStrongPower(pos.north(), Direction.NORTH));

                    if (i >= 15)
                    {
                        return i;
                    }
                    else
                    {
                        i = Math.max(i, this.getStrongPower(pos.south(), Direction.SOUTH));

                        if (i >= 15)
                        {
                            return i;
                        }
                        else
                        {
                            i = Math.max(i, this.getStrongPower(pos.west(), Direction.WEST));

                            if (i >= 15)
                            {
                                return i;
                            }
                            else
                            {
                                i = Math.max(i, this.getStrongPower(pos.east(), Direction.EAST));
                                return i >= 15 ? i : i;
                            }
                        }
                    }
                }
            }
        }

        public int isBlockIndirectlyGettingPowered(BlockPos pos) {
            int i = 0;
            Direction[] var3 = Direction.values();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Direction enumfacing = var3[var5];
                int j = this.getRedstonePower(pos.offset(enumfacing), enumfacing);
                if (j >= 15) {
                    return 15;
                }

                if (j > i) {
                    i = j;
                }
            }

            return i;
        }

        private BlockState calculateCurrentChanges(BlockPos pos1, BlockPos pos2, BlockState state) {
            BlockState iblockstate = state;
            int i = ((Integer) state.get(POWER)).intValue();
            int j = 0;
            j = this.getMaxCurrentStrength(pos2, j);
            this.canProvidePower = false;
            int k = isBlockIndirectlyGettingPowered(pos1);
            this.canProvidePower = true;

            if (k > 0 && k > j - 1) {
                j = k;
            }

            int l = 0;

            for (Direction enumfacing : Direction.Plane.HORIZONTAL) {
                BlockPos blockpos = pos1.offset(enumfacing);
                boolean flag = blockpos.getX() != pos2.getX() || blockpos.getZ() != pos2.getZ();

                if (flag) {
                    l = this.getMaxCurrentStrength(blockpos, l);
                }

                if (getBlockState(blockpos).isSolid() && !getBlockState(pos1.up()).isSolid()) {
                    if (flag && pos1.getY() >= pos2.getY()) {
                        l = this.getMaxCurrentStrength(blockpos.up(), l);
                    }
                } else if (!getBlockState(blockpos).isSolid() && flag && pos1.getY() <= pos2.getY()) {
                    l = this.getMaxCurrentStrength(blockpos.down(), l);
                }
            }

            if (l > j) {
                j = l - 1;
            } else if (j > 0) {
                --j;
            } else {
                j = 0;
            }

            if (k > j - 1) {
                j = k;
            }

            if (i != j) {
                state = state.set(POWER, Integer.valueOf(j));

                if (getBlockState(pos1) == iblockstate) {
                    setBlockState(pos1, state);
                }

                this.blocksNeedingUpdate.add(pos1);

                for (Direction enumfacing1 : Direction.values()) {
                    this.blocksNeedingUpdate.add(pos1.offset(enumfacing1));
                }
            }

            return state;
        }

        private int getMaxCurrentStrength(BlockPos pos, int strength) {
            if (getBlockState(pos).getBlock() != this) {
                return strength;
            } else {
                int i = ((Integer) getBlockState(pos).get(POWER)).intValue();
                return i > strength ? i : strength;
            }
        }

        private boolean isOutsideBuildHeight(BlockPos pos) {
            return pos.getY() < 0 || pos.getY() >= 256;
        }
    }
}
