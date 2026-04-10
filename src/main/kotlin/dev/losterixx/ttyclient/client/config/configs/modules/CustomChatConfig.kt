package dev.losterixx.ttyclient.client.config.configs.modules

import dev.losterixx.ttyclient.client.config.Comment

data class CustomChatConfig(
    @Comment("Enable or disable the Custom Chat module.", "Use ';module --toggle customchat' to change this.")
    var enabled: Boolean = true,

    @Comment("Custom chat width in pixels. Default is 320.")
    var width: Int = 320,

    @Comment("Custom chat height (when chat is open/focused) in pixels. Default is 180.")
    var height: Int = 180,

    @Comment("When true, keeps up to 999999 chat messages instead of Minecraft's default 100.")
    var infiniteChatHistory: Boolean = false,

    @Comment(
        "When true switching servers will NOT erase chat history.",
        "Only affects history clearing, not current visible chat."
    )
    var keepChatHistory: Boolean = false,
)


