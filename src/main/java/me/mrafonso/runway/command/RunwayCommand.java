package me.mrafonso.runway.command;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.command.CommandSender;

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
    }
}
