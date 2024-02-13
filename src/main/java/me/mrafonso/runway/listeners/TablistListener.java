package me.mrafonso.runway.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import me.mrafonso.runway.config.Config;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TablistListener extends AbstractListener {
    public TablistListener(Plugin plugin, ProcessHandler processHandler, ConfigManager configManager) {
        super(plugin, processHandler, configManager, PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
    }

    @Override
    public void onPacketSending(PacketEvent e) {
        if (configManager.config().listeners().tablist()) return;

        Player player = e.getPlayer();
        for (int i = 0; i < e.getPacket().getChatComponents().size(); i++) {
            e.getPacket().getChatComponents().modify(i, component -> handler.processComponent(component, player));
        }
    }
}