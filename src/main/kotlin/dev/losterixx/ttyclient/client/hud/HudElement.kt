package dev.losterixx.ttyclient.client.hud

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor

abstract class HudElement {

    protected val textHeight: Int = 9
    private val bgPadX: Int = 4
    private val bgPadY: Int = 2

    abstract val enabled: Boolean
    abstract val anchor: HudAnchor
    abstract val offsetX: Int
    abstract val offsetY: Int

    abstract fun resolveText(): String
    abstract fun previewText(): String
    abstract fun setOffset(x: Int, y: Int)
    abstract fun setAnchor(anchor: HudAnchor)

    fun render(
        context: GuiGraphicsExtractor,
        scaledScreenW: Int,
        scaledScreenH: Int,
        drawBackground: Boolean,
        backgroundColor: Int,
        roundedCorners: Boolean,
    ) {
        val text = resolveText()
        val (x, y) = computePosition(anchor, offsetX, offsetY, scaledScreenW, scaledScreenH, text)
        drawAt(context, text, x, y, drawBackground, backgroundColor, roundedCorners)
    }

    fun drawAt(
        context: GuiGraphicsExtractor,
        text: String,
        x: Int,
        y: Int,
        drawBackground: Boolean,
        backgroundColor: Int,
        roundedCorners: Boolean = true,
    ) {
        val font = Minecraft.getInstance().font
        val textWidth = font.width(text)

        if (drawBackground) {
            val left = x - bgPadX
            val top = y - bgPadY
            val right = x + textWidth + bgPadX
            val bottom = y + textHeight + bgPadY - 1

            if (roundedCorners) {
                context.fill(left + 1, top + 1, right - 1, bottom - 1, backgroundColor)
                context.fill(left + 1, top, right - 1, top + 1, backgroundColor)
                context.fill(left + 1, bottom - 1, right - 1, bottom, backgroundColor)
                context.fill(left, top + 1, left + 1, bottom - 1, backgroundColor)
                context.fill(right - 1, top + 1, right, bottom - 1, backgroundColor)
            } else {
                context.fill(left, top, right, bottom, backgroundColor)
            }
        }

        context.text(font, text, x, y, -1, true)
    }

    fun computePosition(
        anchor: HudAnchor,
        offsetX: Int,
        offsetY: Int,
        scaleW: Int,
        scaleH: Int,
        text: String
    ): Pair<Int, Int> {
        val textW = Minecraft.getInstance().font.width(text)

        val x = when (anchor) {
            HudAnchor.TOP_LEFT, HudAnchor.BOTTOM_LEFT -> offsetX
            HudAnchor.TOP_RIGHT, HudAnchor.BOTTOM_RIGHT -> scaleW - offsetX - textW
            HudAnchor.CENTER -> scaleW / 2 - textW / 2 + offsetX
        }

        val y = when (anchor) {
            HudAnchor.TOP_LEFT, HudAnchor.TOP_RIGHT -> offsetY
            HudAnchor.BOTTOM_LEFT, HudAnchor.BOTTOM_RIGHT -> scaleH - offsetY - textHeight
            HudAnchor.CENTER -> scaleH / 2 - textHeight / 2 + offsetY
        }

        return Pair(x, y)
    }

    fun computeAnchorAndOffset(
        screenX: Int,
        screenY: Int,
        scaleW: Int,
        scaleH: Int,
        text: String
    ): Triple<HudAnchor, Int, Int> {
        val textW = Minecraft.getInstance().font.width(text)
        val cx = screenX + textW / 2
        val cy = screenY + textHeight / 2

        val rx = cx.toFloat() / scaleW
        val ry = cy.toFloat() / scaleH

        val nearLeft = rx <= 0.5f
        val nearTop = ry <= 0.5f
        val nearCenter = rx in 0.35f..0.65f && ry in 0.35f..0.65f

        val bestAnchor = when {
            nearCenter -> HudAnchor.CENTER
            nearLeft && nearTop -> HudAnchor.TOP_LEFT
            !nearLeft && nearTop -> HudAnchor.TOP_RIGHT
            nearLeft && !nearTop -> HudAnchor.BOTTOM_LEFT
            else -> HudAnchor.BOTTOM_RIGHT
        }

        val newOffsetX = when (bestAnchor) {
            HudAnchor.TOP_LEFT, HudAnchor.BOTTOM_LEFT -> screenX
            HudAnchor.TOP_RIGHT, HudAnchor.BOTTOM_RIGHT -> scaleW - screenX - textW
            HudAnchor.CENTER -> screenX - scaleW / 2 + textW / 2
        }

        val newOffsetY = when (bestAnchor) {
            HudAnchor.TOP_LEFT, HudAnchor.TOP_RIGHT -> screenY
            HudAnchor.BOTTOM_LEFT, HudAnchor.BOTTOM_RIGHT -> scaleH - screenY - textHeight
            HudAnchor.CENTER -> screenY - scaleH / 2 + textHeight / 2
        }

        return Triple(bestAnchor, newOffsetX, newOffsetY)
    }
}



