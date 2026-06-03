package dev.losterixx.ttyclient.client.hud.elements

import dev.losterixx.ttyclient.client.hud.HudAnchor
import dev.losterixx.ttyclient.client.config.configs.hud.LocationHudConfig
import dev.losterixx.ttyclient.client.hud.HudElement
import net.minecraft.client.Minecraft

class LocationElement(private val config: () -> LocationHudConfig) : HudElement() {

    override val enabled: Boolean get() = config().enabled
    override val anchor: HudAnchor get() = config().anchor
    override val offsetX: Int get() = config().x
    override val offsetY: Int get() = config().y

    override fun resolveText(): String {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return previewText()

        val x = player.blockX
        val y = player.blockY
        val z = player.blockZ
        val direction = player.direction.getName()

        return config().text
            .replace("{X}", x.toString(), ignoreCase = true)
            .replace("{Y}", y.toString(), ignoreCase = true)
            .replace("{Z}", z.toString(), ignoreCase = true)
            .replace("{D}", direction.uppercase(), ignoreCase = true)
    }

    override fun previewText(): String =
        config().text
            .replace("{X}", "123", ignoreCase = true)
            .replace("{Y}", "64", ignoreCase = true)
            .replace("{Z}", "-456", ignoreCase = true)
            .replace("{D}", "North", ignoreCase = true)

    override fun setOffset(x: Int, y: Int) {
        config().x = x
        config().y = y
    }

    override fun setAnchor(anchor: HudAnchor) {
        config().anchor = anchor
    }
}


