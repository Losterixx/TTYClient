package dev.losterixx.ttyclient.mixin.modules.autoreconnect;

import dev.losterixx.ttyclient.client.modules.autoreconnect.ReconnectManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {

    @Shadow @Final private Screen parent;

    @Unique
    private Button ttyReconnectButton;

    protected DisconnectedScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (!ReconnectManager.INSTANCE.getConfig().getEnabled()) return;
        if (ReconnectManager.INSTANCE.getLastServerData() == null) return;

        int delay = ReconnectManager.INSTANCE.getConfig().getDelaySeconds();

        ttyReconnectButton = Button.builder(
                Component.literal("Reconnect (" + delay + "s)"),
                b -> ReconnectManager.INSTANCE.reconnect(parent)
        ).bounds(this.width / 2 - 60, this.height - 30, 120, 20).build();

        this.addRenderableWidget(ttyReconnectButton);
        ReconnectManager.INSTANCE.setCurrentReconnectButton(ttyReconnectButton);
    }
}

