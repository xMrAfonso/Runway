package me.mrafonso.runway.integration.tag

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

abstract class AbstractTag {

    abstract fun retrieve() : TagResolver
}