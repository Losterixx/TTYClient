package dev.losterixx.ttyclient.client.config.configs.modules

import dev.losterixx.ttyclient.client.config.Comment

data class FullbrightConfig(
    @Comment("Enable or disable the Fullbright module.", "Use ';module --toggle fullbright' to change this.")
    var enabled: Boolean = true,

    @Comment(
        "Fullbright mode:",
        "  \"gamma\" - boosts the game gamma value (works everywhere, no visual side-effects, but may cause issues with some shaders)",
        "  \"nightvision\" - applies a permanent Night Vision effect (shows stars at night, should work with shaders)"
    )
    var mode: String = "gamma",

    @Comment(
        "Gamma value used in 'gamma' mode.",
        "Higher values = brighter. Default is 10.0 (effectively fullbright)."
    )
    var gamma: Float = 10.0f,

    @Comment("Whether fullbright is currently active (persisted across sessions).")
    var active: Boolean = false,
)

