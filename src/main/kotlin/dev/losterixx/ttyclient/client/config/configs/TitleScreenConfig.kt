package dev.losterixx.ttyclient.client.config.configs

data class TitleScreenConfig(
    var showCardBackground: Boolean = true,
    var items: List<TitleScreenItemConfig> = listOf(
        TitleScreenItemConfig("singleplayer", "S", true),
        TitleScreenItemConfig("multiplayer", "M", true),
        TitleScreenItemConfig("realms", "R", false),
        TitleScreenItemConfig("config", "C", true),
        TitleScreenItemConfig("options", "O", true),
        TitleScreenItemConfig("accessibility", "A", false),
        TitleScreenItemConfig("quit", "Q", true),
    )
)

data class TitleScreenItemConfig(
    var id: String,
    var hotkey: String,
    var enabled: Boolean,
)
