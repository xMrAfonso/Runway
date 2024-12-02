package me.mrafonso.runway.listeners;

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.entity.Player;

public class ScoreboardListener extends AbstractListener {
    public ScoreboardListener(ProcessHandler processHandler, ConfigManager configManager) {
        super(processHandler, configManager);
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent e) {
        PacketTypeCommon type = e.getPacketType();
        if (!config.getOrDefault("listeners.scoreboards", true) || (
            type != PacketType.Play.Server.SCOREBOARD_OBJECTIVE &&
            type != PacketType.Play.Server.UPDATE_SCORE)) return;

        Player player = e.getPlayer();

        if (type == PacketType.Play.Server.SCOREBOARD_OBJECTIVE) {
            WrapperPlayServerScoreboardObjective packet = new WrapperPlayServerScoreboardObjective(e);
            packet.setDisplayName(handler.processComponent(packet.getDisplayName(), player));

        } else {
            WrapperPlayServerUpdateScore packet = new WrapperPlayServerUpdateScore(e);
            packet.setEntityDisplayName(handler.processComponent(packet.getEntityDisplayName(), player));
        }
    }
}