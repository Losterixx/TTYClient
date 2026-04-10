package dev.losterixx.ttyclient.client.modules

interface ClientModule {
    val id: String
    val configPath: String

    fun load()
    fun register() {}
}

