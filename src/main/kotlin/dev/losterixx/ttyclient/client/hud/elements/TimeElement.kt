package dev.losterixx.ttyclient.client.hud.elements

import dev.losterixx.ttyclient.client.hud.HudAnchor
import dev.losterixx.ttyclient.client.config.configs.hud.TimeHudConfig
import dev.losterixx.ttyclient.client.hud.HudElement
import java.time.LocalTime

class TimeElement(private val config: () -> TimeHudConfig) : HudElement() {

    override val enabled: Boolean get() = config().enabled
    override val anchor: HudAnchor get() = config().anchor
    override val offsetX: Int get() = config().x
    override val offsetY: Int get() = config().y

    override fun resolveText(): String {
        val now = LocalTime.now()
        return config().text
            .replace("{H}", String.format("%02d", now.hour), ignoreCase = true)
            .replace("{M}", String.format("%02d", now.minute), ignoreCase = true)
            .replace("{S}", String.format("%02d", now.second), ignoreCase = true)
    }

    override fun previewText(): String =
        config().text
            .replace("{H}", "12", ignoreCase = true)
            .replace("{M}", "34", ignoreCase = true)
            .replace("{S}", "56", ignoreCase = true)

    override fun setOffset(x: Int, y: Int) {
        config().x = x
        config().y = y
    }

    override fun setAnchor(anchor: HudAnchor) {
        config().anchor = anchor
    }
}


