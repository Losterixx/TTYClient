package dev.losterixx.ttyclient.mixin.modules.performance;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.losterixx.ttyclient.client.modules.performance.PerformanceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public class HudGuiRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void ttyRenderHead(GpuBufferSlice indexBuffer, CallbackInfo ci) {
        PerformanceManager mgr = PerformanceManager.INSTANCE;
        if (!mgr.getConfig().getHudThrottleEnabled()) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.screen != null) {
            mgr.forceUpdate();
            return;
        }

        RenderTarget mainTarget = mc.getMainRenderTarget();
        RenderTarget captureTarget = mgr.ensureHudCaptureTarget(mainTarget.width, mainTarget.height);

        if (mgr.isThrottleActive()) {
            captureTarget.blitAndBlendToTexture(mainTarget.getColorTextureView());
            ci.cancel();
        } else {
            RenderSystem.getDevice()
                    .createCommandEncoder()
                    .clearColorAndDepthTextures(
                            captureTarget.getColorTexture(),
                            0x00000000,
                            captureTarget.getDepthTexture(),
                            1.0
                    );
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void ttyRenderTail(GpuBufferSlice indexBuffer, CallbackInfo ci) {
        PerformanceManager mgr = PerformanceManager.INSTANCE;
        if (!mgr.getConfig().getHudThrottleEnabled() || mgr.isThrottleActive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.screen != null) return;

        RenderTarget captureTarget = mgr.getHudCaptureTargetOrNull();
        if (captureTarget == null) return;

        captureTarget.blitAndBlendToTexture(mc.getMainRenderTarget().getColorTextureView());
    }

    @Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget ttyRedirectDrawTarget(Minecraft mc) {
        PerformanceManager mgr = PerformanceManager.INSTANCE;

        if (mgr.getConfig().getHudThrottleEnabled() && !mgr.isThrottleActive() && mc.level != null && mc.screen == null) {
            RenderTarget captureTarget = mgr.getHudCaptureTargetOrNull();
            if (captureTarget != null) return captureTarget;
        }

        return mc.getMainRenderTarget();
    }
}
