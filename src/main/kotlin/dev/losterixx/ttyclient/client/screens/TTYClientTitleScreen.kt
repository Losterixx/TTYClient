package dev.losterixx.ttyclient.client.screens

import com.mojang.realmsclient.RealmsMainScreen
import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.MainClient.MOD_ID
import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.TitleScreenConfig
import dev.losterixx.ttyclient.client.ui.Draw
import dev.losterixx.ttyclient.client.ui.RoundedRect
import dev.losterixx.ttyclient.client.ui.Theme
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen
import net.minecraft.client.gui.screens.options.OptionsScreen
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FontDescription
import net.minecraft.resources.Identifier

class TTYClientTitleScreen : Screen(Component.literal("TTYClient")) {

    companion object {
        private val YELLOW = 0xFFE5C07B.toInt()
        private val COMMENT = 0xFF5C6370.toInt()
        private val FG = 0xFFABB2BF.toInt()

        private val LOGO = listOf(
            " ______     ______     __  __    ",
            "/\\__  _\\   /\\__  _\\   /\\ \\_\\ \\   ",
            "\\/_/\\ \\/   \\/_/\\ \\/   \\ \\____ \\  ",
            "   \\ \\_\\      \\ \\_\\    \\/\\_____\\ ",
            "    \\/_/       \\/_/     \\/_____/ "
        )

        private const val CONFIG_PATH = "config/titlescreen.jsonc"

        fun loadConfig(): TitleScreenConfig = ConfigManager.loadConfig(CONFIG_PATH, TitleScreenConfig::class.java) { TitleScreenConfig() }
        fun saveConfig(cfg: TitleScreenConfig) = ConfigManager.saveConfig(CONFIG_PATH, cfg)

        val JETBRAINS_FONT: FontDescription = FontDescription.Resource(
            Identifier.fromNamespaceAndPath(MOD_ID, "jetbrains_mono")
        )
    }

    private val actionMap: Map<String, TTYClientTitleScreen.() -> Unit> = mapOf(
        "singleplayer" to { minecraft?.setScreen(SelectWorldScreen(this)) },
        "multiplayer" to { minecraft?.setScreen(JoinMultiplayerScreen(this)) },
        "realms" to { minecraft?.setScreen(RealmsMainScreen(this)) },
        "config" to { minecraft?.setScreen(ConfigEditorScreen()) },
        "options" to { minecraft?.setScreen(OptionsScreen(this, minecraft!!.options, false)) },
        "accessibility" to { minecraft?.setScreen(AccessibilityOptionsScreen(this, minecraft!!.options)) },
        "quit" to { minecraft?.stop() },
    )

    private val labelMap: Map<String, String> = mapOf(
        "singleplayer" to "Singleplayer",
        "multiplayer" to "Multiplayer",
        "realms" to "Realms",
        "config" to "Config Editor",
        "options" to "Options",
        "accessibility" to "Accessibility",
        "quit" to "Quit",
    )

    private data class MenuItem(
        val id: String,
        val label: String,
        val key: String,
        val action: TTYClientTitleScreen.() -> Unit,
    )

    private var cfg: TitleScreenConfig = loadConfig()

    private val items: List<MenuItem> get() = cfg.items
        .filter { it.enabled && it.hotkey.isNotEmpty() && actionMap.containsKey(it.id) }
        .map { itemCfg ->
            MenuItem(
                id = itemCfg.id,
                label = labelMap[itemCfg.id] ?: itemCfg.id,
                key = itemCfg.hotkey.take(1).lowercase(),
                action = actionMap[itemCfg.id]!!,
            )
        }

    private var hoveredIndex = -1

    override fun init() {
        super.init()
        cfg = loadConfig()
    }

    override fun onClose() { }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        Draw.rect(context, 0, 0, width, height, Theme.bgPrimary)

        val cx = width / 2
        val lh = Draw.fontHeight

        val logoTop = height / 4 - (LOGO.size * lh) / 2
        LOGO.forEachIndexed { i, line ->
            Draw.textCentered(context, line, cx, logoTop + i * lh, 0, Theme.accent, false)
        }

        val versionY = logoTop + LOGO.size * lh + 6
        Draw.textCentered(context, "v${MainClient.VERSION}", cx, versionY, 0, COMMENT, false)

        val activeItems = items
        val keyColW = Draw.textWidth("[m]") + 2
        val blockW = 120
        val blockX = cx - blockW / 2
        val labelX = blockX + keyColW + 2
        val menuTop = versionY + lh + 28
        val itemH = lh + 8
        val hPad = 12
        val vPad = 6
        val cardR = 6

        val cardX = blockX - hPad
        val cardY = menuTop - vPad
        val cardW = blockW + hPad * 2
        val cardH = activeItems.size * itemH + vPad * 2 - 12

        if (cfg.showCardBackground && activeItems.isNotEmpty()) {
            val cardRadii = RoundedRect.Radii(topLeft = 2, topRight = cardR, bottomLeft = 2, bottomRight = cardR)
            Draw.roundedRect(context, cardX, cardY, cardW, cardH, cardRadii, Draw.withAlpha(Theme.bgSecondary, 180))
        }

        hoveredIndex = -1

        val mouseInMenuArea = mouseX >= cardX && mouseX < cardX + cardW && mouseY >= cardY && mouseY < cardY + cardH

        activeItems.forEachIndexed { i, item ->
            val y = menuTop + i * itemH - 2
            val rowX = blockX - hPad + 2
            val rowW = blockW + hPad * 2 - 4
            val rowH = lh + 4

            val hoverRadii = RoundedRect.Radii(topLeft = 1, topRight = 4, bottomLeft = 1, bottomRight = 4)
            val hovered = mouseInMenuArea && Draw.isInsideRounded(mouseX, mouseY, rowX, y - 4, rowW, rowH, hoverRadii)

            if (hovered) {
                hoveredIndex = i
                Draw.roundedRect(context, rowX, y - 2, rowW, rowH, hoverRadii, Draw.withAlpha(Theme.bgHover, 200))
                Draw.roundedRect(context, rowX, y - 2, 2, rowH, 1, Theme.accent, Draw.CORNER_LEFT)
            }

            Draw.text(context, "[${item.key}]", blockX - 2, y, YELLOW, false)
            Draw.text(context, item.label, labelX - 2, y, if (hovered) FG else Theme.textSecondary, false)
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
        if (items.any { it.key[0] == ch }) return true
        return super.charTyped(input)
    }
}
