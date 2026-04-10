package dev.losterixx.ttyclient.mixin.general;

import dev.losterixx.ttyclient.client.MainClient;
import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientBrandRetriever.class)
public class ClientBrandRetrieverMixin {

    @Inject(at = @At("HEAD"), method = "getClientModName", cancellable = true, remap = false)
    private static void rebrand$getConfiguredClientBrand(CallbackInfoReturnable<String> info) {
        info.setReturnValue("TTYClient (v" + MainClient.INSTANCE.getVERSION() + ")");
    }

}