package me.mrafonso.runway.listeners;

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.entity.Player;

public class ItemListener extends AbstractListener {

    public ItemListener(ProcessHandler processHandler, ConfigManager configManager) {
        super(processHandler, configManager);
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent e) {
        if (!config.getOrDefault("listeners.items", true) ||
            e.getPacketType() != PacketType.Play.Server.SET_SLOT) return;

        Player player = (Player) e.getPlayer();
        WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(e);
        packet.setItem(handler.processItem(packet.getItem(), player));
    }
}
