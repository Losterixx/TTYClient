package dev.losterixx.ttyclient.mixin.general;

import dev.losterixx.ttyclient.client.MainClient;
import dev.losterixx.ttyclient.client.commandsys.Command;
import dev.losterixx.ttyclient.client.commandsys.CommandContext;
import dev.losterixx.ttyclient.client.commandsys.CommandManager;
import dev.losterixx.ttyclient.client.config.ConfigManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenClickCommandMixin {

    @Inject(method = "clickCommandAction", at = @At("HEAD"), cancellable = true)
    private static void onClickCommandAction(LocalPlayer player, String command, Screen screenAfterCommand, CallbackInfo ci) {
        String prefix = ConfigManager.INSTANCE.getGeneral().getCommandPrefix();
        if (!command.startsWith(prefix)) return;

        ci.cancel();

        String input = command.substring(prefix.length()).trim();
        if (input.isEmpty()) return;

        CommandContext ctx = CommandManager.INSTANCE.parse(input);
        Command cmd = CommandManager.INSTANCE.getCommand(ctx.getName());

        if (cmd != null) {
            try {
                cmd.execute(ctx);
            } catch (Exception e) {
                CommandManager.INSTANCE.reply(MainClient.PREFIX + "§cError in command §f" + ctx.getName() + "§c: §7" + e.getMessage());
            }
        } else {
            CommandManager.INSTANCE.reply(MainClient.PREFIX + "§cUnknown command: §f" + ctx.getName());
        }
    }
}


