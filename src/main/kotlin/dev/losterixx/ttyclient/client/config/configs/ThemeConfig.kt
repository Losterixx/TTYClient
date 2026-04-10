package dev.losterixx.ttyclient.client.config.configs

data class ThemeConfig(
    // Backgrounds
    var bgPrimary: Int = 0xFF1A1A24.toInt(),
    var bgSecondary: Int = 0xFF222232.toInt(),
    var bgTertiary: Int = 0xFF2A2A3A.toInt(),

    // Accent colors
    var accent: Int = 0xFFEF4444.toInt(),

    // Text colors
    var textPrimary: Int = 0xFFFFFFFF.toInt(),
    var textSecondary: Int = 0xFFB8B8C8.toInt(),
    var textMuted: Int = 0xFF808090.toInt(),
    var textDark: Int = 0xFF585866.toInt(),

    // Special colors
    var border: Int = 0xFF404055.toInt(),
    var danger: Int = 0xFFEF4444.toInt(),

    // Other
    var editorTheme: EditorThemeConfig = EditorThemeConfig()
)

data class EditorThemeConfig(
    // Backgrounds
    var base: Int = 0xFF1E1E2E.toInt(),
    var mantle: Int = 0xFF181825.toInt(),
    var crust: Int = 0xFF13131E.toInt(),
    var surface: Int = 0xFF313244.toInt(),
    var overlay: Int = 0xFF6C7086.toInt(),

    // Syntax colors
    var selection: Int = 0xFF89B4FA.toInt(),
    var text: Int = 0xFFCDD6F4.toInt(),
    var subtext: Int = 0xFFA6ADC8.toInt(),
    var comment: Int = 0xFF6C7086.toInt(),
    var key: Int = 0xFF89B4FA.toInt(),
    var string: Int = 0xFFA6E3A1.toInt(),
    var number: Int = 0xFFFAB387.toInt(),
    var keyword: Int = 0xFFF38BA8.toInt(),
    var punctuation: Int = 0xFF7F849C.toInt(),

    // Gutter
    var gutterActive: Int = 0xFFBAC2DE.toInt(),
    var gutter: Int = 0xFF585B70.toInt(),

    // File tree icons
    var treeDir: Int = 0xFFAEB6C3.toInt(),
    var treeJsonc: Int = 0xFFE5C07B.toInt(),
    var treeTxt: Int = 0xFFABB2BF.toInt(),
    var treeOther: Int = 0xFF5C6370.toInt()
)

