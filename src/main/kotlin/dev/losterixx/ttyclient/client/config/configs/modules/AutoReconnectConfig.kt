package dev.losterixx.ttyclient.client.config.configs.modules

import dev.losterixx.ttyclient.client.config.Comment

data class AutoReconnectConfig(
    @Comment("Enable or disable the AutoReconnect module.", "Use ';module --toggle autoreconnect' to change this.")
    var enabled: Boolean = false,

    @Comment(
        "Seconds to wait before automatically reconnecting after a disconnect.",
        "Set to 0 to reconnect instantly. Default is 5."
    )
    var delaySeconds: Int = 5,
)

