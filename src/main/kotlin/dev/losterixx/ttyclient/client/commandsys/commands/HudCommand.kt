package dev.losterixx.ttyclient.client.commandsys.commands

import dev.losterixx.ttyclient.client.commandsys.Command
import dev.losterixx.ttyclient.client.commandsys.CommandContext
import dev.losterixx.ttyclient.client.commandsys.CommandManager
import dev.losterixx.ttyclient.client.hud.HudEditorScreen

class HudCommand : Command(
    name = "hud",
    aliases = listOf("hudeditor"),
    description = "Open the HUD editor screen"
) {
    override fun execute(ctx: CommandContext) {
        CommandManager.client.execute {
            CommandManager.client.setScreen(HudEditorScreen())
        }
    }
}

