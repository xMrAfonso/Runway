package me.mrafonso.runway.integration.tag

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Modifying
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

class ActionbarTag : AbstractTag() {
    override fun retrieve(): TagResolver {
        return TagResolver.resolver(setOf("actionbar", "ac")) { _, context ->
            Modifying { current, depth ->
                if (depth == 0) {
                    val target = context.target()
                    if (target is Player) {
                        target.sendActionBar(current)
                    }
                }
                Component.empty().children(listOf())
            }
        }
    }
}