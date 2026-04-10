package dev.losterixx.ttyclient.mixin.modules.zoom;

import dev.losterixx.ttyclient.client.modules.zoom.ZoomManager;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class ZoomCameraMixin {

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void applyZoomFov(float partialTick, CallbackInfoReturnable<Float> cir) {
        ZoomManager.INSTANCE.updateZoomAnimation();

        if (ZoomManager.INSTANCE.isZooming()) {
            cir.setReturnValue(cir.getReturnValue() * ZoomManager.INSTANCE.getFOVMultiplier());
        }
    }
}

