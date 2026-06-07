package dev.losterixx.ttyclient.client.config.configs.hud

import dev.losterixx.ttyclient.client.config.Comment
import dev.losterixx.ttyclient.client.hud.HudAnchor

data class DateHudConfig(
    @Comment("Enable or disable date display.")
    var enabled: Boolean = true,

    @Comment("Anchor point: TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER.")
    var anchor: HudAnchor = HudAnchor.BOTTOM_RIGHT,

    @Comment("Text template. Use {D} for day, {M} for month, {Y} for year.")
    var text: String = "{D}.{M}.{Y}",

    @Comment("Horizontal offset from anchor point (in pixels).")
    var x: Int = 6,

    @Comment("Vertical offset from anchor point (in pixels).")
    var y: Int = 3,
)

