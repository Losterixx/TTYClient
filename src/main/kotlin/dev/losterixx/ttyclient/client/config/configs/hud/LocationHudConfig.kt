package dev.losterixx.ttyclient.client.config.configs.hud

import dev.losterixx.ttyclient.client.config.Comment
import dev.losterixx.ttyclient.client.hud.HudAnchor

data class LocationHudConfig(
    @Comment("Enable or disable location display.")
    var enabled: Boolean = true,
    
    @Comment("Anchor point: TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER.")
    var anchor: HudAnchor = HudAnchor.BOTTOM_LEFT,
    
    @Comment("Text template. Use {X}, {Y}, {Z} for coordinates, {D} for direction.")
    var text: String = "{X}, {Y}, {Z} ({D})",
    
    @Comment("Horizontal offset from anchor point (in pixels).")
    var x: Int = 6,
    
    @Comment("Vertical offset from anchor point (in pixels).")
    var y: Int = 3,
)

