package dev.losterixx.ttyclient.client.commandsys.commands

import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.commandsys.Command
import dev.losterixx.ttyclient.client.commandsys.CommandContext
import dev.losterixx.ttyclient.client.commandsys.CommandManager
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft

class McFetchCommand : Command(
    name = "mcfetch",
    aliases = listOf("mcf"),
    description = "Shows game information"
) {
    override fun execute(ctx: CommandContext) {
        val client = Minecraft.getInstance()

        val gameVersion: String = FabricLoader.getInstance()
            .getModContainer("minecraft")
            .map { it.metadata.version.friendlyString }
            .orElse("???")

        val playerName = client.player?.name?.string ?: "???"
        val fps = "${client.fps} fps"

        val rt = Runtime.getRuntime()
        val usedMb = (rt.totalMemory() - rt.freeMemory()) / 1_048_576
        val maxMb = rt.maxMemory() / 1_048_576
        val memory = "$usedMb / $maxMb MB"

        val serverAddress: String = when {
            client.hasSingleplayerServer() -> "Singleplayer"
            client.currentServer != null -> client.currentServer!!.ip
            else -> "???"
        }

        val dimension = client.level
            ?.dimension()?.toString()
            ?.substringAfterLast("/")?.trim()
            ?.removeSuffix("]")
            ?.removePrefix("minecraft:") ?: "???"

        val position = client.player?.blockPosition()
            ?.let { "${it.x}, ${it.y}, ${it.z}" } ?: "???"

        val gameMode = client.gameMode
            ?.playerMode?.getName()?.lowercase()
            ?.replaceFirstChar { it.uppercase() } ?: "???"

        CommandManager.reply("${MainClient.PREFIX}§fmcfetch")
        CommandManager.replyLine("Player", playerName)
        CommandManager.replyLine("Version", gameVersion)
        CommandManager.replyLine("FPS", fps)
        CommandManager.replyLine("Memory", memory)
        CommandManager.replyLine("Server", serverAddress)
        CommandManager.replyLine("Dimension", dimension)
        CommandManager.replyLine("Position", position)
        CommandManager.replyLine("Game Mode", gameMode)
    }
}
