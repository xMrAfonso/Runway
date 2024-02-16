package me.mrafonso.runway.command;

import de.leonhard.storage.Config;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Join;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("runway")
public class RunwayCommand {

    private final ConfigManager configManager;
    private final ProcessHandler processHandler;
    private final Config lang;

    public RunwayCommand(ConfigManager configManager, ProcessHandler processHandler) {
        this.configManager = configManager;
        this.processHandler = processHandler;
        this.lang = configManager.lang();
    }

    @Command("reload")
    @Permission("runway.reload")
    public void reloadCommand(CommandSender sender) {
        configManager.reload();
        processHandler.reloadPlaceholders();
        sender.sendRichMessage(lang.getString("reload-success"));
        if (sender instanceof Player player) player.playSound(player, "block.note_block.bell", 1, 1);
    }

    @Command("parse")
    public void parseCommand(Player player, @Join(" ") String text) {
        if (text.isBlank()) {
            player.sendRichMessage(lang.getString("not-enough-arguments"));
            player.playSound(player, "block.note_block.bass", 1, 1);
            return;
        }

        if (text.contains("§")) {
            player.sendRichMessage(lang.getString("parse-fail"));
            player.playSound(player, "block.note_block.bass", 1, 1);
            return;
        }

        TagResolver.Single placeholder = Placeholder.component("text", processHandler.processComponent(Component.text("[mm]" + text), player));
        player.sendRichMessage(lang.getString("parse-success"), placeholder);
        player.playSound(player, "entity.experience_orb.pickup", 1, 1);
    }

}
