package dev.losterixx.ttyclient.mixin.modules.hud;

import dev.losterixx.ttyclient.client.modules.hud.HudManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class HudRenderMixin {

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void onRenderHud(GuiGraphicsExtractor context, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        if (!mc.options.hideGui) {
            HudManager.INSTANCE.renderHud(context);
        }
    }
}
