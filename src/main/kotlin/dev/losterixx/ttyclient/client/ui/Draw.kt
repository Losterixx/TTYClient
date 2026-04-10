package dev.losterixx.ttyclient.client.ui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor

object Draw {
    private val mc get() = Minecraft.getInstance()

    fun rect(ctx: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int, color: Int) {
        if (w <= 0 || h <= 0) return
        ctx.fill(x, y, x + w, y + h, color)
    }

    fun rectOutline(ctx: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int, color: Int, t: Int = 1) {
        rect(ctx, x, y, w, t, color)
        rect(ctx, x, y + h - t, w, t, color)
        rect(ctx, x, y + t, t, h - 2 * t, color)
        rect(ctx, x + w - t, y + t, t, h - 2 * t, color)
    }

    fun text(ctx: GuiGraphicsExtractor, text: String, x: Int, y: Int, color: Int, shadow: Boolean = false) {
        ctx.text(mc.font, text, x, y, color, shadow)
    }

    fun textCentered(ctx: GuiGraphicsExtractor, text: String, x: Int, y: Int, w: Int, color: Int, shadow: Boolean = false) {
        val tw = mc.font.width(text)
        ctx.text(mc.font, text, x + (w - tw) / 2, y, color, shadow)
    }

    fun textWidth(text: String): Int = mc.font.width(text)

    val fontHeight: Int get() = mc.font.lineHeight

    fun withAlpha(color: Int, alpha: Int): Int = (alpha.coerceIn(0, 255) shl 24) or (color and 0x00FFFFFF)

    fun isInside(mx: Int, my: Int, x: Int, y: Int, w: Int, h: Int) = mx >= x && mx < x + w && my >= y && my < y + h
}
