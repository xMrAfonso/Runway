package me.mrafonso.runway.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import de.leonhard.storage.Config;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.plugin.Plugin;

public abstract class AbstractListener extends PacketAdapter {

    protected final ProcessHandler handler;
    protected final Config config;

    public AbstractListener(Plugin plugin, ProcessHandler handler, ConfigManager configManager, PacketType... types) {
        super(plugin, types);
        this.handler = handler;
        this.config = configManager.config();
    }

    @Override
    public abstract void onPacketSending(PacketEvent e);
}
