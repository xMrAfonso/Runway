package me.mrafonso.runway.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.plugin.Plugin;

public abstract class AbstractListener extends PacketAdapter {

    protected final ProcessHandler handler;
    protected final ConfigManager configManager;

    public AbstractListener(Plugin plugin, ProcessHandler handler, ConfigManager configManager, PacketType... types) {
        super(plugin, types);
        this.handler = handler;
        this.configManager = configManager;
    }

    @Override
    public abstract void onPacketSending(PacketEvent e);
}
