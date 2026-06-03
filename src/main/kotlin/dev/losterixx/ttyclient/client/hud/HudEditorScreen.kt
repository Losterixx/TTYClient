package dev.losterixx.ttyclient.client.hud

import dev.losterixx.ttyclient.client.modules.hud.HudManager
import dev.losterixx.ttyclient.client.ui.Draw
import dev.losterixx.ttyclient.client.ui.Theme
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

class HudEditorScreen : Screen(Component.literal("HUD Editor")) {

    private val generalConfig get() = HudManager.generalConfig

    private val elements: List<HudElement> by lazy {
        HudManager.elements
    }

    private var dragging: HudElement? = null
    private var dragOffsetX = 0.0
    private var dragOffsetY = 0.0

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, deltaTick: Float) {

        Draw.rect(context, 0, 0, width, height, 0x50000000)

        Draw.textCentered(context, "Drag elements to reposition them", width / 2, 8, 0, Theme.textSecondary, true)
        Draw.textCentered(context, "Press ESC to save & close", width / 2, 20, 0, Theme.textMuted, true)

        val scale = generalConfig.textScale

        context.pose().pushMatrix()
        context.pose().scale(scale, scale)

        val scaleW = (width / scale).toInt()
        val scaleH = (height / scale).toInt()

        for (element in elements) {
            if (!element.enabled) continue

            val text = element.previewText()
            val textW = font.width(text)
            val (posX, posY) = element.computePosition(element.anchor, element.offsetX, element.offsetY, scaleW, scaleH, text)

            val isDragging = dragging === element
            val isHovered = !isDragging && isHovering(mouseX, mouseY, posX, posY, textW, scale)

            drawElementBox(context, element, text, textW, posX, posY, isDragging, isHovered)
        }

        context.pose().popMatrix()

        super.extractRenderState(context, mouseX, mouseY, deltaTick)
    }

    private fun drawElementBox(
        context: GuiGraphicsExtractor,
        element: HudElement,
        text: String,
        textW: Int,
        x: Int,
        y: Int,
        isDragging: Boolean,
        isHovered: Boolean
    ) {
        val textH = 9
        val paddingX = 4
        val paddingY = 2
        val left = x - paddingX
        val top = y - paddingY
        val right = x + textW + paddingX
        val bottom = y + textH + paddingY - 1

        element.drawAt(context, text, x, y, generalConfig.backgroundEnabled, 0x80000000.toInt(), generalConfig.roundedCorners)

        if (isDragging || isHovered) {
            val overlayColor = when {
                isDragging -> Draw.withAlpha(Theme.accent, 40)
                isHovered -> Draw.withAlpha(Theme.accent, 15)
                else -> 0
            }

            if (overlayColor != 0) {
                if (generalConfig.roundedCorners) {
                    context.fill(left + 1, top + 1, right - 1, bottom - 1, overlayColor)
                    context.fill(left + 1, top, right - 1, top + 1, overlayColor)
                    context.fill(left + 1, bottom - 1, right - 1, bottom, overlayColor)
                    context.fill(left, top + 1, left + 1, bottom - 1, overlayColor)
                    context.fill(right - 1, top + 1, right, bottom - 1, overlayColor)
                } else {
                    context.fill(left, top, right, bottom, overlayColor)
                }
            }

            val borderColor = if (isDragging) Theme.accent else Draw.withAlpha(0xFFFFFF, 120)

            if (generalConfig.roundedCorners) {
                context.fill(left + 1, top, right - 1, top + 1, borderColor)
                context.fill(left + 1, bottom - 1, right - 1, bottom, borderColor)
                context.fill(left, top + 1, left + 1, bottom - 1, borderColor)
                context.fill(right - 1, top + 1, right, bottom - 1, borderColor)
            } else {
                Draw.rectOutline(context, left, top, right - left, bottom - top, borderColor)
            }
        }
    }

    private fun isHovering(mouseX: Int, mouseY: Int, ex: Int, ey: Int, tw: Int, scale: Float): Boolean {
        val paddingX = 4
        val paddingY = 2
        val left = (ex - paddingX) * scale
        val top = (ey - paddingY) * scale
        val right = (ex + tw + paddingX) * scale
        val bottom = (ey + 9 + paddingY - 1) * scale

        return mouseX in left.toInt()..right.toInt() && mouseY in top.toInt()..bottom.toInt()
    }

    override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        if (click.button() != 0) return super.mouseClicked(click, doubled)

        val scale = generalConfig.textScale
        val scaleW = (width / scale).toInt()
        val scaleH = (height / scale).toInt()

        for (element in elements.reversed()) {
            if (!element.enabled) continue

            val text = element.previewText()
            val textW = font.width(text)
            val (posX, posY) = element.computePosition(element.anchor, element.offsetX, element.offsetY, scaleW, scaleH, text)

            if (isHovering(click.x().toInt(), click.y().toInt(), posX, posY, textW, scale)) {
                dragging = element
                dragOffsetX = click.x() - posX * scale
                dragOffsetY = click.y() - posY * scale
                return true
            }
        }

        return super.mouseClicked(click, doubled)
    }

    override fun mouseDragged(click: MouseButtonEvent, offsetX: Double, offsetY: Double): Boolean {
        val element = dragging ?: return super.mouseDragged(click, offsetX, offsetY)
        if (click.button() != 0) return super.mouseDragged(click, offsetX, offsetY)

        val scale = generalConfig.textScale
        val scaleW = (width / scale).toInt()
        val scaleH = (height / scale).toInt()
        val text = element.previewText()
        val textW = font.width(text)

        val newPosX = ((click.x() - dragOffsetX) / scale).toInt()
        val newPosY = ((click.y() - dragOffsetY) / scale).toInt()

        val newOx = when (element.anchor) {
            HudAnchor.TOP_LEFT, HudAnchor.BOTTOM_LEFT -> newPosX
            HudAnchor.TOP_RIGHT, HudAnchor.BOTTOM_RIGHT -> scaleW - newPosX - textW
            HudAnchor.CENTER -> newPosX - scaleW / 2 + textW / 2
        }

        val newOy = when (element.anchor) {
            HudAnchor.TOP_LEFT, HudAnchor.TOP_RIGHT -> newPosY
            HudAnchor.BOTTOM_LEFT, HudAnchor.BOTTOM_RIGHT -> scaleH - newPosY - 9
            HudAnchor.CENTER -> newPosY - scaleH / 2 + 9 / 2
        }

        element.setOffset(newOx, newOy)

        return true
    }

    override fun mouseReleased(click: MouseButtonEvent): Boolean {
        val element = dragging ?: return super.mouseReleased(click)

        if (click.button() != 0) return super.mouseReleased(click)

        val scale = generalConfig.textScale
        val scaleW = (width / scale).toInt()
        val scaleH = (height / scale).toInt()
        val text = element.previewText()

        val (currentPosX, currentPosY) = element.computePosition(
            element.anchor,
            element.offsetX,
            element.offsetY,
            scaleW,
            scaleH,
            text
        )

        val (bestAnchor, newOx, newOy) = element.computeAnchorAndOffset(
            currentPosX,
            currentPosY,
            scaleW,
            scaleH,
            text
        )

        element.setAnchor(bestAnchor)
        element.setOffset(newOx, newOy)
        dragging = null

        return true
    }

    override fun onClose() {
        HudManager.saveAll()
        super.onClose()
    }

    override fun isPauseScreen(): Boolean = false
}




