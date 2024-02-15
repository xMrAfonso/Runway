package me.mrafonso.runway;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.core.message.ContextualKey;
import dev.triumphteam.cmd.core.message.MessageKey;
import dev.triumphteam.cmd.core.message.MessageResolver;
import dev.triumphteam.cmd.core.message.context.MessageContext;
import me.mrafonso.runway.command.RunwayCommand;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.listeners.*;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Runway extends JavaPlugin {
    private final ConfigManager configManager = new ConfigManager(this.getDataFolder().toPath());

    @Override
    public void onEnable() {
        configManager.reload();

        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().log(Level.SEVERE, "ProtocolLib not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            configManager.config().placeholderHook().placeholderAPI(false);
            getLogger().log(Level.WARNING, "PlaceholderAPI not found! Disabling PlaceholderAPI support...");
        }

        if (getServer().getPluginManager().getPlugin("MiniPlaceholder") == null) {
            configManager.config().placeholderHook().miniPlaceholders(false);
            getLogger().log(Level.WARNING, "MiniPlaceholder not found! Disabling MiniPlaceholder support...");
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

        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerCommand(new RunwayCommand(configManager, processHandler));

        commandManager.registerMessage(MessageKey.INVALID_ARGUMENT, (sender, context) -> {
            sender.sendRichMessage(configManager.config().messages().invalidArgument());
        });

        commandManager.registerMessage(MessageKey.UNKNOWN_COMMAND, (sender, context) -> {
            sender.sendRichMessage(configManager.config().messages().unknownCommand());
        });

        commandManager.registerMessage(MessageKey.NOT_ENOUGH_ARGUMENTS, (sender, context) -> {
            sender.sendRichMessage(configManager.config().messages().notEnoughArguments());
        });

        getLogger().log(Level.INFO, "The plugin has been successfully enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Disabling the plugin...");
    }
}
