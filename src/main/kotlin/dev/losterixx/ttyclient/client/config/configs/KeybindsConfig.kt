package dev.losterixx.ttyclient.client.config.configs

import dev.losterixx.ttyclient.client.config.Comment

data class KeybindsConfig(
    @Comment(
        "List of all keybinds. Each entry has three fields:",
        "  bind - the key or key combo to press. Modifiers: CTRL, SHIFT, ALT. Separate with '+'.",
        "           Examples: \"X\", \"ALT+C\", \"CTRL+SHIFT+F5\", \"LMB\"",
        "  type - what to execute:",
        "           client = TTYClient command (without prefix, e.g. \"config\")",
        "           command = Minecraft command (without '/', e.g. \"gamemode creative\")",
        "           chat = plain chat message (e.g. \"Hello world\")",
        "           bash = system shell command (e.g. \"notify-send hello\")",
        "           sequence = multiple actions separated by ';', each as \"type: exec\"",
        "                       (e.g. \"client: config; chat: opened config\")",
        "           action = built-in client action (check out the wiki for more information: ;help)",
        "           noop = disabled placeholder (bind is kept but does nothing)",
        "  exec - the value to execute, matching the chosen type.",
        "Example entry: { \"bind\": \"ALT+C\", \"type\": \"client\", \"exec\": \"config\" }"
    )
    var binds: List<ConfiguredKeybind> = emptyList()
)

data class ConfiguredKeybind(
    var bind: String,
    var type: String,
    var exec: String
)