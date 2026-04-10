package dev.losterixx.ttyclient.mixin.general;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.losterixx.ttyclient.client.commandsys.Command;
import dev.losterixx.ttyclient.client.commandsys.CommandManager;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public class ChatInputSuggestorMixin {

    @Shadow @Final EditBox input;
    @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow private boolean keepSuggestions;
    @Shadow public void showSuggestions(boolean narrateFirstSuggestion) {}
    @Shadow public void hide() {}

    @Inject(method = "updateCommandInfo", at = @At("HEAD"), cancellable = true)
    private void ttyRefresh(CallbackInfo ci) {
        String text = input.getValue();
        String prefix = CommandManager.INSTANCE.getPrefix();

        if (!text.startsWith(prefix)) return;

        if (keepSuggestions) {
            ci.cancel();
            return;
        }

        String afterColon = text.substring(prefix.length());
        String[] parts = afterColon.split("\\s+", -1);
        String lastPart = parts[parts.length - 1].toLowerCase(Locale.ROOT);
        boolean flagContext = parts.length > 1;

        List<Suggestion> suggestionList = new ArrayList<>();
        StringRange range;

        if (!flagContext) {
            range = StringRange.between(prefix.length(), text.length());

            for (Command cmd : CommandManager.INSTANCE.getAll()) {
                if (cmd.getName().toLowerCase(Locale.ROOT).startsWith(lastPart)) {
                    suggestionList.add(new Suggestion(range, cmd.getName()));
                }
            }

            suggestionList.sort(Comparator.comparing(s -> s.getText().toLowerCase(Locale.ROOT)));
        } else {
            int startPos = text.length() - lastPart.length();
            range = StringRange.between(startPos, text.length());
            Command cmd = CommandManager.INSTANCE.getCommand(parts[0]);

            if (cmd != null) {
                if (lastPart.startsWith("-")) {
                    for (String flag : cmd.getSupportedFlags()) {
                        String full = "--" + flag;

                        if (full.startsWith(lastPart)) {
                            suggestionList.add(new Suggestion(range, full));
                        }
                    }
                } else {
                    List<String> previousParts = Arrays.asList(parts).subList(1, parts.length - 1);

                    for (String sug : cmd.getArgSuggestions(previousParts, lastPart)) {
                        suggestionList.add(new Suggestion(range, sug));
                    }
                }

                suggestionList.sort(Comparator.comparing(s -> s.getText().toLowerCase(Locale.ROOT)));
            }
        }

        if (pendingSuggestions != null) pendingSuggestions.cancel(false);

        if (!suggestionList.isEmpty()) {
            pendingSuggestions = CompletableFuture.completedFuture(new Suggestions(range, suggestionList));
            showSuggestions(false);
        } else {
            if (pendingSuggestions != null) pendingSuggestions.cancel(false);
            pendingSuggestions = null;
            input.setSuggestion(null);
            hide();
        }

        ci.cancel();
    }
}
