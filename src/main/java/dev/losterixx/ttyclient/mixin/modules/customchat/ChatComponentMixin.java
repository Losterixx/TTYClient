package dev.losterixx.ttyclient.mixin.modules.customchat;

import dev.losterixx.ttyclient.client.modules.customchat.ChatManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @Shadow
    public abstract boolean isChatFocused();

    @Inject(
        method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onExtractRenderState(
            GuiGraphicsExtractor extractor, Font font,
            int p1, int p2, int p3,
            ChatComponent.DisplayMode mode, boolean focused,
            CallbackInfo ci
    ) {
        if (!ChatManager.INSTANCE.getConfig().getEnabled()) return;

        if (!ChatManager.INSTANCE.isCustomChatVisible()) {
            if (isChatFocused()) {
                extractor.text(font, Component.literal("Chat is currently hidden. [ ;module --trigger customchat ]"), 5, 5, 0xFFE38888);
            }

            ci.cancel();
        }
    }

    @Inject(method = "getWidth()I", at = @At("HEAD"), cancellable = true)
    private void onGetWidth(CallbackInfoReturnable<Integer> cir) {
        if (ChatManager.INSTANCE.getConfig().getEnabled()) {
            cir.setReturnValue(ChatManager.INSTANCE.getChatWidth());
        }
    }

    @Inject(method = "getHeight()I", at = @At("HEAD"), cancellable = true)
    private void onGetHeight(CallbackInfoReturnable<Integer> cir) {
        if (ChatManager.INSTANCE.getConfig().getEnabled()) {
            cir.setReturnValue(ChatManager.INSTANCE.getChatHeight(isChatFocused()));
        }
    }

    @Inject(method = "clearMessages", at = @At("HEAD"), cancellable = true)
    private void onClearMessages(boolean clearHistory, CallbackInfo ci) {
        if (clearHistory && ChatManager.INSTANCE.getConfig().getKeepChatHistory()) {
            ci.cancel();
        }
    }

    @ModifyConstant(method = "addMessageToDisplayQueue", constant = @Constant(intValue = 100))
    private int modifyMaxVisibleLines(int original) {
        return ChatManager.INSTANCE.getConfig().getInfiniteChatHistory() ? 999999 : original;
    }

    @ModifyConstant(method = "addMessageToQueue", constant = @Constant(intValue = 100))
    private int modifyMaxStoredMessages(int original) {
        return ChatManager.INSTANCE.getConfig().getInfiniteChatHistory() ? 999999 : original;
    }
}

