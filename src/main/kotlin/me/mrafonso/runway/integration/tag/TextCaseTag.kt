package me.mrafonso.runway.integration.tag

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.tag.Modifying
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.util.Locale

class UppercaseTag : AbstractTag() {
    override fun retrieve(): TagResolver {
        return TagResolver.resolver(setOf("uppercase", "upper")) { _, _ ->
            Modifying { current, _ ->
                if (current is TextComponent) {
                    current.content(current.content().uppercase(Locale.ROOT)).children(listOf())
                } else {
                    current.children(listOf())
                }
            }
        }
    }
}

class LowercaseTag : AbstractTag() {
    override fun retrieve(): TagResolver {
        return TagResolver.resolver(setOf("lowercase", "lower")) { _, _ ->
            Modifying { current, _ ->
                if (current is TextComponent) {
                    current.content(current.content().lowercase(Locale.ROOT)).children(listOf())
                } else {
                    current.children(listOf())
                }
            }
        }
    }
}
