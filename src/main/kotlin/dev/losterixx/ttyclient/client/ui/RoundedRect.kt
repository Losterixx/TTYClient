package dev.losterixx.ttyclient.client.ui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.math.sqrt

object RoundedRect {
    private val mc get() = Minecraft.getInstance()

    const val CORNER_TOP_LEFT = 1
    const val CORNER_TOP_RIGHT = 2
    const val CORNER_BOTTOM_LEFT = 4
    const val CORNER_BOTTOM_RIGHT = 8
    const val CORNER_ALL = CORNER_TOP_LEFT or CORNER_TOP_RIGHT or CORNER_BOTTOM_LEFT or CORNER_BOTTOM_RIGHT
    const val CORNER_LEFT = CORNER_TOP_LEFT or CORNER_BOTTOM_LEFT
    const val CORNER_RIGHT = CORNER_TOP_RIGHT or CORNER_BOTTOM_RIGHT
    const val CORNER_TOP = CORNER_TOP_LEFT or CORNER_TOP_RIGHT
    const val CORNER_BOTTOM = CORNER_BOTTOM_LEFT or CORNER_BOTTOM_RIGHT

    private val cornerCache = mutableMapOf<Int, Array<FloatArray>>()

    data class Radii(
        val topLeft: Int = 0,
        val topRight: Int = 0,
        val bottomLeft: Int = 0,
        val bottomRight: Int = 0
    ) {
        companion object {
            fun uniform(radius: Int) = Radii(radius, radius, radius, radius)
            fun left(radius: Int) = Radii(topLeft = radius, bottomLeft = radius)
            fun right(radius: Int) = Radii(topRight = radius, bottomRight = radius)
            fun top(radius: Int) = Radii(topLeft = radius, topRight = radius)
            fun bottom(radius: Int) = Radii(bottomLeft = radius, bottomRight = radius)
        }
    }

    private fun getOrComputeCorner(radiusInScreenPixels: Int): Array<FloatArray> {
        return cornerCache.getOrPut(radiusInScreenPixels) {
            val sr = radiusInScreenPixels
            val rd = sr.toDouble()

            Array(sr) { i ->
                FloatArray(sr) { j ->
                    val dx = rd - i - 0.5
                    val dy = rd - j - 0.5
                    val dist = sqrt(dx * dx + dy * dy)
                    ((rd + 0.5 - dist).coerceIn(0.0, 1.0)).toFloat()
                }
            }
        }
    }

    private fun getOrComputeCornerOutline(outerRadiusInScreenPixels: Int, innerRadiusInScreenPixels: Int): Array<FloatArray> {
        val key = (outerRadiusInScreenPixels shl 16) or innerRadiusInScreenPixels

        return cornerCache.getOrPut(key) {
            val sr = outerRadiusInScreenPixels
            val outerR = sr.toDouble()
            val innerR = innerRadiusInScreenPixels.toDouble().coerceAtLeast(0.0)

            Array(sr) { i ->
                FloatArray(sr) { j ->
                    val dx = outerR - i - 0.5
                    val dy = outerR - j - 0.5
                    val dist = sqrt(dx * dx + dy * dy)
                    val outer = (outerR + 0.5 - dist).coerceIn(0.0, 1.0)
                    val inner = (dist - (innerR - 0.5)).coerceIn(0.0, 1.0)
                    (outer * inner).toFloat()
                }
            }
        }
    }

    fun clearCornerCache() {
        cornerCache.clear()
    }

    fun draw(ctx: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int, radii: Radii, color: Int) {
        if (w <= 0 || h <= 0) return

        val rTL = radii.topLeft.coerceAtMost(w / 2).coerceAtMost(h / 2)
        val rTR = radii.topRight.coerceAtMost(w / 2).coerceAtMost(h / 2)
        val rBL = radii.bottomLeft.coerceAtMost(w / 2).coerceAtMost(h / 2)
        val rBR = radii.bottomRight.coerceAtMost(w / 2).coerceAtMost(h / 2)

        val maxR = maxOf(rTL, rTR, rBL, rBR)
        if (maxR <= 0) {
            ctx.fill(x, y, x + w, y + h, color)
            return
        }

        val baseAlpha = (color ushr 24) and 0xFF
        val rgb = color and 0x00FFFFFF
        val scale = mc.window.guiScale.toInt().coerceAtLeast(1)

        val topMax = maxOf(rTL, rTR)
        val bottomMax = maxOf(rBL, rBR)

        ctx.fill(x, y + topMax, x + w, y + h - bottomMax, color)

        ctx.fill(x + rTL, y, x + w - rTR, y + topMax, color)

        if (rTL < topMax) ctx.fill(x, y + rTL, x + rTL, y + topMax, color)
        if (rTR < topMax) ctx.fill(x + w - rTR, y + rTR, x + w, y + topMax, color)

        ctx.fill(x + rBL, y + h - bottomMax, x + w - rBR, y + h, color)

        if (rBL < bottomMax) ctx.fill(x, y + h - bottomMax, x + rBL, y + h - rBL, color)
        if (rBR < bottomMax) ctx.fill(x + w - rBR, y + h - bottomMax, x + w, y + h - rBR, color)

        val pose = ctx.pose()
        pose.pushMatrix()
        pose.scale(1f / scale, 1f / scale)

        val sx = x * scale
        val sy = y * scale
        val sw = w * scale
        val sh = h * scale

        if (rTL > 0) drawCorner(ctx, sx, sy, rTL * scale, baseAlpha, rgb, CornerPosition.TOP_LEFT)
        if (rTR > 0) drawCorner(ctx, sx + sw, sy, rTR * scale, baseAlpha, rgb, CornerPosition.TOP_RIGHT)
        if (rBL > 0) drawCorner(ctx, sx, sy + sh, rBL * scale, baseAlpha, rgb, CornerPosition.BOTTOM_LEFT)
        if (rBR > 0) drawCorner(ctx, sx + sw, sy + sh, rBR * scale, baseAlpha, rgb, CornerPosition.BOTTOM_RIGHT)

        pose.popMatrix()
    }

    fun drawOutline(ctx: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int, radii: Radii, color: Int, thickness: Int = 1) {
        if (w <= 0 || h <= 0) return

        val rTL = radii.topLeft.coerceAtMost(w / 2).coerceAtMost(h / 2)
        val rTR = radii.topRight.coerceAtMost(w / 2).coerceAtMost(h / 2)
        val rBL = radii.bottomLeft.coerceAtMost(w / 2).coerceAtMost(h / 2)
        val rBR = radii.bottomRight.coerceAtMost(w / 2).coerceAtMost(h / 2)

        val maxR = maxOf(rTL, rTR, rBL, rBR)
        if (maxR <= 0) {
            Draw.rectOutline(ctx, x, y, w, h, color, thickness)
            return
        }

        val baseAlpha = (color ushr 24) and 0xFF
        val rgb = color and 0x00FFFFFF
        val scale = mc.window.guiScale.toInt().coerceAtLeast(1)

        val topLeftX = if (rTL > 0) x + rTL else x
        val topRightX = if (rTR > 0) x + w - rTR else x + w
        ctx.fill(topLeftX, y, topRightX, y + thickness, color)

        val bottomLeftX = if (rBL > 0) x + rBL else x
        val bottomRightX = if (rBR > 0) x + w - rBR else x + w
        ctx.fill(bottomLeftX, y + h - thickness, bottomRightX, y + h, color)

        val leftTopY = if (rTL > 0) y + rTL else y
        val leftBottomY = if (rBL > 0) y + h - rBL else y + h
        ctx.fill(x, leftTopY, x + thickness, leftBottomY, color)

        val rightTopY = if (rTR > 0) y + rTR else y
        val rightBottomY = if (rBR > 0) y + h - rBR else y + h
        ctx.fill(x + w - thickness, rightTopY, x + w, rightBottomY, color)

        val pose = ctx.pose()
        pose.pushMatrix()
        pose.scale(1f / scale, 1f / scale)

        val sx = x * scale
        val sy = y * scale
        val sw = w * scale
        val sh = h * scale
        val st = thickness * scale

        if (rTL > 0) drawCornerOutline(ctx, sx, sy, rTL * scale, st, baseAlpha, rgb, CornerPosition.TOP_LEFT)
        if (rTR > 0) drawCornerOutline(ctx, sx + sw, sy, rTR * scale, st, baseAlpha, rgb, CornerPosition.TOP_RIGHT)
        if (rBL > 0) drawCornerOutline(ctx, sx, sy + sh, rBL * scale, st, baseAlpha, rgb, CornerPosition.BOTTOM_LEFT)
        if (rBR > 0) drawCornerOutline(ctx, sx + sw, sy + sh, rBR * scale, st, baseAlpha, rgb, CornerPosition.BOTTOM_RIGHT)

        pose.popMatrix()
    }

    private enum class CornerPosition { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    private fun drawCorner(ctx: GuiGraphicsExtractor, cx: Int, cy: Int, sr: Int, baseAlpha: Int, rgb: Int, pos: CornerPosition) {
        val cornerAlpha = getOrComputeCorner(sr)

        for (i in 0 until sr) {
            for (j in 0 until sr) {
                val t = cornerAlpha[i][j]
                if (t <= 0f) continue

                val a = (t * baseAlpha + 0.5).toInt()
                val c = (a shl 24) or rgb

                val px = when (pos) {
                    CornerPosition.TOP_LEFT, CornerPosition.BOTTOM_LEFT -> cx + i
                    CornerPosition.TOP_RIGHT, CornerPosition.BOTTOM_RIGHT -> cx - 1 - i
                }

                val py = when (pos) {
                    CornerPosition.TOP_LEFT, CornerPosition.TOP_RIGHT -> cy + j
                    CornerPosition.BOTTOM_LEFT, CornerPosition.BOTTOM_RIGHT -> cy - 1 - j
                }

                ctx.fill(px, py, px + 1, py + 1, c)
            }
        }
    }

    private fun drawCornerOutline(ctx: GuiGraphicsExtractor, cx: Int, cy: Int, sr: Int, st: Int, baseAlpha: Int, rgb: Int, pos: CornerPosition) {
        val cornerAlpha = getOrComputeCornerOutline(sr, sr - st)

        for (i in 0 until sr) {
            for (j in 0 until sr) {
                val alphaMul = cornerAlpha[i][j]
                if (alphaMul <= 0f) continue

                val a = (alphaMul * baseAlpha + 0.5).toInt()
                val c = (a shl 24) or rgb

                val px = when (pos) {
                    CornerPosition.TOP_LEFT, CornerPosition.BOTTOM_LEFT -> cx + i
                    CornerPosition.TOP_RIGHT, CornerPosition.BOTTOM_RIGHT -> cx - 1 - i
                }

                val py = when (pos) {
                    CornerPosition.TOP_LEFT, CornerPosition.TOP_RIGHT -> cy + j
                    CornerPosition.BOTTOM_LEFT, CornerPosition.BOTTOM_RIGHT -> cy - 1 - j
                }

                ctx.fill(px, py, px + 1, py + 1, c)
            }
        }
    }

    fun isInside(mx: Int, my: Int, x: Int, y: Int, w: Int, h: Int, radii: Radii): Boolean {
        if (mx < x || mx >= x + w || my < y || my >= y + h) return false

        val rTL = radii.topLeft.coerceAtMost(w / 2).coerceAtMost(h / 2)
        val rTR = radii.topRight.coerceAtMost(w / 2).coerceAtMost(h / 2)
        val rBL = radii.bottomLeft.coerceAtMost(w / 2).coerceAtMost(h / 2)
        val rBR = radii.bottomRight.coerceAtMost(w / 2).coerceAtMost(h / 2)

        val maxR = maxOf(rTL, rTR, rBL, rBR)
        if (maxR <= 0) return true

        val inTopLeft = mx < x + rTL && my < y + rTL
        val inTopRight = mx >= x + w - rTR && my < y + rTR
        val inBottomLeft = mx < x + rBL && my >= y + h - rBL
        val inBottomRight = mx >= x + w - rBR && my >= y + h - rBR

        if (inTopLeft && rTL > 0) {
            val dx = mx - (x + rTL)
            val dy = my - (y + rTL)
            return dx * dx + dy * dy <= rTL * rTL
        }

        if (inTopRight && rTR > 0) {
            val dx = mx - (x + w - rTR)
            val dy = my - (y + rTR)
            return dx * dx + dy * dy <= rTR * rTR
        }

        if (inBottomLeft && rBL > 0) {
            val dx = mx - (x + rBL)
            val dy = my - (y + h - rBL)
            return dx * dx + dy * dy <= rBL * rBL
        }

        if (inBottomRight && rBR > 0) {
            val dx = mx - (x + w - rBR)
            val dy = my - (y + h - rBR)
            return dx * dx + dy * dy <= rBR * rBR
        }

        return true
    }
}

