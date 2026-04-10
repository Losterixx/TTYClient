package dev.losterixx.ttyclient.mixin.modules.autoreconnect;

import dev.losterixx.ttyclient.client.modules.autoreconnect.ReconnectManager;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin {

    @Inject(method = "join", at = @At("HEAD"))
    private void onJoin(ServerData serverData, CallbackInfo ci) {
        ReconnectManager.INSTANCE.setLastServerData(serverData);
    }
}

