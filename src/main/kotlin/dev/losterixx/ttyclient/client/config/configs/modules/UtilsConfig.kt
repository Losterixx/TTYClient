package dev.losterixx.ttyclient.client.config.configs.modules

import dev.losterixx.ttyclient.client.config.Comment

data class DurabilityWarningConfig(
    @Comment("Enable the durability warning.")
    var enabled: Boolean = true,

    @Comment(
        "Show warning when a held item's durability falls below this percentage (0–100).",
        "Default: 20 -> warn when less than 20% durability remains."
    )
    var threshold: Int = 20,

    @Comment("Play a pling sound with each warning flash.")
    var soundEnabled: Boolean = true,
)

data class UtilsConfig(
    @Comment("Enable or disable the Utils module.", "Use ';module --toggle utils' to change this.")
    var enabled: Boolean = true,

    @Comment(
        "Durability warning: flashes a yellow/red action-bar message and plays a pling",
        "every 0.5 s when any held item's durability is critically low."
    )
    var durabilityWarning: DurabilityWarningConfig = DurabilityWarningConfig(),
)
