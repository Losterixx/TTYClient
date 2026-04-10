package dev.losterixx.ttyclient.client.commandsys.commands

import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.commandsys.Command
import dev.losterixx.ttyclient.client.commandsys.CommandContext
import dev.losterixx.ttyclient.client.commandsys.CommandManager
import net.fabricmc.loader.api.FabricLoader

class InfoCommand : Command(
    name = "info",
    description = "Shows TTYClient information",
    usage = ":info [--name|-n] [--version|-v] [--author|-a] [--description|-d] [--license|-l]"
) {
    override fun getSupportedFlags(): List<String> = listOf("name", "version", "author", "description", "license")

    override fun execute(ctx: CommandContext) {
        val container = FabricLoader.getInstance()
            .getModContainer(MainClient.MOD_ID).orElse(null)

        val modName = "TTYClient"
        val version = MainClient.VERSION
        val author = container?.metadata?.authors
            ?.joinToString(", ") { it.name }
            ?: "???"
        val description = container?.metadata?.description
            ?: "???"
        val license = container?.metadata?.license?.joinToString(", ") ?: "???"

        val wantName = ctx.hasFlag("name", 'n')
        val wantVer = ctx.hasFlag("version", 'v')
        val wantAuth = ctx.hasFlag("author", 'a')
        val wantDesc = ctx.hasFlag("description", 'd')
        val wantLicense = ctx.hasFlag("license", 'l')
        val wantAll = !ctx.hasAnyFlag()

        if (wantAll) {
            CommandManager.reply("${MainClient.PREFIX}§finfo")
            CommandManager.replyLine("Name", modName)
            CommandManager.replyLine("Version", version)
            CommandManager.replyLine("Author", author)
            CommandManager.replyLine("Description", description)
            CommandManager.replyLine("License", license)
        } else {
            if (wantName) CommandManager.reply("${MainClient.PREFIX}§cName§8: §f$modName")
            if (wantVer) CommandManager.reply("${MainClient.PREFIX}§cVersion§8: §f$version")
            if (wantAuth) CommandManager.reply("${MainClient.PREFIX}§cAuthor§8: §f$author")
            if (wantDesc) CommandManager.reply("${MainClient.PREFIX}§cDescription§8: §f$description")
            if (wantLicense) CommandManager.reply("${MainClient.PREFIX}§cLicense§8: §f$license")
        }
    }
}
