package dev.losterixx.ttyclient.client.hud.elements

import dev.losterixx.ttyclient.client.hud.HudAnchor
import dev.losterixx.ttyclient.client.config.configs.hud.PingHudConfig
import dev.losterixx.ttyclient.client.hud.HudElement
import net.minecraft.client.Minecraft

class PingElement(private val config: () -> PingHudConfig) : HudElement() {

    override val enabled: Boolean get() = config().enabled
    override val anchor: HudAnchor get() = config().anchor
    override val offsetX: Int get() = config().x
    override val offsetY: Int get() = config().y

    override fun resolveText(): String {
        val ping = getPing()
        return config().text.replace("{PING}", ping.toString(), ignoreCase = true)
    }

    override fun previewText(): String =
        config().text.replace("{PING}", "30", ignoreCase = true)

    override fun setOffset(x: Int, y: Int) {
        config().x = x
        config().y = y
    }

    override fun setAnchor(anchor: HudAnchor) {
        config().anchor = anchor
    }

    private fun getPing(): Int {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return 0
        val connection = mc.connection ?: return 0
        val playerInfo = connection.getPlayerInfo(player.uuid) ?: return 0
        return playerInfo.latency
    }
}



