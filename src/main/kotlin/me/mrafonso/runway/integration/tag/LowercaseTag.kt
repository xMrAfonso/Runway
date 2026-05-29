package me.mrafonso.runway.integration.tag

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.tag.Modifying
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class LowercaseTag : AbstractTag() {
    override fun retrieve(): TagResolver {
        return TagResolver.resolver(setOf("lowercase", "lower")) { _, _ ->
            Modifying { current, _ ->
                if (current is TextComponent) {
                    current.content(current.content().lowercase()).children(listOf())
                } else {
                    current.children(listOf())
                }
            }
        }
    }
}