package dev.losterixx.ttyclient.mixin.general;

import dev.losterixx.ttyclient.client.MainClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(method = "createTitle", at = @At("TAIL"), cancellable = true)
    private void createTitle(CallbackInfoReturnable<String> info) {
        String mcVersion = FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("???");

        info.setReturnValue("TTYClient v" + MainClient.INSTANCE.getVERSION() + " | MC v" + mcVersion);
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void shutdown(CallbackInfo info) {
        MainClient.INSTANCE.onShutdown();
    }
}