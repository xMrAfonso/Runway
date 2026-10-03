package me.mrafonso.runway.processing

/**
 * Translates legacy section-sign codes (`§c`, `§l`, `§x§r§r§g§g§b§b`) into MiniMessage tags.
 *
 * MiniMessage rejects input containing legacy codes, which many older plugins still send,
 * so without this the whole text would be skipped.
 */
object LegacyTranslator {
    private val HEX = Regex("§x((?:§[0-9a-fA-F]){6})")
    private val CODE = Regex("§([0-9a-fk-orA-FK-OR])")

    private val TAGS = mapOf(
        '0' to "black", '1' to "dark_blue", '2' to "dark_green", '3' to "dark_aqua",
        '4' to "dark_red", '5' to "dark_purple", '6' to "gold", '7' to "gray",
        '8' to "dark_gray", '9' to "blue", 'a' to "green", 'b' to "aqua",
        'c' to "red", 'd' to "light_purple", 'e' to "yellow", 'f' to "white",
        'k' to "obfuscated", 'l' to "bold", 'm' to "strikethrough", 'n' to "underlined",
        'o' to "italic", 'r' to "reset"
    )

    fun toMiniMessage(text: String): String {
        if ('§' !in text) return text
        return text
            .replace(HEX) { match -> "<#${match.groupValues[1].replace("§", "")}>" }
            .replace(CODE) { match -> "<${TAGS.getValue(match.groupValues[1].lowercase()[0])}>" }
    }
}
