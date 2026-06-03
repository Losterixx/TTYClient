package dev.losterixx.ttyclient.client.config.configs.hud

import dev.losterixx.ttyclient.client.hud.HudAnchor
import kotlinx.serialization.Serializable

@Serializable
data class FpsHudConfig(
    var enabled: Boolean = true,
    var anchor: HudAnchor = HudAnchor.TOP_LEFT,
    var text: String = "{FPS} ғᴘs",
    var x: Int = 6,
    var y: Int = 4,
)

