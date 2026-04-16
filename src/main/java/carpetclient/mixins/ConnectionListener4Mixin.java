package carpetclient.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/*
Mixing override to remove client side timeouts.
 */
@Mixin(targets = "net.minecraft.server.network.ConnectionListener$40061135")
public class ConnectionListener4Mixin {
	@ModifyConstant(method = "initChannel", constant = @Constant(intValue = 30), remap = false)
	private int noTimeout(int value) {
		return 0;
	}
}
