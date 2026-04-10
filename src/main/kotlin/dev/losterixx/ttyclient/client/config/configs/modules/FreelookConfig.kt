package dev.losterixx.ttyclient.client.config.configs.modules

import dev.losterixx.ttyclient.client.config.Comment

data class FreelookConfig(
    @Comment("Enable or disable the Freelook module.", "Use ';module --toggle freelook' to change this.")
    var enabled: Boolean = true,

    @Comment("Invert the pitch (up/down) camera movement while in freelook.")
    var invertPitch: Boolean = false,

    @Comment("Invert the yaw (left/right) camera movement while in freelook.")
    var invertYaw: Boolean = false,

    @Comment("Allow the scroll wheel to adjust camera distance while in freelook.")
    var scrollable: Boolean = true,

    @Comment("Mouse sensitivity multiplier while in freelook.", "Default is 0.15.")
    var sensitivity: Float = 0.15f,

    @Comment("How much the camera distance changes per scroll tick while in freelook.", "Default is 1.5.")
    var scrollStep: Float = 1.5f,

    @Comment("Show your own nametag while in freelook (third-person).")
    var showOwnNametag: Boolean = true,

    @Comment("Hold mode: when true, the module activates while the key is held and", "deactivates on release. When false, each key press toggles the state.")
    var hold: Boolean = true,

    @Comment("Maximum camera distance when using the scroll wheel in freelook.")
    var maxDistance: Float = 15.0f,
)