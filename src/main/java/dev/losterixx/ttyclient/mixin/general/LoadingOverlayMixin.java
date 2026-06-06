package dev.losterixx.ttyclient.mixin.general;

import dev.losterixx.ttyclient.client.MainClient;
import dev.losterixx.ttyclient.client.ui.Draw;
import dev.losterixx.ttyclient.client.ui.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public class LoadingOverlayMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private ReloadInstance reload;
    @Shadow @Final private boolean fadeIn;

    @Shadow private float currentProgress;
    @Shadow private long fadeOutStart;
    @Shadow private long fadeInStart;

    @Unique
    private static final String[] LOGO = {
            " ______     ______     __  __    ",
            "/\\__  _\\   /\\__  _\\   /\\ \\_\\ \\   ",
            "  \\/_/\\ \\/   \\/_/\\ \\/   \\ \\____ \\  ",
            "   \\ \\_\\      \\ \\_\\    \\/\\_____\\ ",
            "    \\/_/       \\/_/     \\/_____/ "
    };

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void ttyDrawLoadingScreen(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (minecraft == null || minecraft.font == null || graphics == null || reload == null) {
            return;
        }

        try {
            graphics.guiWidth();
        } catch (Exception e) {
            return;
        }

        try {
            int width = graphics.guiWidth();
            int height = graphics.guiHeight();
            long now = Util.getMillis();

            if (fadeIn && fadeInStart == -1L) {
                fadeInStart = now;
            }

            float fadeOutAnim = (fadeOutStart > -1L) ? (now - fadeOutStart) / 1000.0f : -1.0f;
            float fadeInAnim = (fadeInStart  > -1L) ? (now - fadeInStart)  / 500.0f  : -1.0f;

            float logoAlpha;

            if (fadeOutAnim >= 1.0f) {
                if (minecraft.screen != null) {
                    minecraft.screen.extractRenderStateWithTooltipAndSubtitles(graphics, 0, 0, deltaTicks);
                } else {
                    minecraft.gui.extractDeferredSubtitles();
                }

                float t = 1.0f - clamp01(fadeOutAnim - 1.0f);
                int bgAlpha = Mth.ceil(t * 255.0f);

                graphics.nextStratum();
                graphics.fill(0, 0, width, height, withAlpha(Theme.INSTANCE.getBgPrimary(), bgAlpha));

                logoAlpha = t;
            } else {
                graphics.fill(0, 0, width, height, Theme.INSTANCE.getBgPrimary());
                logoAlpha = (fadeInAnim >= 0.0f) ? clamp01(fadeInAnim) : 1.0f;
            }

            int logoAlphaInt = clampAlpha(logoAlpha);
            int cx = width / 2;

            int fontH = Draw.INSTANCE.getFontHeight() - 2;
            int lineH = fontH + 2;
            int totalH = LOGO.length * lineH;
            int logoY = height / 3 - totalH / 2;

            int accentColor = withAlpha(Theme.INSTANCE.getAccent(), logoAlphaInt);
            int mutedColor  = withAlpha(Theme.INSTANCE.getTextMuted(), logoAlphaInt);

            for (int i = 0; i < LOGO.length; i++) {
                int lw = Draw.INSTANCE.textWidth(LOGO[i]);
                Draw.INSTANCE.text(graphics, LOGO[i], (cx - lw / 2) + 10, logoY + i * lineH, accentColor, false);
            }

            String ver = "v" + MainClient.INSTANCE.getVERSION();
            int vw = Draw.INSTANCE.textWidth(ver);
            Draw.INSTANCE.text(graphics, ver, cx - vw / 2, logoY + totalH + 6, mutedColor, false);

            currentProgress = clamp01(currentProgress * 0.95f + reload.getActualProgress() * 0.05f);

            if (fadeOutAnim < 2.0f) {
                float barAlpha01 = (fadeOutAnim >= 0.0f)
                        ? (1.0f - clamp01(fadeOutAnim))
                        : logoAlpha;
                int barAlpha = clampAlpha(barAlpha01);

                int barW = Math.min(width / 2, 320);
                int barX = cx - barW / 2;
                int barY = (int) (height * 0.8325);
                int barH = 4;
                int barRadius = 2;

                String pct = Math.round(currentProgress * 100) + "%";
                int pw = Draw.INSTANCE.textWidth(pct);
                Draw.INSTANCE.text(graphics, pct, cx - pw / 2, barY - fontH - 6,
                        withAlpha(Theme.INSTANCE.getTextMuted(), barAlpha), false);

                Draw.INSTANCE.roundedRect(graphics, barX, barY, barW, barH, barRadius,
                        withAlpha(Theme.INSTANCE.getBgSecondary(), barAlpha), Draw.CORNER_ALL);

                int filledW = Math.round(currentProgress * barW);
                if (filledW > 0) {
                    Draw.INSTANCE.roundedRect(graphics, barX, barY, filledW, barH, barRadius,
                            withAlpha(Theme.INSTANCE.getAccent(), barAlpha), Draw.CORNER_ALL);
                }
            }

            if (fadeOutAnim >= 2.0f) {
                minecraft.setOverlay(null);
            }

            ci.cancel();
        } catch (NullPointerException | IllegalStateException e) {
            System.err.println("[TTYClient] Failed to render custom loading screen, using vanilla. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Unique
    private static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
    }

    @Unique
    private static float clamp01(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    @Unique
    private static int clampAlpha(float v) {
        return Math.round(clamp01(v) * 255.0f);
    }
}





