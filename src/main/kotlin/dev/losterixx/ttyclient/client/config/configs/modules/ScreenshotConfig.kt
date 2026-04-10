package dev.losterixx.ttyclient.client.config.configs.modules

import dev.losterixx.ttyclient.client.config.Comment

data class ScreenshotConfig(
    @Comment("Enable the Screenshot module.", "When enabled, screenshots show a clickable chat message with copy/open/upload actions.")
    var enabled: Boolean = true,

    @Comment(
        "Screenshot output quality/resolution.",
        "Options: \"native\" (default), \"720p\", \"1080p\", \"2k\", \"4k\", \"8k\"",
    )
    var quality: String = "native",
)

