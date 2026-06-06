package dev.losterixx.ttyclient.client.ui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor

object Draw {
    const val CORNER_TOP_LEFT = RoundedRect.CORNER_TOP_LEFT
    const val CORNER_TOP_RIGHT = RoundedRect.CORNER_TOP_RIGHT
    const val CORNER_BOTTOM_LEFT = RoundedRect.CORNER_BOTTOM_LEFT
    const val CORNER_BOTTOM_RIGHT = RoundedRect.CORNER_BOTTOM_RIGHT
    const val CORNER_ALL = RoundedRect.CORNER_ALL
    const val CORNER_LEFT = RoundedRect.CORNER_LEFT
    const val CORNER_RIGHT = RoundedRect.CORNER_RIGHT
    const val CORNER_TOP = RoundedRect.CORNER_TOP
    const val CORNER_BOTTOM = RoundedRect.CORNER_BOTTOM

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

    fun roundedRect(ctx: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int, r: Int, color: Int, corners: Int = CORNER_ALL) {
        val radii = when {
            corners == CORNER_ALL -> RoundedRect.Radii.uniform(r)
            corners == CORNER_LEFT -> RoundedRect.Radii.left(r)
            corners == CORNER_RIGHT -> RoundedRect.Radii.right(r)
            corners == CORNER_TOP -> RoundedRect.Radii.top(r)
            corners == CORNER_BOTTOM -> RoundedRect.Radii.bottom(r)

            else -> RoundedRect.Radii(
                topLeft = if ((corners and CORNER_TOP_LEFT) != 0) r else 0,
                topRight = if ((corners and CORNER_TOP_RIGHT) != 0) r else 0,
                bottomLeft = if ((corners and CORNER_BOTTOM_LEFT) != 0) r else 0,
                bottomRight = if ((corners and CORNER_BOTTOM_RIGHT) != 0) r else 0
            )
        }

        RoundedRect.draw(ctx, x, y, w, h, radii, color)
    }

    fun roundedRect(ctx: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int, radii: RoundedRect.Radii, color: Int) {
        RoundedRect.draw(ctx, x, y, w, h, radii, color)
    }

    fun roundedRectOutline(ctx: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int, r: Int, color: Int, t: Int = 1, corners: Int = CORNER_ALL) {
        val radii = when {
            corners == CORNER_ALL -> RoundedRect.Radii.uniform(r)
            corners == CORNER_LEFT -> RoundedRect.Radii.left(r)
            corners == CORNER_RIGHT -> RoundedRect.Radii.right(r)
            corners == CORNER_TOP -> RoundedRect.Radii.top(r)
            corners == CORNER_BOTTOM -> RoundedRect.Radii.bottom(r)

            else -> RoundedRect.Radii(
                topLeft = if ((corners and CORNER_TOP_LEFT) != 0) r else 0,
                topRight = if ((corners and CORNER_TOP_RIGHT) != 0) r else 0,
                bottomLeft = if ((corners and CORNER_BOTTOM_LEFT) != 0) r else 0,
                bottomRight = if ((corners and CORNER_BOTTOM_RIGHT) != 0) r else 0
            )
        }

        RoundedRect.drawOutline(ctx, x, y, w, h, radii, color, t)
    }

    fun roundedRectOutline(ctx: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int, radii: RoundedRect.Radii, color: Int, t: Int = 1) {
        RoundedRect.drawOutline(ctx, x, y, w, h, radii, color, t)
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

    fun isInsideRounded(mx: Int, my: Int, x: Int, y: Int, w: Int, h: Int, r: Int, corners: Int = CORNER_ALL): Boolean {
        val radii = when {
            corners == CORNER_ALL -> RoundedRect.Radii.uniform(r)
            corners == CORNER_LEFT -> RoundedRect.Radii.left(r)
            corners == CORNER_RIGHT -> RoundedRect.Radii.right(r)
            corners == CORNER_TOP -> RoundedRect.Radii.top(r)
            corners == CORNER_BOTTOM -> RoundedRect.Radii.bottom(r)
            else -> RoundedRect.Radii(
                topLeft = if ((corners and CORNER_TOP_LEFT) != 0) r else 0,
                topRight = if ((corners and CORNER_TOP_RIGHT) != 0) r else 0,
                bottomLeft = if ((corners and CORNER_BOTTOM_LEFT) != 0) r else 0,
                bottomRight = if ((corners and CORNER_BOTTOM_RIGHT) != 0) r else 0
            )
        }
        return RoundedRect.isInside(mx, my, x, y, w, h, radii)
    }

    fun isInsideRounded(mx: Int, my: Int, x: Int, y: Int, w: Int, h: Int, radii: RoundedRect.Radii): Boolean {
        return RoundedRect.isInside(mx, my, x, y, w, h, radii)
    }
}
