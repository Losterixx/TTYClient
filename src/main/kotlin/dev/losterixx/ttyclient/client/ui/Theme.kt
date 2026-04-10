package dev.losterixx.ttyclient.client.ui

import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.EditorThemeConfig

object Theme {
    private val logger = MainClient.LOGGER

    var bgPrimary = 0xFF1A1A24.toInt()
    var bgSecondary = 0xFF222232.toInt()
    var bgTertiary = 0xFF2A2A3A.toInt()
    var bgHover = 0xFF353548.toInt()

    var accent = 0xFFEF4444.toInt()
    var accentHover = 0xFFFF6B6B.toInt()

    var textPrimary = 0xFFFFFFFF.toInt()
    var textSecondary = 0xFFB8B8C8.toInt()
    var textMuted = 0xFF808090.toInt()
    var textDark = 0xFF585866.toInt()

    var border = 0xFF404055.toInt()
    var danger = 0xFFEF4444.toInt()

    var editorTheme: EditorThemeConfig = EditorThemeConfig()

    fun lightenColor(color: Int, amount: Float): Int {
        val a = (color shr 24) and 0xFF
        val r = ((color shr 16) and 0xFF).let { (it + (255 - it) * amount).toInt().coerceIn(0, 255) }
        val g = ((color shr 8) and 0xFF).let { (it + (255 - it) * amount).toInt().coerceIn(0, 255) }
        val b = (color and 0xFF).let { (it + (255 - it) * amount).toInt().coerceIn(0, 255) }
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
    
    fun darkenColor(color: Int, amount: Float): Int {
        val a = (color shr 24) and 0xFF
        val r = ((color shr 16) and 0xFF).let { (it * (1f - amount)).toInt().coerceIn(0, 255) }
        val g = ((color shr 8) and 0xFF).let { (it * (1f - amount)).toInt().coerceIn(0, 255) }
        val b = (color and 0xFF).let { (it * (1f - amount)).toInt().coerceIn(0, 255) }
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    fun loadFromConfig() {
        val themeConfig = ConfigManager.theme

        bgPrimary = themeConfig.bgPrimary
        bgSecondary = themeConfig.bgSecondary
        bgTertiary = themeConfig.bgTertiary
        accent = themeConfig.accent
        textPrimary = themeConfig.textPrimary
        textSecondary = themeConfig.textSecondary
        textMuted = themeConfig.textMuted
        textDark = themeConfig.textDark
        border = themeConfig.border
        danger = themeConfig.danger
        editorTheme = themeConfig.editorTheme

        logger.info("Theme loaded from config.")
    }
}
