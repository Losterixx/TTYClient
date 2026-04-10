package dev.losterixx.ttyclient.client.commandsys.commands

import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.commandsys.Command
import dev.losterixx.ttyclient.client.commandsys.CommandContext
import dev.losterixx.ttyclient.client.commandsys.CommandManager

class HelpCommand : Command(
    name = "help",
    aliases = listOf("?"),
    description = "Lists all available commands"
) {
    override fun execute(ctx: CommandContext) {
        CommandManager.reply("${MainClient.PREFIX}§fhelp")
        CommandManager.getAll()
            .sortedBy { it.name }
            .forEach { cmd ->
                val aliases = if (cmd.aliases.isNotEmpty()) {
                    " §8(${cmd.aliases.joinToString(", ") { "${CommandManager.prefix}$it" }})"
                } else ""

                CommandManager.reply(" §7▸ §c${CommandManager.prefix}${cmd.name} §8- §7${cmd.description}$aliases")
            }
        CommandManager.reply(" §8Tip: §7flags use §f--flag §7or §f-f §7syntax, e.g. §f:info --version")
        CommandManager.reply(" §8Wiki: §7https://github.com/Losterixx/TTYClient/wiki/")
    }
}

