package dev.losterixx.ttyclient.client.config.configs.modules

import dev.losterixx.ttyclient.client.config.Comment

data class ZoomConfig(
    @Comment("Enable or disable the Zoom module.", "Use ':module --toggle zoom' to change this.")
    var enabled: Boolean = true,

    @Comment("How much to zoom in. Smaller value = more zoom.", "Default is 0.15 (roughly 6x optical zoom).")
    var zoomStrength: Float = 0.15f,

    @Comment("Enable a smooth zoom-in / zoom-out animation.")
    var zoomAnimation: Boolean = true,

    @Comment("Speed of the zoom animation (lerp factor per frame).", "Higher = faster. Default is 0.1.")
    var zoomAnimationSpeed: Float = 0.1f,

    @Comment("Allow the scroll wheel to fine-tune zoom level while zoomed.")
    var scrollable: Boolean = true,

    @Comment("How much the zoom level changes per scroll tick.", "Default is 0.05.")
    var scrollStep: Float = 0.05f,

    @Comment("Hold mode: when true, zoom is active while the key is held and stops on release.",
        "When false, each key press toggles zoom on/off.")
    var hold: Boolean = true,
)

