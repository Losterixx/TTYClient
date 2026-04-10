package dev.losterixx.ttyclient.mixin.modules.freelook;

import dev.losterixx.ttyclient.client.modules.freelook.FreelookManager;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Camera.class)
public abstract class FreelookCameraMixin {

    @ModifyArg(
        method = "alignWithEntity",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"),
        index = 0
    )
    private float modifyYaw(float yaw) {
        if (FreelookManager.INSTANCE.isActive()) {
            return FreelookManager.INSTANCE.getFreelookYaw();
        }

        return yaw;
    }

    @ModifyArg(
        method = "alignWithEntity",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"),
        index = 1
    )
    private float modifyPitch(float pitch) {
        if (FreelookManager.INSTANCE.isActive()) {
            return FreelookManager.INSTANCE.getFreelookPitch();
        }

        return pitch;
    }

    @ModifyArg(
        method = "alignWithEntity",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F", ordinal = 0),
        index = 0
    )
    private float modifyClipDistance(float distance) {
        if (FreelookManager.INSTANCE.isActive()) {
            return FreelookManager.INSTANCE.getCameraDistance();
        }

        return distance;
    }
}