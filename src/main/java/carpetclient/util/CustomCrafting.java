package carpetclient.util;

import carpetclient.mixinInterface.AMixinRegistryNamespaced;
import carpetclient.mixinInterface.AMixinSearchTree;
import carpetclient.mixins.ICraftingManager;
import carpetclient.mixins.IClientRecipeBook;
import carpetclient.pluginchannel.CarpetPluginChannel;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeCollection;
import net.minecraft.client.crafting.ClientRecipeBook;
import net.minecraft.client.search.ReloadableIdSearchTree;
import net.minecraft.client.search.SearchRegistry;
import net.minecraft.client.search.SearchTree;
import net.minecraft.crafting.CraftingManager;
import net.minecraft.crafting.recipe.Recipe;
import net.minecraft.crafting.recipe.ShapedRecipe;
import net.minecraft.crafting.recipe.ShapelessRecipe;
import net.minecraft.item.CreativeModeTab;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.JsonUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Recipe bridge class for Carpet servers synching custom recipes with carpet servers.
 */
public class CustomCrafting {

    /**
     * Main custom recipe method created to recieve custom recipes from carpet servers.
     *
     * @param data
     */
    public static void addCustomRecipes(PacketByteBuf data) {
        NbtCompound nbt;
        try {
            nbt = data.readNbtCompound();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        resetCraftingRecipes();

        NbtList nbttaglist = nbt.getList("recipeList", 10);
        for (int i = 0; i < nbttaglist.size(); i++) {
            NbtCompound ruleNBT = (NbtCompound) nbttaglist.getElement(i);

            String name = ruleNBT.getString("name");
            String recipe = ruleNBT.getString("recipe");
            JsonObject json = (new JsonParser()).parse(recipe).getAsJsonObject();
            try {
                Recipe recipeparsed = parseRecipeJson(json);

                try {
                    CraftingManager.register(name, recipeparsed);
                } catch (IllegalAccessError e1) {
                    // Forge, you know, some things, are better done at run time.
                    Class<?> NamespacedWrapper_class = CraftingManager.REGISTRY.getClass();

                    Field NamespacedWrapper_locked = NamespacedWrapper_class.getDeclaredField("locked");
                    NamespacedWrapper_locked.setAccessible(true);
                    NamespacedWrapper_locked.set(CraftingManager.REGISTRY, false);

                    Field NamespacedWrapper_delegate = NamespacedWrapper_class.getDeclaredField("delegate");
                    NamespacedWrapper_delegate.setAccessible(true);
                    Object ForgeRegistry_inst = NamespacedWrapper_delegate.get(CraftingManager.REGISTRY);

                    Field ForgeRegistry_isFrozen = ForgeRegistry_inst.getClass().getDeclaredField("isFrozen");
                    ForgeRegistry_isFrozen.setAccessible(true);
                    ForgeRegistry_isFrozen.set(ForgeRegistry_inst, false);

                    Method CraftingManager_register = CraftingManager.class.getDeclaredMethod("func_193379_a", String.class, Recipe.class);
                    CraftingManager_register.setAccessible(true);
                    CraftingManager_register.invoke(null, name, recipeparsed);
                }
            } catch (Exception e) {
                System.out.println("something went wrong");
                e.printStackTrace();
                return;
            }
        }
        resetRecipeBook();
        sendConfirmationPacketThatUpdatesCanBeReceived();
    }

    /**
     * Confirmation method that recipes where recieved requesting an update from the server.
     * Packets can't be sent at the same time or they will create issues in the packet reader, this system is in place to create artificial delay.
     */
    private static void sendConfirmationPacketThatUpdatesCanBeReceived() {
        PacketByteBuf sender = new PacketByteBuf(Unpooled.buffer());
        sender.writeInt(CarpetPluginChannel.CUSTOM_RECIPES);
        CarpetPluginChannel.packatSender(sender);
    }

    /**
     * Resets recipe book to vanilla as it might be cluttered with custom recipes.
     */
    public static void resetCraftingRecipes() {
        ((AMixinRegistryNamespaced) CraftingManager.REGISTRY).carpetClient$clear();
        ICraftingManager.setNextId(0);
        if (!CraftingManager.init()) {
            return;
        }
        resetRecipeBook();
    }

    /**
     * Private reset method for reseting to vanilla recipes.
     */
    private static void resetRecipeBook() {
        ClientRecipeBook.COLLECTIONS.clear();
        ClientRecipeBook.COLLECTIONS_BY_TAB.clear();

        Table<CreativeModeTab, String, RecipeCollection> table = HashBasedTable.<CreativeModeTab, String, RecipeCollection>create();

        for (Recipe irecipe : (Iterable<Recipe>) CraftingManager.REGISTRY) {
            if (!irecipe.isSpecial()) {
                CreativeModeTab creativetabs = IClientRecipeBook.callGetTab(irecipe.getResult());
                String s = irecipe.getGroup();
                RecipeCollection recipelist1;

                if (s.isEmpty()) {
                    recipelist1 = IClientRecipeBook.callSetupTab(creativetabs);
                } else {
                    recipelist1 = table.get(creativetabs, s);

                    if (recipelist1 == null) {
                        recipelist1 = IClientRecipeBook.callSetupTab(creativetabs);
                        table.put(creativetabs, s, recipelist1);
                    }
                }

                recipelist1.add(irecipe);
            }
        }

        SearchTree<RecipeCollection> searchTree = Minecraft.getInstance().getSearchTree(SearchRegistry.RECIPE_COLLECTIONS);
        if (searchTree instanceof ReloadableIdSearchTree<?>) {
            ReloadableIdSearchTree<RecipeCollection> searchTreeImpl = (ReloadableIdSearchTree<RecipeCollection>) searchTree;

            ((AMixinSearchTree) searchTreeImpl).clear();
            ClientRecipeBook.COLLECTIONS.forEach(searchTreeImpl::add);
            searchTreeImpl.reload();
        }
    }

    /**
     * Helper method grabed from recipe parser.
     *
     * @param json
     * @return
     */
    private static Recipe parseRecipeJson(JsonObject json) {
        String s = JsonUtils.getString(json, "type");

        if ("crafting_shaped".equals(s)) {
            return ShapedRecipe.fromJson(json);
        } else if ("crafting_shapeless".equals(s)) {
            return ShapelessRecipe.fromJson(json);
        } else {
            throw new JsonSyntaxException("Invalid or unsupported recipe type '" + s + "'");
        }
    }
}
