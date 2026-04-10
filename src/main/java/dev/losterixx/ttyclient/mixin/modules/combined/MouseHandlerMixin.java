package dev.losterixx.ttyclient.mixin.modules.combined;

import dev.losterixx.ttyclient.client.modules.freelook.FreelookManager;
import dev.losterixx.ttyclient.client.modules.zoom.ZoomManager;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (FreelookManager.INSTANCE.handleScroll(vertical)) {
            ci.cancel();
            return;
        }

        if (ZoomManager.INSTANCE.handleScroll(vertical)) {
            ci.cancel();
        }
    }

    @Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void redirectTurn(LocalPlayer player, double deltaX, double deltaY) {
        if (FreelookManager.INSTANCE.isActive()) {
            FreelookManager.INSTANCE.updateFreelookAngles((float) deltaX, (float) deltaY);
            return;
        }

        if (ZoomManager.INSTANCE.isZooming()) {
            double multiplier = ZoomManager.INSTANCE.getFOVMultiplier();
            player.turn(deltaX * multiplier, deltaY * multiplier);
            return;
        }

        player.turn(deltaX, deltaY);
    }
}

