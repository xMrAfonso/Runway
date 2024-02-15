package me.mrafonso.runway.command;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.core.annotations.Join;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.config.section.Inventory;
import me.mrafonso.runway.config.section.Messages;
import me.mrafonso.runway.util.ProcessHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

@Command("runway")
public class RunwayCommand {

    private final ConfigManager configManager;
    private final ProcessHandler processHandler;

    public RunwayCommand(ConfigManager configManager, ProcessHandler processHandler) {
        this.configManager = configManager;
        this.processHandler = processHandler;
    }

    @Command("reload")
    @Permission("runway.reload")
    public void reloadCommand(CommandSender sender) {
        configManager.reload();
        processHandler.reloadPlaceholders();
        sender.sendRichMessage(configManager.config().messages().reloadSuccess());
        if (sender instanceof Player player) player.playSound(player, "block.note_block.bell", 1, 1);
    }

    @Command("parse")
    public void parseCommand(Player player, @Join(" ") String text) {
        Messages messages = configManager.config().messages();
        if (text.isBlank()) {
            player.sendRichMessage(messages.notEnoughArguments());
            player.playSound(player, "block.note_block.bass", 1, 1);
            return;
        }

        if (text.contains("§")) {
            player.sendRichMessage(messages.parseFail());
            player.playSound(player, "block.note_block.bass", 1, 1);
            return;
        }

        TagResolver.Single placeholder = Placeholder.component("text", processHandler.processComponent(Component.text("[mm]" + text), player));
        player.sendRichMessage(messages.parseSuccess(), placeholder);
        player.playSound(player, "entity.experience_orb.pickup", 1, 1);
    }

}
