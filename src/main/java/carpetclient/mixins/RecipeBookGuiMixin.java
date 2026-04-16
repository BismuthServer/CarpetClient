package carpetclient.mixins;

import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.crafting.recipe.Recipe;
import net.minecraft.inventory.CraftingInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookGui.class)
public class RecipeBookGuiMixin {

    @Shadow  private TextFieldWidget searchField;

    @Shadow  private String prevSearch;

    @Shadow public boolean isGuiOpen(){return false;}

    @Shadow @Final private RecipeBookPage page;

    @Shadow private int width;

    @Shadow private int height;

    @Shadow private int offset;

    @Shadow private void updateCollections(boolean p_193003_1_) {}

    @Unique
	private static String memoText = "";

    @Inject(method = "initGui", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/recipebook/RecipeBookGui;searchField:Lnet/minecraft/client/gui/widget/TextFieldWidget;", ordinal = 4, shift = At.Shift.AFTER))
    private void setSearchText(boolean p_193014_1_, CraftingInventory p_193014_2_, CallbackInfo c) {
        if(!isGuiOpen()){
            memoText = "";
        }
        searchField.setText(memoText);
    }

    @Inject(method = "keyPressed", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/recipebook/RecipeBookGui;prevSearch:Ljava/lang/String;", ordinal = 1, shift = At.Shift.AFTER))
    private void setSearchText(char typedChar, int keycode, CallbackInfoReturnable<Boolean> c) {
        memoText = prevSearch;
    }

    @Inject(method = "mouseClicked", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/recipebook/RecipeBookGui;page:Lnet/minecraft/client/gui/recipebook/RecipeBookPage;", ordinal = 2, shift = At.Shift.AFTER))
    private void setSearchText(int p_191862_1_, int p_191862_2_, int p_191862_3_, CallbackInfoReturnable<Boolean> c) {
        if(p_191862_3_ == 2) {
            try {
                page.mouseClicked(p_191862_1_, p_191862_2_, 0, (this.width - 147) / 2 - this.offset, (this.height - 166) / 2, 147, 166);
                Recipe irecipe = page.getLastClickedRecipe();
                String s = irecipe.getResult().getHoverName();
                searchField.setText(s);
                prevSearch = s;
                memoText = s;
                this.updateCollections(true);
            }catch (Exception e){}
        }
    }
}
