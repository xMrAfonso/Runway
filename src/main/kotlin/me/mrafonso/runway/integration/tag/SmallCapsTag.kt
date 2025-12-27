package me.mrafonso.runway.integration.tag

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.tag.Modifying
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class SmallCapsTag : AbstractTag() {

    private val smallCapsMap = mapOf(
        'a' to 'ᴀ', 'b' to 'ʙ', 'c' to 'ᴄ', 'd' to 'ᴅ', 'e' to 'ᴇ', 'f' to 'ғ',
        'g' to 'ɢ', 'h' to 'ʜ', 'i' to 'ɪ', 'j' to 'ᴊ', 'k' to 'ᴋ', 'l' to 'ʟ',
        'm' to 'ᴍ', 'n' to 'ɴ', 'o' to 'ᴏ', 'p' to 'ᴘ', 'q' to 'ǫ', 'r' to 'ʀ',
        's' to 's', 't' to 'ᴛ', 'u' to 'ᴜ', 'v' to 'ᴠ', 'w' to 'ᴡ', 'x' to 'x',
        'y' to 'ʏ', 'z' to 'ᴢ'
    )

    override fun retrieve(): TagResolver {
        return TagResolver.resolver(setOf("smallcaps", "sc")) { _, _ ->
            Modifying { current, _ ->
                if (current is TextComponent) {
                    current.content(convertToSmallCaps(current.content())).children(listOf())
                } else {
                    current.children(listOf())
                }
            }
        }
    }

    private fun transformComponent(component: Component): Component {
        if (component is TextComponent) {
            val transformed = component.content(convertToSmallCaps(component.content()))
            return transformed.children(component.children().map { transformComponent(it) })
        }
        return component.children(component.children().map { transformComponent(it) })
    }

    private fun convertToSmallCaps(text: String): String {
        return text.map { char ->
            smallCapsMap[char.lowercaseChar()] ?: char
        }.joinToString("")
    }
}
