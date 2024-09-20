package me.mrafonso.runway.listeners;

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.entity.Player;

public class TablistListener extends AbstractListener {
    public TablistListener(ProcessHandler processHandler, ConfigManager configManager) {
        super(processHandler, configManager);
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent e) {
        if (!config.getOrDefault("listeners.tablist", true) ||
            e.getPacketType() == PacketType.Play.Server.PLAYER_LIST_HEADER_AND_FOOTER) return;

        Player player = (Player) e.getPlayer();
        WrapperPlayServerPlayerListHeaderAndFooter packet = new WrapperPlayServerPlayerListHeaderAndFooter(e);
        packet.setFooter(handler.processComponent(packet.getFooter(), player));
        packet.setHeader(handler.processComponent(packet.getHeader(), player));
    }
}