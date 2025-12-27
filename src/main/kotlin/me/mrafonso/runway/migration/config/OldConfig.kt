package me.mrafonso.runway.migration.config

import kotlinx.serialization.Serializable

@Serializable
data class OldConfig(
    val requirePrefix: RequirePrefix = RequirePrefix(),
    val placeholderHook: PlaceholderHook = PlaceholderHook(),
    val disableItalics: Boolean = true,
    val ignoreLegacy: Boolean = true,
    val listeners: Listeners = Listeners()
) {

    @Serializable
    data class RequirePrefix(
        val minimessage: Boolean = true,
        val placeholders: Boolean = true
    )

    @Serializable
    data class PlaceholderHook(
        val placeholderAPI: Boolean = true,
        val miniPlaceholders: Boolean = true
    )

    @Serializable
    data class Listeners(
        val systemMessages: Boolean = true,
        val tablist: Boolean = true,
        val titles: Boolean = true,
        val scoreboards: Boolean = true,
        val inventory: Inventory = Inventory(),
        val items: Boolean = true
    )

    @Serializable
    data class Inventory(
        val title: Boolean = true,
        val items: Boolean = true
    )
}