package me.mrafonso.runway.listeners;

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.entity.Player;

public class SystemChatListener extends AbstractListener {
    public SystemChatListener(ProcessHandler processHandler, ConfigManager configManager) {
        super(processHandler, configManager);
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent e) {
        if (!config.getOrDefault("listeners.system-messages", true) ||
            e.getPacketType() != PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) return;

        Player player = (Player) e.getPlayer();
        WrapperPlayServerSystemChatMessage packet = new WrapperPlayServerSystemChatMessage(e);
        packet.setMessage(handler.processComponent(packet.getMessage(), player));
    }
}
