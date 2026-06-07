package dev.losterixx.ttyclient.client.config.configs.hud

import dev.losterixx.ttyclient.client.config.Comment

data class GeneralHudConfig(
    @Comment("Enable or disable the HUD module.")
    var enabled: Boolean = true,

    @Comment("Hide HUD when F1 (hide GUI) is pressed.")
    var hideOnF1: Boolean = true,

    @Comment("Hide HUD when F3 (debug overlay) is active.")
    var hideOnF3: Boolean = false,

    @Comment("Hide HUD when chat is open.")
    var hideOnChat: Boolean = false,

    @Comment("Hide HUD when ESC menu is open.")
    var hideOnESC: Boolean = false,

    @Comment("Enable background behind HUD elements.")
    var backgroundEnabled: Boolean = true,

    @Comment(
        "Background style: RECT = sharp corners, PIXEL = 1px pseudo-rounded, ROUNDED = smooth rounded corners."
    )
    var backgroundStyle: String = "PIXEL",

    @Comment("Radius for rounded corners (only applies when backgroundStyle is ROUNDED).")
    var backgroundRadius: Int = 4,

    @Comment("Text scale multiplier for all HUD elements.")
    var textScale: Float = 1.0f,
)

