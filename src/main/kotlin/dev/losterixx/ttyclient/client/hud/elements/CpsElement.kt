package dev.losterixx.ttyclient.client.hud.elements

import dev.losterixx.ttyclient.client.hud.HudAnchor
import dev.losterixx.ttyclient.client.config.configs.hud.CpsHudConfig
import dev.losterixx.ttyclient.client.hud.HudElement

class CpsElement(
    private val config: () -> CpsHudConfig,
    private val leftCps: () -> Int,
    private val rightCps: () -> Int,
) : HudElement() {

    override val enabled: Boolean get() = config().enabled
    override val anchor: HudAnchor get() = config().anchor
    override val offsetX: Int get() = config().x
    override val offsetY: Int get() = config().y

    override fun resolveText(): String =
        config().text
            .replace("{L}", leftCps().toString(), ignoreCase = true)
            .replace("{R}", rightCps().toString(), ignoreCase = true)

    override fun previewText(): String =
        config().text
            .replace("{L}", "5", ignoreCase = true)
            .replace("{R}", "3", ignoreCase = true)

    override fun setOffset(x: Int, y: Int) {
        config().x = x
        config().y = y
    }

    override fun setAnchor(anchor: HudAnchor) {
        config().anchor = anchor
    }
}


