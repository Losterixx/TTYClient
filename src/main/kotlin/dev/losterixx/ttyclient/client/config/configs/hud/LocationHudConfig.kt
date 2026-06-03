package dev.losterixx.ttyclient.client.config.configs.hud

import dev.losterixx.ttyclient.client.hud.HudAnchor
import kotlinx.serialization.Serializable

@Serializable
data class LocationHudConfig(
    var enabled: Boolean = true,
    var anchor: HudAnchor = HudAnchor.BOTTOM_LEFT,
    var text: String = "{X}, {Y}, {Z} ({D})",
    var x: Int = 6,
    var y: Int = 3,
)

