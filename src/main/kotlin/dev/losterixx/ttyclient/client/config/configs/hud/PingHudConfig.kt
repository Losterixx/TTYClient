package dev.losterixx.ttyclient.client.config.configs.hud

import dev.losterixx.ttyclient.client.config.Comment
import dev.losterixx.ttyclient.client.hud.HudAnchor

data class PingHudConfig(
    @Comment("Enable or disable ping display.")
    var enabled: Boolean = true,
    
    @Comment("Anchor point: TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER.")
    var anchor: HudAnchor = HudAnchor.TOP_LEFT,
    
    @Comment("Text template. Use {PING} placeholder for the ping value.")
    var text: String = "{PING}ms",
    
    @Comment("Horizontal offset from anchor point (in pixels).")
    var x: Int = 6,
    
    @Comment("Vertical offset from anchor point (in pixels).")
    var y: Int = 18,
)

