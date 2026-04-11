package dev.losterixx.ttyclient.client.config.configs.modules

import dev.losterixx.ttyclient.client.config.Comment

data class NotificationsConfig(
    @Comment("Enable or disable the notification overlay.")
    var enabled: Boolean = true,

    @Comment(
        "Animation style for notifications.",
        "Options: \"SLIDE\" (slides in from the side), \"FADE\" (fades in/out), \"NONE\" (instant)"
    )
    var animation: String = "SLIDE",

    @Comment("Play a sound when a notification appears.")
    var sound: Boolean = true,

    @Comment(
        "Default display duration for notifications in milliseconds.",
        "Default is 4000 (4 seconds)."
    )
    var durationMs: Long = 4000,

    @Comment("Maximum number of notifications shown on screen at once.")
    var maxVisible: Int = 3,

    @Comment(
        "Corner to display notifications in.",
        "Options: \"TOP_RIGHT\", \"TOP_LEFT\", \"BOTTOM_RIGHT\", \"BOTTOM_LEFT\""
    )
    var position: String = "TOP_RIGHT",

    @Comment("Width of each notification panel in pixels.")
    var width: Int = 160,

    @Comment("Horizontal margin from the screen edge in pixels.")
    var marginX: Int = 4,

    @Comment("Vertical margin from the screen edge in pixels.")
    var marginY: Int = 4,

    @Comment("Gap between stacked notifications in pixels.")
    var gap: Int = 2,
)
