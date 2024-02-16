package me.mrafonso.runway.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TitleListener extends AbstractListener {
    public TitleListener(Plugin plugin, ProcessHandler processHandler, ConfigManager configManager) {
        super(plugin, processHandler, configManager, PacketType.Play.Server.SET_TITLE_TEXT,
                                                     PacketType.Play.Server.SET_SUBTITLE_TEXT);
    }

    @Override
    public void onPacketSending(PacketEvent e) {
        if (!config.getOrDefault("listeners.titles", true)) return;
        PacketContainer packet = e.getPacket();
        Player player = e.getPlayer();

        if (packet.getType() == PacketType.Play.Server.SET_TITLE_TEXT) {
            packet.getChatComponents().modify(0, component -> handler.processComponent(component, player));

        } else if (packet.getType() == PacketType.Play.Server.SET_SUBTITLE_TEXT) {
            packet.getChatComponents().modify(0, component -> handler.processComponent(component, player));
        }
    }
}