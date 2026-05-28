package dev.losterixx.ttyclient.client.config.configs.modules

import dev.losterixx.ttyclient.client.config.Comment

data class PerformanceConfig(
    @Comment(
        "Enable or disable HUD throttling.",
        "When enabled, the HUD is rendered to a cached texture and reused between frames.",
        "Only active while in-game with no screen open (ESC/inventory etc. always render at full speed)."
    )
    var hudThrottleEnabled: Boolean = false,

    @Comment(
        "How often the HUD texture is refreshed, in milliseconds.",
        "Lower = more responsive, higher = more FPS savings.",
        "Examples: 50ms = 20 updates/s,  100ms = 10 updates/s",
        "Recommended: 50 for a good balance. Set to 0 to update every frame (same as disabled)."
    )
    var hudThrottleIntervalMs: Long = 50L,
)
