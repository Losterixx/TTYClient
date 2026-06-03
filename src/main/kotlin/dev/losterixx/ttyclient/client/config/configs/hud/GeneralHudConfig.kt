package dev.losterixx.ttyclient.client.config.configs.hud

import kotlinx.serialization.Serializable

@Serializable
data class GeneralHudConfig(
    var enabled: Boolean = true,
    var hideOnF1: Boolean = true,
    var hideOnF3: Boolean = false,
    var hideOnChat: Boolean = false,
    var hideOnESC: Boolean = false,
    var backgroundEnabled: Boolean = true,
    var roundedCorners: Boolean = true,
    var textScale: Float = 1.0f,
)

