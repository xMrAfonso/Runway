package me.mrafonso.runway.listeners;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import de.leonhard.storage.Config;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;

public abstract class AbstractListener extends SimplePacketListenerAbstract {

    protected final ProcessHandler handler;
    protected final Config config;

    public AbstractListener(ProcessHandler handler, ConfigManager configManager) {
        super(PacketListenerPriority.NORMAL);
        this.handler = handler;
        this.config = configManager.config();
    }
}
