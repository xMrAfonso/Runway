package me.mrafonso.runway.integration.tag

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

abstract class AbstractTag {

    abstract fun retrieve() : TagResolver
}