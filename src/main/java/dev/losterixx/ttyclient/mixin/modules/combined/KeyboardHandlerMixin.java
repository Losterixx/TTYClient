package dev.losterixx.ttyclient.mixin.modules.combined;

import dev.losterixx.ttyclient.client.modules.debug.KeyVisualizerManager;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void onKeyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (action == 1) {
            KeyVisualizerManager.INSTANCE.onKeyPress(event.key(), event.scancode(), event.modifiers());
        }
    }
}


