package me.mrafonso.runway.command

import dev.triumphteam.cmd.bukkit.annotation.Permission
import dev.triumphteam.cmd.core.annotations.Command
import dev.triumphteam.cmd.core.annotations.Join
import me.mrafonso.runway.config.Lang
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.handler.ConfigHandler
import me.mrafonso.runway.handler.ProcessHandler
import me.mrafonso.runway.handler.ResolverHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Command("runway")
class RunwayCommand(
    private val configHandler: ConfigHandler,
    private val resolverHandler: ResolverHandler,
    private val processHandler: ProcessHandler
) {

    @Command("reload")
    @Permission("runway.reload")
    fun reloadCommand(sender: CommandSender) {
        configHandler.reloadAll()
        resolverHandler.reloadAll()
        sender.sendRichMessage(configHandler.get<Lang>().reloadSuccess)
        if (sender is Player) sender.playSound(sender, "block.note_block.bell", 1f, 1f)
    }

    @Command("parse")
    @Permission("runway.parse")
    fun parseCommand(sender: CommandSender, @Join(" ") text: String) {
        if (text.isBlank()) {
            sender.sendRichMessage(configHandler.get<Lang>().notEnoughArguments)
            if (sender is Player) sender.playSound(sender, "block.note_block.bass", 1f, 1f)
            return
        }

        if (text.contains("§")) {
            sender.sendRichMessage(configHandler.get<Lang>().parseFail)
            if (sender is Player) sender.playSound(sender, "block.note_block.bass", 1f, 1f)
            return
        }

        val toBeParsed = configHandler.get<Settings>().prefix.value + text
        val parsedText = if (sender is Player) processHandler.processComponent(Component.text(toBeParsed), sender)
                         else processHandler.processComponent(Component.text(toBeParsed))

        val placeholder = parsedText?.let {
            Placeholder.component("text", parsedText)
        } ?: Placeholder.component("text", Component.text(text))

        sender.sendRichMessage(configHandler.get<Lang>().parseSuccess, placeholder)
        if (sender is Player) sender.playSound(sender, "entity.experience_orb.pickup", 1f, 1f)
    }
}