package dev.losterixx.ttyclient.client.commandsys.commands

import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.commandsys.Command
import dev.losterixx.ttyclient.client.commandsys.CommandContext
import dev.losterixx.ttyclient.client.commandsys.CommandManager
import dev.losterixx.ttyclient.client.modules.screenshots.ScreenshotManager
import java.io.File

class ScreenshotCommand : Command(
    name = "screenshot",
    aliases = listOf("ss"),
    description = "Screenshot actions (copy, upload)",
    usage = ":screenshot <copy/upload> <path>"
) {
    override fun execute(ctx: CommandContext) {
        val subcommand = ctx.args.getOrNull(0)?.lowercase()

        when (subcommand) {
            "copy" -> {
                val path = extractPath(ctx, "copy")

                if (path.isEmpty()) {
                    CommandManager.reply("${MainClient.PREFIX}§cUsage: $usage")
                    return
                }

                ScreenshotManager.copyImageToClipboard(path)
            }

            "upload" -> {
                val path = extractPath(ctx, "upload")
                if (path.isEmpty()) {
                    CommandManager.reply("${MainClient.PREFIX}§cUsage: $usage")
                    return
                }

                val file = File(path)
                if (!file.exists()) {
                    CommandManager.reply("${MainClient.PREFIX}§cFile not found: §f$path")
                    return
                }

                ScreenshotManager.uploadImage(file)
            }

            else -> CommandManager.reply("${MainClient.PREFIX}§cUsage: $usage")
        }
    }

    private fun extractPath(ctx: CommandContext, subcommand: String): String {
        val afterCmd = ctx.raw
            .substringAfter(ctx.name)
            .trim()
        val afterSub = if (afterCmd.lowercase().startsWith(subcommand)) {
            afterCmd.substring(subcommand.length).trim()
        } else {
            afterCmd.trim()
        }

        return afterSub.removeSurrounding("\"").removeSurrounding("'")
    }
}


