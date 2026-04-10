package dev.losterixx.ttyclient.client.commandsys

abstract class Command(
    val name: String,
    val aliases: List<String> = emptyList(),
    val description: String = "",
    val usage: String = ":$name"
) {
    abstract fun execute(ctx: CommandContext)

    open fun getSupportedFlags(): List<String> = emptyList()

    open fun getArgSuggestions(parts: List<String>, partial: String): List<String> = emptyList()
}


