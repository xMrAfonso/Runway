package me.mrafonso.runway;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import de.leonhard.storage.Config;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.bukkit.message.BukkitMessageKey;
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
    public void onEnable() {
        configManager.reload();

        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().log(Level.SEVERE, "ProtocolLib not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
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

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new SystemChatListener(this, processHandler, configManager));
        protocolManager.addPacketListener(new TablistListener(this, processHandler, configManager));
        protocolManager.addPacketListener(new ServerPingListener(this, processHandler, configManager));
        protocolManager.addPacketListener(new InventoryListener(this, processHandler, configManager));
        protocolManager.addPacketListener(new TitleListener(this, processHandler, configManager));
        protocolManager.addPacketListener(new ScoreboardListener(this, processHandler, configManager));
        protocolManager.addPacketListener(new ItemListener(this, processHandler, configManager));

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
