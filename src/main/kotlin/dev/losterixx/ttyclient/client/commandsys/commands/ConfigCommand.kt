package dev.losterixx.ttyclient.client.commandsys.commands

import dev.losterixx.ttyclient.client.commandsys.Command
import dev.losterixx.ttyclient.client.commandsys.CommandContext
import dev.losterixx.ttyclient.client.commandsys.CommandManager
import dev.losterixx.ttyclient.client.screens.ConfigEditorScreen

class ConfigCommand : Command(
    name = "config",
    aliases = listOf("cfg"),
    description = "Open the config editor screen"
) {
    override fun execute(ctx: CommandContext) {
        CommandManager.client.execute {
            CommandManager.client.setScreen(ConfigEditorScreen())
        }
    }
}

