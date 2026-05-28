package me.mrafonso.runway.integration.tag

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.tag.Modifying
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class PlainTag : AbstractTag() {
    override fun retrieve(): TagResolver {
        return TagResolver.resolver(setOf("plain")) { _, _ ->
            Modifying { current, _ ->
                if (current is TextComponent) {
                    Component.text(current.content())
                } else {
                    Component.empty()
                }
            }
        }
    }
}
