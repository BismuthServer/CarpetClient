package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(PickaxeItem.class)
public class PickaxeItemMixin extends ToolItem {

    protected PickaxeItemMixin(float attackDamageIn, float attackSpeedIn, Tier materialIn, Set<Block> effectiveBlocksIn) {
        super(attackDamageIn, attackSpeedIn, materialIn, effectiveBlocksIn);
    }

    @Inject(method = "getMiningSpeed", at = @At("HEAD"), cancellable = true)
    public void canPlaceOnOver(ItemStack stack, BlockState state, CallbackInfoReturnable<Float> cir) {
        Material material = state.getMaterial();
        cir.setReturnValue(material != Material.IRON && material != Material.ANVIL && material != Material.STONE && (!Config.missingTools || (material != Material.PISTON && material != Material.GLASS)) ? super.getMiningSpeed(stack, state) : this.miningSpeed);
    }
}
