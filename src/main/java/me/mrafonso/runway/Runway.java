package me.mrafonso.runway;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.EventManager;
import de.leonhard.storage.Config;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.bukkit.message.BukkitMessageKey;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.mrafonso.runway.command.RunwayCommand;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.listeners.*;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.util.logging.Level;

public final class Runway extends JavaPlugin {
    private final ConfigManager configManager = new ConfigManager(this);
    private BukkitCommandManager<CommandSender> commandManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
            .reEncodeByDefault(true)
            .bStats(false)
            .checkForUpdates(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        configManager.reload();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            configManager.config().set("placeholder-hook.placeholderapi", false);
            getLogger().log(Level.WARNING, "PlaceholderAPI not found! Disabling PlaceholderAPI support...");
        }

        if (getServer().getPluginManager().getPlugin("MiniPlaceholder") == null) {
            configManager.config().set("placeholder-hook.miniplaceholders", false);
            getLogger().log(Level.WARNING, "MiniPlaceholder not found! Disabling MiniPlaceholders support...");
        }

        ProcessHandler processHandler = new ProcessHandler(configManager);
        processHandler.reloadPlaceholders();

        EventManager manager = PacketEvents.getAPI().getEventManager();
        manager.registerListeners(new SystemChatListener(processHandler, configManager),
                                  new TablistListener(processHandler, configManager),
                                  new InventoryListener(processHandler, configManager),
                                  new TitleListener(processHandler, configManager),
                                  new ScoreboardListener(processHandler, configManager),
                                  new ItemListener(processHandler, configManager));
        PacketEvents.getAPI().init();

        commandManager = BukkitCommandManager.create(this);
        commandManager.registerCommand(new RunwayCommand(configManager, processHandler));
        registerMessages();

        getLogger().log(Level.INFO, "The plugin has been successfully enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Disabling the plugin...");
    }

    public InputStream resourceAsStream(String path) {
        return getClassLoader().getResourceAsStream(path);
    }

    private void registerMessages() {
        Config lang = configManager.lang();

        commandManager.registerMessage(BukkitMessageKey.INVALID_ARGUMENT, (sender, context) -> sender.sendRichMessage(lang.getString("invalid-argument")));
        commandManager.registerMessage(BukkitMessageKey.UNKNOWN_COMMAND, (sender, context) -> sender.sendRichMessage(lang.getString("unknown-command")));
        commandManager.registerMessage(BukkitMessageKey.NOT_ENOUGH_ARGUMENTS, (sender, context) -> sender.sendRichMessage(lang.getString("not-enough-arguments")));
        commandManager.registerMessage(BukkitMessageKey.NO_PERMISSION, (sender, context) -> sender.sendRichMessage(lang.getString("no-permission")));
    }
}
