package dev.losterixx.ttyclient.client.screens

import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.ui.Draw
import dev.losterixx.ttyclient.client.ui.Theme
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.client.gui.screens.options.OptionsScreen
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

class TTYClientTitleScreen : Screen(Component.literal("TTYClient")) {

    companion object {
        private val YELLOW = 0xFFE5C07B.toInt()
        private val COMMENT = 0xFF5C6370.toInt()
        private val FG = 0xFFABB2BF.toInt()

        private val LOGO = listOf(
            "  ______      _____      __  __ ",
            " /\\__   _\\   /\\__  _\\   /\\ \\_\\ \\",
            "   \\/_/\\ \\/   \\/_/\\ \\/   \\ \\____ \\",
            "         \\ \\_\\       \\ \\_\\    \\/\\____\\",
            "           \\/_/        \\/_/     \\/____/"
        )
        /*private val LOGO = listOf(
            "████████╗████████╗██╗   ██╗",
            "   ██╔══╝╚══██╔══╝╚██╗ ██╔╝",
            "   ██║      ██║    ╚████╔╝ ",
            "   ██║      ██║     ╚██╔╝  ",
            "   ██║      ██║      ██║   ",
            "   ╚═╝      ╚═╝      ╚═╝   "
        )*/
    }

    private data class MenuItem(
        val label: String,
        val key: String,
        val action: TTYClientTitleScreen.() -> Unit
    )

    private val items: List<MenuItem> by lazy {
        listOf(
            MenuItem("Singleplayer", "s") { minecraft?.setScreen(SelectWorldScreen(this)) },
            MenuItem("Multiplayer", "m") { minecraft?.setScreen(JoinMultiplayerScreen(this)) },
            MenuItem("Options", "o") { minecraft?.setScreen(OptionsScreen(this, minecraft!!.options, false)) },
            MenuItem("Quit", "q") { minecraft?.stop() }
        )
    }

    private var hoveredIndex = -1

    override fun onClose() { }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        Draw.rect(context, 0, 0, width, height, Theme.bgPrimary)

        val cx = width / 2
        val lh = Draw.fontHeight + 2

        val logoTop = height / 4 - (LOGO.size * lh) / 2
        LOGO.forEachIndexed { i, line ->
            Draw.textCentered(context, line, cx - 10, logoTop + i * lh, 0, Theme.accent, false)
        }

        val versionY = logoTop + LOGO.size * lh + 6
        Draw.textCentered(context, "v${MainClient.VERSION}", cx, versionY, 0, COMMENT, false)

        val sepY = versionY + lh + 12
        Draw.rect(context, cx - 45, sepY, 90, 1, Draw.withAlpha(COMMENT, 120))

        val keyColW = Draw.textWidth("[m]") + 2
        val blockW = 110
        val blockX = cx - blockW / 2
        val labelX = blockX + keyColW + 5
        val menuTop = sepY + lh + 6
        val itemH = lh + 6

        hoveredIndex = -1
        items.forEachIndexed { i, item ->
            val y = menuTop + i * itemH
            val hovered = Draw.isInside(mouseX, mouseY, blockX - 4, y - 4, blockW + 8, lh + 4)

            if (hovered) {
                hoveredIndex = i
                Draw.rect(context, blockX - 4, y - 4, blockW + 8, lh + 4, Draw.withAlpha(Theme.bgHover, 180))
                Draw.rect(context, blockX - 4, y - 4, 2, lh + 4, Theme.accent)
            }

            val keyStr = "[${item.key}]"
            Draw.text(context, keyStr, blockX + 2, y, YELLOW, false)

            val labelColor = if (hovered) FG else Theme.textSecondary
            Draw.text(context, item.label, labelX + 2, y, labelColor, false)
        }

        Draw.textCentered(context, "TTYClient · v${MainClient.VERSION}", cx, height - 12, 0, COMMENT, false)
    }

    override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        if (click.button() == 0 && hoveredIndex >= 0) {
            items[hoveredIndex].action(this)
            return true
        }

        return super.mouseClicked(click, doubled)
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        items.forEach { item ->
            if (input.key() == item.key[0].uppercaseChar().code) {
                minecraft?.execute { item.action(this) }
                return true
            }
        }

        return super.keyPressed(input)
    }

    override fun charTyped(input: CharacterEvent): Boolean {
        val ch = input.codepoint().toChar().lowercaseChar()
        if (items.any { it.key[0].lowercaseChar() == ch }) return true

        return super.charTyped(input)
    }
}
