package carpetclient.coders.EDDxample;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.structure.StructureBox;
import net.ornithemc.osl.networking.api.PacketBuffer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/*
Code inspeiration from EDDxample.

A class to show bounding boxes of different structures on the server.
 */
public class ShowBoundingBoxes {
    private static Random randy = new Random();
    public static Minecraft mc = Minecraft.getInstance();

    public static final int OUTER_BOUNDING_BOX = 0;
    public static final int END_CITY = 1;
    public static final int FORTRESS = 2;
    public static final int TEMPLE = 3;
    public static final int VILLAGE = 4;
    public static final int STRONGHOLD = 5;
    public static final int MINESHAFT = 6;
    public static final int MONUMENT = 7;
    public static final int MANTION = 8;
    public static final int SLIME_CHUNKS = 9;

    public static boolean[] show = {
            true,
            true,
            true,
            true,
            false,
            false,
            false,
            false,
            false,
            false
    };

    private static ArrayList<StructureBox>[] group = new ArrayList[10];
    private static Color[] colors = {
            new Color(0xFFFF00), //0
            new Color(0xFF0000), //1
            new Color(0xFF0000), //2
            new Color(0x00FF00), //3
            new Color(0xFFFFFF), //4
            new Color(0xFFFFFF), //5
            new Color(0xFFFFFF), //6
            new Color(0x0000FF), //7
            new Color(0x00FF00), //8
            new Color(0x00FF00), //9
    };

    public static final int renderDist = 160;
    public static long seed = 0;
    public static int dimension = -2;
    private static int expectedStructureCount = 0;
    private static int structureCount = 0;

    static {
        for (int i = 0; i < group.length; i++) {
            group[i] = new ArrayList<StructureBox>();
        }
    }

    /**
     * Main render method to render the bounding boxes
     *
     * @param partialTicks
     */
    public static void RenderStructures(float partialTicks) {
        if (group == null) return;
        
        LocalClientPlayerEntity player = Minecraft.getInstance().player;
        final double d0 = player.prevX + (player.x - player.prevX) * partialTicks;
        final double d1 = player.prevY + (player.y - player.prevY) * partialTicks;
        final double d2 = player.prevZ + (player.z - player.prevZ) * partialTicks;

        RenderUtils.prepareOpenGL(true);

        if (show[SLIME_CHUNKS]) {
            ArrayList<StructureBox> array = group[SLIME_CHUNKS];
            if (array == null) return;
            for (StructureBox box : array) {
                if (insideRenderDistance(box, player)) {
                    RenderUtils.drawBox(d0, d1, d2, box.minX, box.minY, box.minZ, box.maxX + 1, box.maxY + 1, box.maxZ + 1, colors[SLIME_CHUNKS]);
                }
            }
        }

        if (player.dimension == dimension) {
            for (int i = 0; i < group.length; i++) {
                if (!show[i]) continue;
                ArrayList<StructureBox> array = group[i];
                if (array == null) return;
                for (StructureBox box : array) {
                    if (insideRenderDistance(box, player)) {
                        RenderUtils.drawBox(d0, d1, d2, box.minX, box.minY, box.minZ, box.maxX + 1, box.maxY + 1, box.maxZ + 1, colors[i]);
                    }
                }
            }
        }

        RenderUtils.prepareOpenGL(false);
    }

    /**
     * Calculates if the bounding box is within render distance to the player.
     *
     * @param box    The bounding box that is to be displayed.
     * @param player Relation to the player that the boxes should be displayed.
     * @return If within range returns true.
     */
    private static boolean insideRenderDistance(StructureBox box, LocalClientPlayerEntity player) {
        int minX = (int) player.x - renderDist;
        int maxX = (int) player.x + renderDist;

        if (box.maxX < minX || box.minX > maxX) {
            return false;
        }

        int minZ = (int) player.z - renderDist;
        int maxZ = (int) player.z + renderDist;

        if (box.maxZ < minZ || box.minZ > maxZ) {
            return false;
        }

        return true;
    }

    /**
     * The reciever for the data that is being sent from the server
     *
     * @param data Data from the server.
     */
    public static void getStructureComponent(PacketBuffer data) {
        NbtCompound nbt = null;
        try {
            nbt = data.readNbtCompound();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        World worldIn = Minecraft.getInstance().world;
        if (nbt != null && worldIn != null) {
            NbtList nbttaglist = nbt.getList("Boxes", 9);
            
            ArrayList<NbtCompound> allBoxes = new ArrayList<>();
            for (int i = 0; i < nbttaglist.size(); i++) {
                NbtList boxList = (NbtList) nbttaglist.getElement(i);
                for (int j = 0; j < boxList.size(); j++) {
                    allBoxes.add(boxList.getCompound(j));
                }
            }
            
            structureComponentInitialSettings(nbt, allBoxes.size());
            
            for (NbtCompound box : allBoxes)
                addStructure(box);
        }
    }
    
    public static void largeBoundingBoxStructuresStart(PacketBuffer data) {
        NbtCompound nbt = null;
        try {
            nbt = data.readNbtCompound();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        if (nbt != null && Minecraft.getInstance().world != null) {
            int expected = data.readVarInt();
            if (expected >= 100000)
                expected = 100000;
            structureComponentInitialSettings(nbt, expected);
        }
    }
    
    public static void largeBoundingBoxStructures(PacketBuffer data) {
        int count = data.readUnsignedByte() + 1;
        for (int i = 0; i < count; i++) {
            NbtCompound nbt = null;
            try {
                nbt = data.readNbtCompound();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            if (nbt != null && Minecraft.getInstance().world != null) {
                addStructure(nbt);
            }
        }
    }
    
    public static void structureComponentInitialSettings(NbtCompound nbt, int expectedStructureCount_) {
        structureCount = 0;
        expectedStructureCount = expectedStructureCount_;
        
        dimension = nbt.getInt("Dimention");
        seed = nbt.getLong("Seed");
        
        LocalClientPlayerEntity player = Minecraft.getInstance().player;
        int cnkX = (int) player.x / 16;
        int cnkZ = (int) player.z / 16;

        for (int ChunkX = cnkX - 20; ChunkX < cnkX + 20; ChunkX++) {
            for (int ChunkZ = cnkZ - 20; ChunkZ < cnkZ + 20; ChunkZ++) {
                if (mc.world.dimension.getType() == DimensionType.OVERWORLD && isSlimeChunk(ChunkX, ChunkZ, seed)) {
                    StructureBox boundingBox = new StructureBox(ChunkX << 4, 0, ChunkZ << 4, (ChunkX << 4) + 16, 40, (ChunkZ << 4) + 16);
                    group[SLIME_CHUNKS].add(boundingBox);
                }
            }
        }
    }
    
    public static void addStructure(NbtCompound compound) {
        if (structureCount >= expectedStructureCount)
            return;
        
        int type = compound.getInt("type");
        StructureBox boundingBox = new StructureBox(compound.getIntArray("bb"));
        group[type].add(boundingBox);
        
        structureCount++;
    }

    private static boolean isSlimeChunk(int x, int z, long seed) {
        randy.setSeed(seed + (long)(x * x * 4987142) + (long)(x * 5947611) + (long)(z * z) * 4392871L + (long)(z * 389711) ^ 987234911L);
        return randy.nextInt(10) == 0;
    }
    
    public static void clear(){
        for (ArrayList<StructureBox> l : group)
            l.clear();
        structureCount = 0;
        expectedStructureCount = 0;
    }
}
