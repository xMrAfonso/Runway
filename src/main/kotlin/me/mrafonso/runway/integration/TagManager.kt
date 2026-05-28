package me.mrafonso.runway.integration

import me.mrafonso.runway.integration.tag.AbstractTag
import me.mrafonso.runway.integration.tag.ActionbarTag
import me.mrafonso.runway.integration.tag.LowercaseTag
import me.mrafonso.runway.integration.tag.PAPITag
import me.mrafonso.runway.integration.tag.PlainTag
import me.mrafonso.runway.integration.tag.SmallCapsTag
import me.mrafonso.runway.integration.tag.UppercaseTag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class TagManager(
    private val tags: MutableList<AbstractTag> = mutableListOf()
) {

    init {
        register(
            PAPITag(),
            ActionbarTag(),
            SmallCapsTag(),
            UppercaseTag(),
            LowercaseTag(),
            PlainTag()
        )
    }

    fun register(vararg tag: AbstractTag) {
        tags.addAll(tag)
    }

    fun resolver() : TagResolver {
        val resolvers = tags.map { it.retrieve() }
        return TagResolver.resolver(*resolvers.toTypedArray())
    }
}
