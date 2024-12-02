package me.mrafonso.runway.listeners;

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class SystemChatListener extends AbstractListener {
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SystemChatListener(ProcessHandler processHandler, ConfigManager configManager) {
        super(processHandler, configManager);
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent e) {
        if (!config.getOrDefault("listeners.system-messages", true) ||
            e.getPacketType() != PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) return;

        Player player = e.getPlayer();
        WrapperPlayServerSystemChatMessage packet = new WrapperPlayServerSystemChatMessage(e);
        String message = mm.serialize(packet.getMessage());

        if (message.startsWith("<lang:multiplayer.message_not_delivered:")) {
            e.setCancelled(true);
            return;
        }

        if (message.contains("[actionbar]")) {
            message = message.replace("[actionbar]", "");
            e.setCancelled(true);
            player.sendActionBar(handler.processComponent(message, player));
        } else {
            packet.setMessage(handler.processComponent(message, player));
        }
    }
}
