package dev.losterixx.ttyclient.client.config.configs

import dev.losterixx.ttyclient.client.config.Comment

data class GeneralConfig(
    @Comment("Prefix character(s) used to trigger client commands.", "Default is \";\". The \"/\" character should not be used.")
    var commandPrefix: String = ";",

    var keyBoard: KeyBoardConfig = KeyBoardConfig(),
)

data class KeyBoardConfig(
    var isQwertz: Boolean = true,
)

