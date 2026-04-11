package dev.losterixx.ttyclient.client.modules

enum class ModuleCategory {
    RENDER, VISUAL, CHAT, UTILS, MISC
}

interface ClientModule {
    val id: String
    val configPath: String

    val displayName: String get() = id
    val description: String get() = ""

    val category: ModuleCategory get() = ModuleCategory.MISC

    fun load()
    fun register() {}

    fun onEnable() {}
    fun onDisable() {}
}
