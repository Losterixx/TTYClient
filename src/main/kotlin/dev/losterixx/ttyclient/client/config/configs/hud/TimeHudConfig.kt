package dev.losterixx.ttyclient.client.config.configs.hud

import dev.losterixx.ttyclient.client.hud.HudAnchor
import kotlinx.serialization.Serializable

@Serializable
data class TimeHudConfig(
    var enabled: Boolean = true,
    var anchor: HudAnchor = HudAnchor.BOTTOM_RIGHT,
    var text: String = "{H}:{M}:{S}",
    var x: Int = 7,
    var y: Int = 17,
)

