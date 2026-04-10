package dev.losterixx.ttyclient.client.commandsys.commands

import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.commandsys.Command
import dev.losterixx.ttyclient.client.commandsys.CommandContext
import dev.losterixx.ttyclient.client.commandsys.CommandManager
import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.modules.autoreconnect.ReconnectManager
import dev.losterixx.ttyclient.client.modules.customchat.ChatManager
import dev.losterixx.ttyclient.client.modules.freelook.FreelookManager
import dev.losterixx.ttyclient.client.modules.fullbright.FullbrightManager
import dev.losterixx.ttyclient.client.modules.zoom.ZoomManager

class ModuleCommand : Command(
    name = "module",
    aliases = listOf("mod"),
    description = "Control client modules",
    usage = ":module <--trigger/--toggle> <module>"
) {
    companion object {
        val MODULES = listOf("freelook", "zoom", "fullbright", "customchat", "autoreconnect")
    }

    override fun getSupportedFlags(): List<String> = listOf("trigger", "toggle")

    override fun getArgSuggestions(parts: List<String>, partial: String): List<String> {
        val hasAction = parts.any { it in listOf("--trigger", "-t", "--toggle", "-T") }
        return if (hasAction) MODULES.filter { it.startsWith(partial.lowercase()) } else emptyList()
    }

    override fun execute(ctx: CommandContext) {
        when {
            ctx.hasFlag("trigger", 't') -> {
                val module = ctx.args.getOrNull(0)?.lowercase() ?: return showUsage()
                handleTrigger(module)
            }

            ctx.hasFlag("toggle", 'T') -> {
                val module = ctx.args.getOrNull(0)?.lowercase() ?: return showUsage()
                handleToggle(module)
            }

            else -> showUsage()
        }
    }

    private fun showUsage() {
        CommandManager.reply(
            "${MainClient.PREFIX}§cUsage: §f${usage}",
            " §7▸ §c--trigger §8(-t)§8 - §7trigger a module",
            " §7▸ §c--toggle §8(-T)§8 - §7enable/disable a module",
            " §7Modules: §f${MODULES.joinToString(", ")}"
        )
    }

    private fun handleTrigger(name: String) {
        when (name) {
            "freelook" -> {
                if (!FreelookManager.config.enabled) {
                    CommandManager.reply("${MainClient.PREFIX}§cFreelook is disabled. Use §f:module --toggle freelook §cto enable it.")
                    return
                }

                FreelookManager.trigger()
            }

            "zoom" -> {
                if (!ZoomManager.config.enabled) {
                    CommandManager.reply("${MainClient.PREFIX}§cZoom is disabled. Use §f:module --toggle zoom §cto enable it.")
                    return
                }

                ZoomManager.trigger()
            }

            "fullbright" -> {
                if (!FullbrightManager.config.enabled) {
                    CommandManager.reply("${MainClient.PREFIX}§cFullbright is disabled. Use §f:module --toggle fullbright §cto enable it.")
                    return
                }

                FullbrightManager.trigger()
            }

            "customchat" -> {
                if (!ChatManager.config.enabled) {
                    CommandManager.reply("${MainClient.PREFIX}§cCustomChat is disabled. Use §f:module --toggle customchat §cto enable it.")
                    return
                }
                ChatManager.toggle()
            }

            "autoreconnect" -> {
                CommandManager.reply("${MainClient.PREFIX}§7AutoReconnect has no trigger action. Use §f:module --toggle autoreconnect §7to enable/disable it.")
            }

            else -> unknownModule(name)
        }
    }

    private fun handleToggle(name: String) {
        when (name) {
            "freelook" -> {
                val newEnabled = !FreelookManager.config.enabled
                FreelookManager.config.enabled = newEnabled

                if (!newEnabled && FreelookManager.isActive()) FreelookManager.trigger()
                ConfigManager.saveConfig("config/modules/freelook.jsonc", FreelookManager.config)
            }

            "zoom" -> {
                val newEnabled = !ZoomManager.config.enabled
                ZoomManager.config.enabled = newEnabled

                if (!newEnabled && ZoomManager.isActive()) ZoomManager.stopZoom()
                ConfigManager.saveConfig("config/modules/zoom.jsonc", ZoomManager.config)
            }

            "fullbright" -> {
                val newEnabled = !FullbrightManager.config.enabled
                FullbrightManager.config.enabled = newEnabled

                if (!newEnabled) FullbrightManager.disable()
                ConfigManager.saveConfig("config/modules/fullbright.jsonc", FullbrightManager.config)
            }

            "customchat" -> {
                val newEnabled = !ChatManager.config.enabled
                ChatManager.config.enabled = newEnabled
                ConfigManager.saveConfig("config/modules/customchat.jsonc", ChatManager.config)
            }

            "autoreconnect" -> {
                val newEnabled = !ReconnectManager.config.enabled
                ReconnectManager.config.enabled = newEnabled
                ConfigManager.saveConfig("config/modules/autoreconnect.jsonc", ReconnectManager.config)
            }

            else -> unknownModule(name)
        }
    }

    private fun unknownModule(name: String) {
        CommandManager.reply(
            "${MainClient.PREFIX}§cUnknown module: §f$name",
            " §7Available: §f${MODULES.joinToString(", ")}"
        )
    }
}
