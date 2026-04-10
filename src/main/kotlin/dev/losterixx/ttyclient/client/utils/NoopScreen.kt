package dev.losterixx.ttyclient.client.utils

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class NoopScreen : Screen(Component.empty()) {

    override fun init() {
        onClose()
    }

    override fun extractRenderState(ctx: GuiGraphicsExtractor, mx: Int, my: Int, delta: Float) {}
}

