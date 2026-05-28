package dev.losterixx.ttyclient.mixin.modules.performance;

import dev.losterixx.ttyclient.client.modules.performance.PerformanceManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(
        method = "extractGui(Lnet/minecraft/client/DeltaTracker;ZZ)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void ttyThrottleExtractGui(DeltaTracker deltaTracker, boolean renderLevel, boolean hasScreen, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.screen != null) {
            PerformanceManager.INSTANCE.forceUpdate();
            return;
        }

        if (PerformanceManager.INSTANCE.shouldSkip()) {
            ci.cancel();
        }
    }
}

