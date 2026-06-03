package dev.losterixx.ttyclient.client.hud.elements

import dev.losterixx.ttyclient.client.hud.HudAnchor
import dev.losterixx.ttyclient.client.config.configs.hud.DateHudConfig
import dev.losterixx.ttyclient.client.hud.HudElement
import java.time.LocalDate

class DateElement(private val config: () -> DateHudConfig) : HudElement() {

    override val enabled: Boolean get() = config().enabled
    override val anchor: HudAnchor get() = config().anchor
    override val offsetX: Int get() = config().x
    override val offsetY: Int get() = config().y

    override fun resolveText(): String {
        val now = LocalDate.now()
        return config().text
            .replace("{D}", String.format("%02d", now.dayOfMonth), ignoreCase = true)
            .replace("{M}", String.format("%02d", now.monthValue), ignoreCase = true)
            .replace("{Y}", now.year.toString(), ignoreCase = true)
    }

    override fun previewText(): String =
        config().text
            .replace("{D}", "01", ignoreCase = true)
            .replace("{M}", "01", ignoreCase = true)
            .replace("{Y}", "1970", ignoreCase = true)

    override fun setOffset(x: Int, y: Int) {
        config().x = x
        config().y = y
    }

    override fun setAnchor(anchor: HudAnchor) {
        config().anchor = anchor
    }
}


