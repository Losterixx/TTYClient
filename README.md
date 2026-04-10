# TTYClient

**A terminal-themed Minecraft client for those who live in the shell.**

TTYClient is built around one idea: your Minecraft client should feel as scriptable and customizable as your Linux system. Type client has commands starting with a `;` prefix, bind keys to chat messages, Minecraft commands, or literal bash scripts, and configure everything through plain JSONC text files - all live-reloaded, no restarts needed.

---

## Requirements

- Minecraft **26.1** with [Fabric Loader](https://fabricmc.net/) ≥ 0.18.5
- [Fabric API](https://modrinth.com/mod/fabric-api) 0.144.4+26.1
- [Kotlin for Fabric](https://modrinth.com/mod/fabric-language-kotlin) 1.13.9+kotlin.2.3.10

## Installation

Drop `ttyclient-*.jar` alongside Fabric API and Kotlin for Fabric into your `mods/` folder. On first launch, TTYClient creates its config directory at `~/TTYClient/`.

---

## Usage

Type `;` followed by a command in chat. Commands are intercepted client-side and **never sent to the server**.

Important commands:
```
;help                         list all commands
;mcfetch                      neofetch-style game info
;module --trigger <module>    trigger a module
;module --toggle <module>     enable or disable a module
;config                       open the in-game config editor
```

The command prefix (default `;`) can be changed in `~/TTYClient/config/general.jsonc`.

---

## Keybinds

Keybinds live in `~/TTYClient/config/keybinds.jsonc` and support multiple execution types:

```jsonc
{ "bind": "ALT+C",    "type": "client",  "exec": "config" },
{ "bind": "C",        "type": "client",  "exec": "module --trigger zoom" },
{ "bind": "CTRL+F1",  "type": "command", "exec": "gamemode creative" },
{ "bind": "F9",       "type": "bash",    "exec": "notify-send 'test notify'" },
{ "bind": "DELETE",   "type": "action",  "exec": "Disconnect" }
```

Types: `client`, `command`, `chat`, `bash`, `sequence`, `action`, `noop`

---

## Modules

| Module | Default bind | Description                                     |
|---|-----|-------------------------------------------------|
| **Zoom** | `C` | Smooth animated FOV zoom, scrollable, hold or toggle |
| **Freelook** | `X` | Detach camera from look direction in third-person |
| **Fullbright** | `B` | Remove darkness via gamma boost or Night Vision |
| **Custom Crosshair** | -   | 15x15 pixel grid crosshair, fully configurable in JSONC |
| **Custom Chat** | `I` | Resizable chat window, drag to resize, unlimited history, toggable chat |
| **AutoReconnect** | -   | Auto-reconnect with countdown after disconnect  |
| **RenderUtils** | -   | Custom shield/totem model transforms, low fire overlay |
| **Screenshots** | -   | F2 screenshot with copy/open/upload actions in chat, resolution scaling up to 8k |
| **Utils** | -   | Durability warning: flashing action-bar alert + pling sound |

All module configs are in `~/TTYClient/config/modules/`, with live-reload on save.

---

## Config Editor

Press `ALT+C` or run `;config` to open the built-in in-game editor - JSONC syntax highlighting, inline hex color swatches, undo/redo, and a persistent file tree.

---

## Building

```bash
git clone https://github.com/Losterixx/TTYClient.git
cd TTYClient
./gradlew build
# -> build/libs/ttyclient-*.jar
```

---

## License

[MIT License](LICENSE)
