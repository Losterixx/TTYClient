package dev.losterixx.ttyclient.client.commandsys.commands

import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.commandsys.Command
import dev.losterixx.ttyclient.client.commandsys.CommandContext
import dev.losterixx.ttyclient.client.commandsys.CommandManager
import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.manager.ModuleManager
import dev.losterixx.ttyclient.client.modules.autoreconnect.ReconnectManager
import dev.losterixx.ttyclient.client.modules.crosshair.CrosshairManager
import dev.losterixx.ttyclient.client.modules.customchat.ChatManager
import dev.losterixx.ttyclient.client.modules.freelook.FreelookManager
import dev.losterixx.ttyclient.client.modules.fullbright.FullbrightManager
import dev.losterixx.ttyclient.client.modules.notifications.NotificationManager
import dev.losterixx.ttyclient.client.modules.renderutils.RenderUtilsManager
import dev.losterixx.ttyclient.client.modules.screenshots.ScreenshotManager
import dev.losterixx.ttyclient.client.modules.utils.UtilsManager
import dev.losterixx.ttyclient.client.modules.zoom.ZoomManager

class ModuleCommand : Command(
    name = "module",
    aliases = listOf("mod"),
    description = "Control client modules",
    usage = ":module <--trigger/--toggle> <module>"
) {
    companion object {
        val MODULES: List<String> get() = ModuleManager.getAll().map { it.id }.sorted()
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

            else -> CommandManager.reply(
                "${MainClient.PREFIX}§7Module §f$name §7has no trigger action.",
                " §7Use §f:module --toggle $name §7to enable/disable it."
            )
        }
    }

    private fun handleToggle(name: String) {
        when (name) {
            "freelook" -> {
                val new = !FreelookManager.config.enabled
                FreelookManager.config.enabled = new
                ConfigManager.saveConfig(FreelookManager.configPath, FreelookManager.config)
                ModuleManager.postToggle(FreelookManager, new)
            }

            "zoom" -> {
                val new = !ZoomManager.config.enabled
                ZoomManager.config.enabled = new
                ConfigManager.saveConfig(ZoomManager.configPath, ZoomManager.config)
                ModuleManager.postToggle(ZoomManager, new)
            }

            "fullbright" -> {
                val new = !FullbrightManager.config.enabled
                FullbrightManager.config.enabled = new
                ConfigManager.saveConfig(FullbrightManager.configPath, FullbrightManager.config)
                ModuleManager.postToggle(FullbrightManager, new)
            }

            "customchat" -> {
                val new = !ChatManager.config.enabled
                ChatManager.config.enabled = new
                ConfigManager.saveConfig(ChatManager.configPath, ChatManager.config)
                ModuleManager.postToggle(ChatManager, new)
            }

            "autoreconnect" -> {
                val new = !ReconnectManager.config.enabled
                ReconnectManager.config.enabled = new
                ConfigManager.saveConfig(ReconnectManager.configPath, ReconnectManager.config)
                ModuleManager.postToggle(ReconnectManager, new)
            }

            "crosshair" -> {
                val new = !CrosshairManager.config.enabled
                CrosshairManager.config.enabled = new
                ConfigManager.saveConfig(CrosshairManager.configPath, CrosshairManager.config)
                ModuleManager.postToggle(CrosshairManager, new)
            }

            "renderutils" -> {
                val new = !RenderUtilsManager.config.enabled
                RenderUtilsManager.config.enabled = new
                ConfigManager.saveConfig(RenderUtilsManager.configPath, RenderUtilsManager.config)
                ModuleManager.postToggle(RenderUtilsManager, new)
            }

            "utils" -> {
                val new = !UtilsManager.config.enabled
                UtilsManager.config.enabled = new
                ConfigManager.saveConfig(UtilsManager.configPath, UtilsManager.config)
                ModuleManager.postToggle(UtilsManager, new)
            }

            "screenshots" -> {
                val new = !ScreenshotManager.config.enabled
                ScreenshotManager.config.enabled = new
                ConfigManager.saveConfig(ScreenshotManager.configPath, ScreenshotManager.config)
                ModuleManager.postToggle(ScreenshotManager, new)
            }

            "notifications" -> {
                val new = !NotificationManager.config.enabled
                NotificationManager.config.enabled = new
                ConfigManager.saveConfig(NotificationManager.configPath, NotificationManager.config)
                ModuleManager.postToggle(NotificationManager, new)
            }

            else -> CommandManager.reply(
                "${MainClient.PREFIX}§cUnknown module: §f$name",
                " §7Available: §f${MODULES.joinToString(", ")}"
            )
        }
    }
}
