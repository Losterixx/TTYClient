package dev.losterixx.ttyclient.mixin.modules.renderutils;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.losterixx.ttyclient.client.modules.renderutils.RenderUtilsManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class FireOverlayMixin {

    @Inject(method = "renderFire", at = @At("HEAD"))
    private static void onRenderFire(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            TextureAtlasSprite sprite,
            CallbackInfo ci) {

        var cfg = RenderUtilsManager.INSTANCE.getConfig();
        if (!cfg.getEnabled()) return;

        float offset = cfg.getFire().getHeightOffset();
        if (offset != 0.0f) {
            poseStack.translate(0.0f, offset, 0.0f);
        }
    }
}

