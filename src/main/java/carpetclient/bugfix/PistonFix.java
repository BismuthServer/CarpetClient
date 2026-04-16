package carpetclient.bugfix;

import carpetclient.Config;
import carpetclient.mixins.IWorld;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MovingBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Tickable;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.ornithemc.osl.networking.api.PacketBuffer;

import java.util.Iterator;

/*
Class used to fix players glitching through moving piston blocks.
 */
public class PistonFix {
    private static PistonFix instance;
    private static boolean pushPlayersNow;
    private static boolean pistonFix;
    private static boolean firstPistonPush;

    static {
        instance = new PistonFix();
    }

    /**
     * Process the packet received from the server. Used to synch packets form the server ticks relative to the client ticks.
     *
     * @param data
     */
    public static void processPacket(PacketBuffer data) {
        if (!Config.clipThroughPistons.getValue()) return;

        if (pistonFix) {
            instance.fixTileEntitys();
        }
        pistonFix = true;
    }

    /**
     * Updates player being moved to simulate regular game logic where players move before tile entitys.
     */
    public static void movePlayer() {
        if (pushPlayersNow && firstPistonPush) {
            instance.move();
            firstPistonPush = false;
        }
    }

    /**
     * Resets booleans used in packet synching.
     */
    public static void resetBools() {
        firstPistonPush = true;
        pistonFix = false;
    }

    /**
     * Simulates moving the player
     */
    private void move() {
        Minecraft.getInstance().player.tick();
    }

    /**
     * Simulates tile entity's and fast updates them. As many packets can arrive on the client at the same time desynched
     * tick wise relative to the server the only way to fix it is to simulate tile entity's moving within synching packet's.
     */
    private void fixTileEntitys() {
        World world = Minecraft.getInstance().world;
        Iterator<BlockEntity> iterator = world.tickingBlockEntities.iterator();
        pushPlayersNow = true;

        while (iterator.hasNext()) {
            BlockEntity tileentity = iterator.next();

            if (!(tileentity instanceof MovingBlockEntity)) continue;

            if (!tileentity.isRemoved() && tileentity.hasWorld()) {
                BlockPos blockpos = tileentity.getPos();

                if (world.isChunkLoaded(blockpos) && ((IWorld) world).getWorldBorder().contains(blockpos)) {
                    try {
                        ((Tickable) tileentity).tick();
                    } catch (Throwable throwable) {
                        CrashReport crashreport2 = CrashReport.of(throwable, "Ticking block entity");
                        CrashReportCategory crashreportcategory2 = crashreport2.addCategory("Block entity being ticked");
                        tileentity.populateCrashReport(crashreportcategory2);
                        throw new CrashException(crashreport2);
                    }
                }
            }

            if (tileentity.isRemoved()) {
                iterator.remove();
                world.blockEntities.remove(tileentity);

                if (world.isChunkLoaded(tileentity.getPos())) {
                    world.getChunk(tileentity.getPos()).removeBlockEntity(tileentity.getPos());
                }
            }
        }

        pushPlayersNow = false;
    }
}
