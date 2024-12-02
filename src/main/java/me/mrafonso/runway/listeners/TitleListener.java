package me.mrafonso.runway.listeners;

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleSubtitle;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleText;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.entity.Player;

public class TitleListener extends AbstractListener {
    public TitleListener(ProcessHandler processHandler, ConfigManager configManager) {
        super(processHandler, configManager);
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent e) {
        PacketTypeCommon type = e.getPacketType();
        if (!config.getOrDefault("listeners.titles", true) || (
            type != PacketType.Play.Server.SET_TITLE_TEXT &&
            type != PacketType.Play.Server.SET_TITLE_SUBTITLE)) return;

        Player player = e.getPlayer();

        if (type == PacketType.Play.Server.SET_TITLE_TEXT) {
            WrapperPlayServerSetTitleText packet = new WrapperPlayServerSetTitleText(e);
            packet.setTitle(handler.processComponent(packet.getTitle(), player));

        } else {
            WrapperPlayServerSetTitleSubtitle packet = new WrapperPlayServerSetTitleSubtitle(e);
            packet.setSubtitle(handler.processComponent(packet.getSubtitle(), player));
        }
    }
}