package dev.losterixx.ttyclient.client.commandsys

data class CommandContext(
    val raw: String,
    val name: String,
    val args: List<String>,
    val flags: Set<String>,
    val shortFlags: Set<Char>
) {
    fun hasFlag(long: String, short: Char? = null): Boolean = flags.contains(long) || (short != null && shortFlags.contains(short))

    fun hasAnyFlag(): Boolean = flags.isNotEmpty() || shortFlags.isNotEmpty()
}

