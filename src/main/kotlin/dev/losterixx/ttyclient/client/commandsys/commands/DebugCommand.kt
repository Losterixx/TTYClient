package dev.losterixx.ttyclient.client.commandsys.commands

import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.commandsys.Command
import dev.losterixx.ttyclient.client.commandsys.CommandContext
import dev.losterixx.ttyclient.client.commandsys.CommandManager
import dev.losterixx.ttyclient.client.modules.debug.KeyVisualizerManager

class DebugCommand : Command(
    name = "debug",
    description = "Debug utilities for development",
    usage = "${CommandManager.prefix}debug <--toggle|--trigger> <target> [args]"
) {
    override fun getSupportedFlags(): List<String> = listOf("toggle", "trigger")

    override fun execute(ctx: CommandContext) {
        when {
            ctx.hasFlag("toggle", 'T') -> {
                val target = ctx.args.getOrNull(0)?.lowercase() ?: return showUsage()
                handleToggle(target)
            }

            ctx.hasFlag("trigger", 't') -> {
                val target = ctx.args.getOrNull(0)?.lowercase() ?: return showUsage()
                handleTrigger(target, ctx.args.drop(1))
            }

            else -> showUsage()
        }
    }

    private fun handleToggle(target: String) {
        when (target) {
            "keys" -> {
                KeyVisualizerManager.keysEnabled = !KeyVisualizerManager.keysEnabled
                val state = if (KeyVisualizerManager.keysEnabled) "§aenabled" else "§cdisabled"
                CommandManager.reply("${MainClient.PREFIX}Key Visualizer $state§7.")
            }

            else -> CommandManager.reply("${MainClient.PREFIX}§cUnknown toggle target: §f$target")
        }
    }

    private fun handleTrigger(target: String, args: List<String>) {
        when (target) {
            "spam" -> {
                val n = args.getOrNull(0)?.toIntOrNull()
                if (n == null || n < 1) {
                    CommandManager.reply("${MainClient.PREFIX}§cUsage: §f${CommandManager.prefix}debug --trigger spam <n>")
                    return
                }

                for (i in 1..n) {
                    CommandManager.reply("Spam message $i")
                }

                CommandManager.reply("${MainClient.PREFIX}Sent §f$n§7 spam message(s).")
            }

            else -> CommandManager.reply("${MainClient.PREFIX}§cUnknown trigger target: §f$target")
        }
    }

    private fun showUsage() {
        CommandManager.reply(
            "${MainClient.PREFIX}§fdebug §8- §7Debug utilities",
            "",
            " §c--toggle §8(-T) §7<target>",
            "   §8▸ §fkeys §7- toggle key press visualizer",
            "",
            " §c--trigger §8(-t) §7<target> [args]",
            "   §8▸ §fspam <num>  §7- send n messages §8(client-side)",
            ""
        )
    }
}
