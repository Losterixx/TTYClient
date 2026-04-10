package dev.losterixx.ttyclient.client.config.configs.modules

import dev.losterixx.ttyclient.client.config.Comment

data class CrosshairConfig(
    @Comment("Enable or disable the Custom Crosshair module.", "Use ';module --toggle crosshair' to change this.")
    var enabled: Boolean = true,

    @Comment(
        "Color of the crosshair pixels.",
        "Use hex format: #RRGGBB (opaque) or #AARRGGBB (with alpha).",
        "Default is #E2E2E2"
    )
    var color: Int = 0xFFE2E2E2.toInt(),

    @Comment("Show the vanilla attack cooldown indicator below the custom crosshair.")
    var showAttackIndicator: Boolean = true,

    @Comment(
        "15x15 crosshair pixel grid. Each entry is a row of exactly 15 characters (spaces ignored).",
        "'X' = filled pixel, '0' = transparent",
        "Must have exactly 15 rows."
    )
    var grid: List<String> = DEFAULT_GRID
) {
    companion object {
        val DEFAULT_GRID: List<String> = listOf(
            "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0",
            "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0",
            "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0",
            "0 0 0 0 0 0 0 X 0 0 0 0 0 0 0",
            "0 0 0 0 0 0 0 X 0 0 0 0 0 0 0",
            "0 0 0 0 0 0 0 X 0 0 0 0 0 0 0",
            "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0",
            "0 0 0 X X X 0 X 0 X X X 0 0 0",
            "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0",
            "0 0 0 0 0 0 0 X 0 0 0 0 0 0 0",
            "0 0 0 0 0 0 0 X 0 0 0 0 0 0 0",
            "0 0 0 0 0 0 0 X 0 0 0 0 0 0 0",
            "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0",
            "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0",
            "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0"
        )
    }
}
