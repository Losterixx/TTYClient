package dev.losterixx.ttyclient.client.commandsys

import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.commandsys.commands.ConfigCommand
import dev.losterixx.ttyclient.client.commandsys.commands.DebugCommand
import dev.losterixx.ttyclient.client.commandsys.commands.HelpCommand
import dev.losterixx.ttyclient.client.commandsys.commands.HudCommand
import dev.losterixx.ttyclient.client.commandsys.commands.InfoCommand
import dev.losterixx.ttyclient.client.commandsys.commands.McFetchCommand
import dev.losterixx.ttyclient.client.commandsys.commands.ModuleCommand
import dev.losterixx.ttyclient.client.commandsys.commands.ScreenshotCommand
import dev.losterixx.ttyclient.client.config.ConfigManager
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor

object CommandManager {

    private val commands = mutableMapOf<String, Command>()
    val client: Minecraft get() = Minecraft.getInstance()

    val prefix: String get() = ConfigManager.general.commandPrefix

    fun init() {
        register(McFetchCommand())
        register(InfoCommand())
        register(HelpCommand())
        register(ConfigCommand())
        register(ModuleCommand())
        register(ScreenshotCommand())
        register(DebugCommand())
        register(HudCommand())

        ClientSendMessageEvents.ALLOW_CHAT.register { message ->
            if (message.startsWith(prefix)) {
                val input = message.removePrefix(prefix).trim()

                if (input.isNotEmpty()) {
                    val ctx = parse(input)
                    val cmd = commands[ctx.name.lowercase()]

                    if (cmd != null) {
                        try {
                            cmd.execute(ctx)
                        } catch (e: Exception) {
                            reply("${MainClient.PREFIX}§cError in command §f${ctx.name}§c: §7${e.message}")
                        }
                    } else {
                        reply("${MainClient.PREFIX}§cUnknown command: §f${ctx.name}§c. §7Type §f${prefix}help §7for available commands.")
                    }
                }
                return@register false
            } else {
                return@register true
            }
        }
    }

    fun register(command: Command) {
        commands[command.name.lowercase()] = command
        command.aliases.forEach { commands[it.lowercase()] = command }
    }

    fun getAll(): List<Command> = commands.values.distinct()

    fun getCommand(name: String): Command? = commands[name.lowercase()]

    fun parse(input: String): CommandContext {
        val tokens = input.trim().split("\\s+".toRegex())
        val name = tokens.firstOrNull() ?: ""
        val args = mutableListOf<String>()
        val flags = mutableSetOf<String>()
        val shortFlags = mutableSetOf<Char>()

        for (token in tokens.drop(1)) {
            when {
                token.startsWith("--") && token.length > 2 -> flags.add(token.removePrefix("--"))
                token.startsWith("-") && token.length > 1 -> token.removePrefix("-").forEach { shortFlags.add(it) }
                else -> args.add(token)
            }
        }

        return CommandContext(input, name, args, flags, shortFlags)
    }

    fun reply(vararg lines: String) {
        val hud = client.gui

        lines.forEach { line ->
            hud.chat.addClientSystemMessage(buildLine(line))
        }
    }

    private fun buildLine(line: String): Component {
        if (line.startsWith(MainClient.PREFIX)) {
            val rest = line.removePrefix(MainClient.PREFIX)

            return Component.literal("·» ")
                .withStyle { it.withColor(TextColor.fromRgb(0xF33636)) }
                .append(Component.literal("§7$rest"))
        }

        return Component.literal(line)
    }

    fun replyLine(label: String, value: String) {
        reply(" §7▸ §c$label: §f$value")
    }
}
