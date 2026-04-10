package dev.losterixx.ttyclient.mixin.general;

import dev.losterixx.ttyclient.client.screens.TTYClientTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    public void overrideMainMenu(CallbackInfo info) {
        Minecraft.getInstance().setScreen(new TTYClientTitleScreen());
        info.cancel();
    }

}