package dev.losterixx.ttyclient.client.config.configs.hud

import dev.losterixx.ttyclient.client.hud.HudAnchor
import kotlinx.serialization.Serializable

@Serializable
data class DateHudConfig(
    var enabled: Boolean = true,
    var anchor: HudAnchor = HudAnchor.BOTTOM_RIGHT,
    var text: String = "{D}.{M}.{Y}",
    var x: Int = 6,
    var y: Int = 3,
)

